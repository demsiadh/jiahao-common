package scheduled;

import jodd.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
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
public class ScheduledUtil {
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
                LocalDateTime localDateTime = LocalDateTime.now();
                Runnable runnable = () -> {
                    log.info("{} task fail, retry", taskId);
                    // 重试结果
                    Boolean retryResult = supplier.get();
                    if (retryResult || LocalDateTime.now().getHour() - localDateTime.getHour() >= ScheduledConstants.DAY_SCHEDULE_DURATION_HOUR) {
                        remove(taskId);
                    }
                };
                addTask(new ScheduledTask(runnable, taskId, ScheduledConstants.DAY_SCHEDULE_INTERVAL));
            }
        } catch (Exception e) {
            log.error("{} task fail Exception: {}", taskId, e.toString());
        }
        log.info("{} task finish!", taskId);
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
    public void addTask(ScheduledTask retryTask) {
        // 如果重试任务已经在map中则直接返回
        if (FUTURE_MAP.get(retryTask.getTaskId()) != null) {
            log.error("ScheduledService add task Failed! {} task already exist", retryTask.getTaskId());
            return;
        }

        // 将任务添加到定时服务中
        ScheduledFuture<?> scheduledFuture = service.scheduleWithFixedDelay(retryTask.getRunnable(), retryTask.getDelay(),
                retryTask.getDelay(), TimeUnit.SECONDS);

        // 将任务添加到重试任务中
        FUTURE_MAP.put(retryTask.getTaskId(), scheduledFuture);
    }



    /**
     * 根据taskId 移除定时任务
     * @param taskId 任务id
     */
    public void remove(String taskId) {
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
        new Thread(() -> cff.accept(taskId, supplier)).start();
    }

    /**
     * 重试的定时任务(线程池版)
     * @param taskId 任务id
     * @param supplier 执行任务的方法
     */
    public void getSupplierByPool(String taskId, Supplier<Boolean> supplier){
        THREAD_POOL_EXECUTOR.execute(()-> cff.accept(taskId,supplier));
    }

    // 消费者接口
    private interface DConsumer<T, R> {
        void accept(T t, R r);
    }
}
