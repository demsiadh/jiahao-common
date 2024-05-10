package scheduled;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * <big>定时任务工具类使用示例</big>
 * <p></p>
 *
 * @author 13684
 * @data 2024/5/10 下午5:14
 */
@Component
@RequiredArgsConstructor
public class ScheduledApplication {
    private final ScheduledUtil scheduledUtil;


    @Scheduled(cron = "0 0 7,8,12 * * ? ")
    public void testSimple() {
        scheduledUtil.getSupplier("testSimple", () -> {
            System.out.println("testSimple");
            return true;
        });
    }

    @Scheduled(cron = "0 0 7,8,12 * * ? ")
    public void testPool() {
        scheduledUtil.getSupplier("testPool", () -> {
            System.out.println("testPool");
            return false;
        });
    }
}
