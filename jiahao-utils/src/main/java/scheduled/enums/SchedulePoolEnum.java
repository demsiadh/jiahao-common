package scheduled.enums;

import base.BaseEnum;
import lombok.Getter;
import scheduled.util.ScheduledUtil;


import java.util.concurrent.ThreadPoolExecutor;

/**
 * <big>线程池枚举类</big>
 * <p>用来存储项目中所有的线程池</p>
 *
 * @author 13684
 * @data 2024/7/15 上午10:30
 */
public enum SchedulePoolEnum implements BaseEnum {
    QuickHandler(0, "快速线程池", ScheduledUtil.getQuickHandlerExecutor()),
    ThreadPool(1, "标准线程池", ScheduledUtil.getThreadPoolExecutor());
    ;
    final Integer value;
    final String desc;
    @Getter
    final ThreadPoolExecutor threadPoolExecutor;

    SchedulePoolEnum(Integer value, String desc, ThreadPoolExecutor threadPoolExecutor) {
        this.value = value;
        this.desc = desc;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getDesc() {
        return desc;
    }

}
