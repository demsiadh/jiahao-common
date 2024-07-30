package scheduled.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * <big>定时任务配置类</big>
 *
 * @author 13684
 * @data 2024/5/27 下午5:02
 */
@ConfigurationProperties(prefix = "schedule")
@Component
@Data
public class ScheduleConfig {
    // 定时任务最大重试次数
    private int maxRetryTimes = 3;

    // 定时任务重试时间间隔
    private String retryIntervalStr = "MINUTES";
    // 定时任务重试时间
    private int retryInterval = 1;
    // 定时任务重试时间单位
    private TimeUnit retryIntervalUnit;

    // 定时任务重试最大时间
    private String maxRetryIntervalStr = "MINUTES";
    // 定时任务重试最大时间
    private int maxRetryInterval = 10;
    // 定时任务重试最大时间单位
    private TimeUnit maxRetryIntervalUnit;

    @PostConstruct
    public void init() {
        for (TimeUnit unit : TimeUnit.values()) {
            if (unit.name().equals(retryIntervalStr)) {
                retryIntervalUnit = unit;
            }
            if (unit.name().equals(maxRetryIntervalStr)) {
                maxRetryIntervalUnit = unit;
            }
        }
    }
}
