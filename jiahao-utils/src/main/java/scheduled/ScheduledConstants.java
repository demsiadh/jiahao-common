package scheduled;

/**
 * <big>任务常量类</big>
 * <p>可以做成读取配置文件的，nacos等可以配置热更新</p>
 *
 * @author 13684
 * @data 2024/5/10 下午4:20
 */
public interface ScheduledConstants {
    // 日调度任务重试时长
    long DAY_SCHEDULE_DURATION_HOUR = 3;
    // 日调度任务重试时间间隔
    long DAY_SCHEDULE_INTERVAL = 300;
}
