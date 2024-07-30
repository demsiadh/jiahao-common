package scheduled.example;


import org.springframework.stereotype.Component;
import scheduled.annotation.InitiativeExecute;
import scheduled.util.ScheduledUtil;

import javax.annotation.Resource;

/**
 * <big>测试任务类</big>
 * <p></p>
 *
 * @author 13684
 * @data 2024/7/15 上午9:35
 */
@Component
@InitiativeExecute
public class TestTask {
    @Resource
    ScheduledUtil scheduledUtil;
    public void test() {
        scheduledUtil.getSupplier("TestTask.test", () -> {
            System.out.println("test");
            return false;
        });
    }
}
