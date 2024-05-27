package scheduled;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <big>需要等待重试的任务</big>
 * <p></p>
 *
 * @author 13684
 * @data 2024/5/10 下午4:39
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledTask {
    // 需要重新执行的线程任务
    private Runnable runnable;
    // 任务id
    private String taskId;
}
