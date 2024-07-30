package scheduled.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <big>自定义拒绝策略</big>
 * <p></p>
 *
 * @author 13684
 * @data 2024/7/8 下午5:00
 */
@Component
@Slf4j
public class ScheduleRejected implements RejectedExecutionHandler {

    /**
     * 当线程池满的时候，直接调用队列的put方法来进行阻塞
     * @param r the runnable task requested to be executed
     * @param executor the executor attempting to execute this task
     */
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        try {
            executor.getQueue().put(r);
        } catch (InterruptedException e) {
            // 不会被打断
        }
    }
}
