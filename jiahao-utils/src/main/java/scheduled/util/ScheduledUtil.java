package scheduled.util;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import scheduled.config.ScheduleConfig;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * <big>任务工具类</big>
 * <p>包含重试，尽量选择幂等的方法</p>
 *
 * @author 13684
 * @data 2024/5/10 下午4:11
 */
@Slf4j
@Component
public class ScheduledUtil {
    @Resource
    private ScheduleConfig scheduleConfig;
    private static ScheduledExecutorService service;
    // 任务集合
    private static final Map<String, ScheduledFuture<?>> FUTURE_MAP = new ConcurrentHashMap<>();
    // 长短任务的区分时间 10s
    private static final Long MAX_TIME = 10L;
    // 任务执行的默认耗时（用来处理任务第一次运行）
    private static final Long DEFAULT_TIME = 100L;
    // 记录执行时间，每次记录任务执行时间，用来分类
    private static final Map<String, Long> TASK_TIME_MAP = new ConcurrentHashMap<>();
    // 快速线程池，用来处理执行时间小于10s的任务（为了区分长任务和短任务）
    private static final ThreadPoolExecutor QUICK_HANDLER_EXECUTOR = new ThreadPoolExecutor(2, 4, 30,
            TimeUnit.SECONDS, new ResizeLinkedBlockingQueue<>(500),
            new ThreadFactoryBuilder().setNameFormat("quickHandler-pool-%d").setDaemon(true).build(),
            new ScheduleRejected());
    // 标准线程池，执行长任务，并且在开机时执行所有任务并统计时间
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(2, 5, 30,
            TimeUnit.SECONDS, new ResizeLinkedBlockingQueue<>(500),
            new ThreadFactoryBuilder().setNameFormat("standardHandler-pool-%d").setDaemon(true).build(),
            new ScheduleRejected());
    public static ThreadPoolExecutor getThreadPoolExecutor() {
        return THREAD_POOL_EXECUTOR;
    }
    public static ThreadPoolExecutor getQuickHandlerExecutor() {
        return QUICK_HANDLER_EXECUTOR;
    }
    // 消费者
    private final DConsumer<String, ScheduledRunnable> cff = (taskId, scheduledRunnable) -> {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("{} task start", taskId);
        try {
            // 接受任务执行的返回值
            Boolean apply = scheduledRunnable.supplier.get();
            // 如果执行失败就重试
            if (!apply) {
                addTask(scheduledRunnable);
            }
        } catch (Exception e) {
            log.error("{} task fail Exception: {}", taskId, e.getMessage(), e);
        }finally {
            log.info("{} task finish!", taskId);
            // 记录当前任务执行时间
            TASK_TIME_MAP.put(taskId, LocalDateTimeUtil.between(startTime, LocalDateTime.now()).getSeconds());
        }
    };


    @PostConstruct
    public void init() {
        // 初始化线程工厂 设置为守护线程
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("schedule-pool-%d").setDaemon(true).build();
        // 如果定时线程池已经存在则先关闭
        if (service != null) {
            service.shutdown();
        }
        // 创建定时任务线程池 异常处理直接抛出异常
        service = new ScheduledThreadPoolExecutor(2, threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 添加定时任务（一定时间后重新执行）
     *
     * @param retryTask 需要重试的任务对象
     */
    private void addTask(ScheduledRunnable retryTask) {
        // 如果重试任务已经在map中则直接返回
        if (FUTURE_MAP.get(retryTask.taskId) != null) {
            log.error("ScheduledService add task Failed! {} task already exist", retryTask.taskId);
            return;
        }
        // 将任务添加到定时服务中
        ScheduledFuture<?> scheduledFuture = service.scheduleWithFixedDelay(retryTask, scheduleConfig.getRetryInterval(),
                scheduleConfig.getRetryInterval(), scheduleConfig.getRetryIntervalUnit());

        // 将任务添加到重试任务中
        FUTURE_MAP.put(retryTask.taskId, scheduledFuture);
    }


    /**
     * 根据taskId 移除定时任务
     *
     * @param taskId 任务id
     */
    private void remove(String taskId) {
        if (FUTURE_MAP.get(taskId) == null) {
            log.error("ScheduledService remove task Failed! {} task not exist", taskId);
            return;
        }
        ScheduledFuture<?> scheduledFuture = FUTURE_MAP.get(taskId);
        // 取消当前任务，但并不强制取消正在执行的任务
        boolean cancel = scheduledFuture.cancel(false);
        if (!cancel) {
            log.error("{} task cancel fail", taskId);
        }
        // 将当前任务从任务队列中删除
        FUTURE_MAP.remove(taskId);
    }

    /**
     * 执行重试的定时任务
     *
     * @param taskId   任务id
     * @param supplier 执行任务的方法
     */
    public void getSupplier(String taskId, Supplier<Boolean> supplier) {
        // 创建重试任务
        ScheduledRunnable scheduledRunnable = new ScheduledRunnable(taskId, supplier, LocalDateTime.now().plusSeconds(scheduleConfig.getMaxRetryIntervalUnit().toSeconds(scheduleConfig.getMaxRetryInterval())));
        // 如果执行时间小于10s，放到快速线程池中
        if (TASK_TIME_MAP.getOrDefault(taskId, DEFAULT_TIME) <= MAX_TIME) {
            QUICK_HANDLER_EXECUTOR.execute(() -> cff.accept(taskId, scheduledRunnable));
        }else {
            THREAD_POOL_EXECUTOR.execute(() -> cff.accept(taskId, scheduledRunnable));
        }
    }


    // 消费者接口
    private interface DConsumer<T, R> {
        void accept(T t, R r);
    }

    public class ScheduledRunnable implements Runnable, Serializable {
        private int retryTimes;
        private final String taskId;
        private final Supplier<Boolean> supplier;
        private final LocalDateTime deadlineTime;

        public ScheduledRunnable(String taskId, Supplier<Boolean> supplier, LocalDateTime deadlineTime) {
            this.taskId = taskId;
            this.supplier = supplier;
            this.deadlineTime = deadlineTime;
            retryTimes = 0;
        }
        @Override
        public void run() {
            // 如果重试次数超过最大次数或者超过最大重试时间则移除任务
            if (retryTimes++ >= scheduleConfig.getMaxRetryTimes()) {
                remove(taskId);
                log.error("{} task retry times is over max retry times!", taskId);
                return;
            } else if (LocalDateTime.now().isAfter(deadlineTime)) {
                remove(taskId);
                log.error("{} task retry times is over deadline time!", taskId);
                return;
            }
            log.info("{} task retry", taskId);
            // 执行任务 如果执行成功则移除任务
            boolean retryResult = supplier.get();
            if (retryResult) {
                remove(taskId);
            }
        }
    }
}
