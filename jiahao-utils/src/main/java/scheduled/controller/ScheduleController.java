package scheduled.controller;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.*;
import scheduled.annotation.InitiativeExecute;
import scheduled.domain.dto.SchedulePoolDTO;
import scheduled.domain.dto.ScheduleTaskDTO;
import scheduled.enums.SchedulePoolEnum;
import scheduled.util.ResizeLinkedBlockingQueue;

import java.lang.reflect.Method;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
     * @param scheduleTaskDTO   调度任务
     * @return  调用结果
     */
    @PostMapping("/execute")
    public String executeSchedule(@RequestBody ScheduleTaskDTO scheduleTaskDTO) {
        try {
            // 获取类
            Class<?> aClass = Class.forName(scheduleTaskDTO.getClassName());
            if (!aClass.isAnnotationPresent(InitiativeExecute.class)) {
                return "Class not annotated with @InitiativeExecute";
            }

            // 获取方法
            Method method = aClass.getDeclaredMethod(scheduleTaskDTO.getMethodName());

            // 获取访问权限
            method.setAccessible(true);

            // 获取实例
            Object bean = applicationContext.getBean(aClass);
            // 执行实例方法
            method.invoke(bean);
            log.info("Schedule execute success! ScheduleTaskDto={}", scheduleTaskDTO);
        } catch (Exception e) {
            log.error("Schedule execute failed! ScheduleTaskDto={}", scheduleTaskDTO);
            log.error("Exception ", e);
            return "exception";
        }
        return "success";
    }

    @PutMapping("/updatePool")
    public String updatePool(@RequestParam SchedulePoolEnum type, @RequestBody SchedulePoolDTO schedulePoolDTO) {
        try {
            // 获取线程池
            ThreadPoolExecutor threadPoolExecutor = type.getThreadPoolExecutor();

            // 设置核心线程数 (这里就直接if判断了，真实项目可以开发前端页面回显)
            if (ObjectUtil.isNotEmpty(schedulePoolDTO.getCorePoolSize())) {
                threadPoolExecutor.setCorePoolSize(schedulePoolDTO.getCorePoolSize());
            }

            // 设置最大线程数
            if (ObjectUtil.isNotEmpty(schedulePoolDTO.getMaximumPoolSize())) {
                threadPoolExecutor.setMaximumPoolSize(schedulePoolDTO.getMaximumPoolSize());
            }

            // 设置线程空闲时间
            if (!ObjectUtil.hasNull(schedulePoolDTO.getKeepAliveTime(), schedulePoolDTO.getKeepAliveTimeUnit())) {
                threadPoolExecutor.setKeepAliveTime(schedulePoolDTO.getKeepAliveTime(), schedulePoolDTO.getKeepAliveTimeUnit());
            }

            // 设置队列容量大小
            ResizeLinkedBlockingQueue<Runnable> queue = (ResizeLinkedBlockingQueue<Runnable>) threadPoolExecutor.getQueue();
            if (ObjectUtil.isNotEmpty(schedulePoolDTO.getQueueCapacity())) {
                queue.setCapacity(schedulePoolDTO.getQueueCapacity());
            }

            log.info("Update pool success! type={}, schedulePoolDTO={}", type, schedulePoolDTO);
            StringBuilder sb = new StringBuilder();
            for (SchedulePoolEnum value : SchedulePoolEnum.values()) {
                ResizeLinkedBlockingQueue<Runnable> tempQueue = (ResizeLinkedBlockingQueue<Runnable>) value.getThreadPoolExecutor().getQueue();
                sb.append(value.getDesc()).append(": corePoolSize=").append(value.getThreadPoolExecutor().getCorePoolSize())
                        .append(", maximumPoolSize=").append(value.getThreadPoolExecutor().getMaximumPoolSize())
                        .append(", keepAliveTime=").append(value.getThreadPoolExecutor().getKeepAliveTime(TimeUnit.SECONDS)).append("s")
                        .append(", queueCapacity=").append(tempQueue.getCapacity())
                        .append("\n");
            }
            log.info("All pool info: \n{}", sb);
        }catch (Exception e) {
            log.error("Update pool failed! type={}, schedulePoolDTO={}", type, schedulePoolDTO);
            log.error("Exception ", e);
            return "exception";
        }
        return "success";
    }

    @GetMapping("/getPool")
    public String getPool(@RequestParam SchedulePoolEnum type) {
        try {
            ThreadPoolExecutor threadPoolExecutor = type.getThreadPoolExecutor();
            ResizeLinkedBlockingQueue<Runnable> queue = (ResizeLinkedBlockingQueue<Runnable>) threadPoolExecutor.getQueue();
            return type.getDesc() + ": corePoolSize=" + threadPoolExecutor.getCorePoolSize() +
                    ", maximumPoolSize=" + threadPoolExecutor.getMaximumPoolSize() +
                    ", keepAliveTime=" + threadPoolExecutor.getKeepAliveTime(TimeUnit.SECONDS) +
                    ", queueCapacity=" + queue.getCapacity();

        }catch (Exception e) {
            log.error("Get pool failed! type={}", type);
            log.error("Exception ", e);
            return "exception";
        }
    }




    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
