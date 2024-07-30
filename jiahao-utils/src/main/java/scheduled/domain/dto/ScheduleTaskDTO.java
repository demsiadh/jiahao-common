package scheduled.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <big>任务执行的dto对象</big>
 * <p>包含主动执行一个任务所需要的属性</p>
 *
 * @author 13684
 * @data 2024/7/15 上午10:18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleTaskDTO {
    // 任务全类名
    private String className;
    // 任务方法名
    private String methodName;
    // 访问id
    private String accessId;
    // 访问key（鉴权使用）
    private String accessKey;
}
