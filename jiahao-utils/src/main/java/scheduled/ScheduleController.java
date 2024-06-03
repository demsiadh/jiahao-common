package scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

/**
 * <big>任务控制器</big>
 * <p></p>
 *
 * @author 13684
 * @data 2024/6/3 上午10:31
 */
@Slf4j
@RestController
@RequestMapping("/schedule")
public class ScheduleController implements ApplicationContextAware {
    private ApplicationContext applicationContext;


    /**
     * 通过反射，主动调用定时任务(适用于单体架构，无分布式调度框架)
     * 需要进行鉴权，根据ak，或者其他方法进行鉴权
     *
     * @param className     调用的类的全路径名字
     * @param methodName    调用方法
     * @param accessId      用户ak
     * @param accessKey     用户ak
     * @return  调用结果
     */
    @PutMapping("/execute")
    public String executeSchedule(String className, String methodName, String accessId, String accessKey) {
        try {
            // 获取类
            Class<?> aClass = Class.forName(className);

            // 获取方法
            Method method = aClass.getDeclaredMethod(methodName);

            // 获取访问权限
            method.setAccessible(true);

            // 获取实例
            Object bean = applicationContext.getBean(aClass);
            // 执行实例方法
            method.invoke(bean);

        } catch (Exception e) {
            log.error("Schedule execute failed! ClassName: {}, MethodName: {}, AccessId: {}, AccessKey: {}", className, methodName, accessId, accessKey);
            log.error("Exception ", e);
        }
        return "success";
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
