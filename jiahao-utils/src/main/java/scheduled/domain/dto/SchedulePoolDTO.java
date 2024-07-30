package scheduled.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * <big>线程池DTO对象</big>
 * <p>用来动态修改线程池参数</p>
 *
 * @author 13684
 * @data 2024/7/15 上午10:23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulePoolDTO {
    // 核心线程数
    private int corePoolSize;
    // 最大线程数
    private int maximumPoolSize;
    // 队列容量大小
    private int queueCapacity;
    // 线程空闲时间
    private int keepAliveTime;
    // 线程空闲时间单位
    private TimeUnit keepAliveTimeUnit;
    // 访问id
    private String accessId;
    // 访问key（鉴权使用）
    private String accessKey;
}
