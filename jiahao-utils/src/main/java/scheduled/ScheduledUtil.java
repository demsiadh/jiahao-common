package scheduled;

import jodd.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
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
    // 线程池
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR =
            new ThreadPoolExecutor(2,2,0,
                    TimeUnit.SECONDS,new LinkedBlockingQueue<>(1000));

    // 消费者
    private final DConsumer<String, Supplier<Boolean>> cff = (taskId, supplier) -> {
        log.info("{} task start", taskId);
        try {
            // 接受任务执行的返回值
            Boolean apply = supplier.get();
            // 如果执行失败就重试
            if (!apply) {
                LocalDateTime deadlineTime = LocalDateTime.now().plusSeconds(scheduleConfig.getMaxRetryIntervalUnit().toSeconds(scheduleConfig.getMaxRetryInterval()));
                Runnable runnable = new ScheduledRunnable(taskId, supplier, deadlineTime);
                addTask(new ScheduledTask(runnable, taskId));
            }
        } catch (Exception e) {
            log.error("{} task fail Exception: {}", taskId, e.toString(), e);
        }
        log.info("{} task finish!", taskId);
    };

    // 处理无返回值任务的消费者
    private final DConsumer<String, Runnable> vff = (taskId, runnable) -> {
        log.info("{} task start", taskId);
        try {
            runnable.run();
        }catch (Exception e) {
            log.error("{} task fail Exception: {}", taskId, e.toString(), e);
        }finally {
            log.info("{} task finish!", taskId);
        }
    };

    @PostConstruct
    public void init() {
        // 初始化线程工厂 设置为守护线程
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("schedule-pool-%d").setDaemon(true).get();
        // 如果定时线程池已经存在则先关闭
        if (service != null) {
            service.shutdown();
        }
        // 创建定时任务线程池 异常处理直接抛出异常
        service = new ScheduledThreadPoolExecutor(2, threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 添加定时任务（一定时间后重新执行）
     * @param retryTask  需要重试的任务对象
     */
    private void addTask(ScheduledTask retryTask) {
        // 如果重试任务已经在map中则直接返回
        if (FUTURE_MAP.get(retryTask.getTaskId()) != null) {
            log.error("ScheduledService add task Failed! {} task already exist", retryTask.getTaskId());
            return;
        }
        // 将任务添加到定时服务中
        ScheduledFuture<?> scheduledFuture = service.scheduleWithFixedDelay(retryTask.getRunnable(), scheduleConfig.getRetryInterval(),
                scheduleConfig.getRetryInterval(), scheduleConfig.getRetryIntervalUnit());

        // 将任务添加到重试任务中
        FUTURE_MAP.put(retryTask.getTaskId(), scheduledFuture);
    }



    /**
     * 根据taskId 移除定时任务
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
     * @param taskId 任务id
     * @param supplier 执行任务的方法
     */
    public void getSupplier(String taskId, Supplier<Boolean> supplier) {
        THREAD_POOL_EXECUTOR.execute(()-> cff.accept(taskId,supplier));
    }

    /**
     * 执行无返回值的定时任务（不会失败重试）
     * @param taskId   任务id
     * @param runnable 执行任务的方法
     */
    public void getRunnable(String taskId, Runnable runnable) {
        THREAD_POOL_EXECUTOR.execute(()-> vff.accept(taskId,runnable));
    }

    // 消费者接口
    private interface DConsumer<T, R> {
        void accept(T t, R r);
    }

    private class ScheduledRunnable implements Runnable{
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
            }else if (LocalDateTime.now().isAfter(deadlineTime)) {
                remove(taskId);
                log.error("{} task retry times is over deadline time!", taskId);
                return;
            }
            log.info("{} task retry", taskId);
            // 执行任务
            boolean retryResult = supplier.get();
            if (retryResult) {
                remove(taskId);
            }
        }
    }
}
