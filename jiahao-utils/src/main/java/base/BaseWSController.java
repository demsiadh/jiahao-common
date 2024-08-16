package base;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 基础的websocket控制器
 * @author Kangjiahao
 */
@Slf4j
public abstract class BaseWSController {
    private static final Executor CALC_EXECUTOR = new ThreadPoolExecutor(
            // 核心线程数
            20,
            // 最大线程数
            100,
            // 空闲线程最大存活时间
            60L,
            // 空闲线程最大存活时间单位
            TimeUnit.SECONDS,
            // 等待队列及大小
            new ArrayBlockingQueue<>(1000),
            // 创建新线程时使用的工厂
            Executors.defaultThreadFactory(),
            // 当线程池达到最大时的处理策略
            // 丢掉最早未处理的任务
            new ThreadPoolExecutor.DiscardOldestPolicy()
    );

    private final static Map<String, Map<String, Set<Session>>> PARAM_KEY_2_SESSIONS_MAP = Maps.newConcurrentMap();
    private final static Map<String, Map<String, Timer>> PARAM_KEY_2_TIMER_MAP = Maps.newConcurrentMap();
    private final static Map<String, Map<String, Long>> PARAM_KEY_2_LAST_MODIFY_TIME_MAP = Maps.newConcurrentMap();

    void onOpenDefault(Session session, String... params) {
        String className = this.getClass().getName();
        log.info("请求开始, class:{}, params:{}", className, JSON.toJSONString(params));
        String reqKey = getReqKey(params);
        try {
            // 先推一次
            try {
                session.getBasicRemote().sendText(getMessage(params));
            } catch (IOException e) {
                log.error("推送消息失败, class:{}, params:{}", className, JSON.toJSONString(params));
            }
            initByClassName(className);
            if (PARAM_KEY_2_SESSIONS_MAP.get(className).containsKey(reqKey)) {
                PARAM_KEY_2_SESSIONS_MAP.get(className).get(reqKey).add(session);
            } else {
                PARAM_KEY_2_SESSIONS_MAP.get(className).put(reqKey, Sets.newHashSet(session));
            }
            if (!PARAM_KEY_2_TIMER_MAP.get(className).containsKey(reqKey)) {
                PARAM_KEY_2_TIMER_MAP.get(className).put(reqKey, newTimer(className, params));
            }
        } catch (Exception e) {
            log.warn("连接失败, class:{}, params:{}", className, JSON.toJSONString(params), e);
        }
    }

    void onCloseDefault() {
        log.info("用户退出, class:{}", this.getClass().getName());
    }

    void onMessageDefault(String message, Session session) {
        log.info("用户消息, class:{}, message: {}", this.getClass().getName(), message);
    }

    void onErrorDefault(Session session, Throwable error) {
        log.error("用户错误, class:{}", this.getClass().getName(), error);
    }

    public Timer newTimer(String className, String... params) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    doGetAndSend(className, params);
                } catch (Exception e) {
                    log.error("定时推送失败, , class:{}, params:{}", this.getClass().getName(), JSON.toJSONString(params), e);
                }
            }
        }, 10000, 10000);
        return timer;
    }

    String getMessage(String... params) {
        return null;
    }

    void doGetAndSend(String className, String... params) {
        String reqKey = getReqKey(params);
        Set<Session> sessions = PARAM_KEY_2_SESSIONS_MAP.get(className).get(reqKey);
        sessions.removeIf(session -> Objects.isNull(session) || !session.isOpen());
        if (CollectionUtil.isEmpty(sessions)) {
            removeTimer(className, reqKey);
        }
        Date currModifyTime = getLastModifyTime(reqKey);
        String sendMessage;
        if (Objects.isNull(currModifyTime)) {
            sendMessage = null;
        } else {
            if (Objects.nonNull(PARAM_KEY_2_LAST_MODIFY_TIME_MAP.get(className).get(reqKey))
                    && PARAM_KEY_2_LAST_MODIFY_TIME_MAP.get(className).get(reqKey) >= currModifyTime.getTime()) {
                log.info("最近无更新, class:{}, 最近更新时间: {}", this.getClass().getName(), DateUtil.format(currModifyTime, DatePattern.NORM_DATETIME_MS_FORMAT));
                return;
            }
            PARAM_KEY_2_LAST_MODIFY_TIME_MAP.get(className).put(reqKey, currModifyTime.getTime());
            sendMessage = getMessage(reqKey);
        }
        for (Session session : sessions) {
            if (!session.isOpen()) {
                continue;
            }
            CALC_EXECUTOR.execute(() -> {
                try {
                    if (!session.isOpen()) {
                        return;
                    }
                    session.getBasicRemote().sendText(sendMessage);
                } catch (Exception e) {
                    log.error("推送消息失败, class:{}, params:{}", this.getClass().getName(), JSON.toJSONString(params), e);
                }
            });
        }
    }

    Date getLastModifyTime(String... params) {
        return null;
    }

    void removeTimer(String className, String reqKey) {
        Timer timer = PARAM_KEY_2_TIMER_MAP.get(className).get(reqKey);
        if (Objects.nonNull(timer)) {
            timer.cancel();
            PARAM_KEY_2_TIMER_MAP.remove(reqKey);
        }
    }

    String getReqKey(String... params) {
        return String.join("|", params);
    }

    void initByClassName(String className) {
        if (!PARAM_KEY_2_SESSIONS_MAP.containsKey(className)) {
            PARAM_KEY_2_SESSIONS_MAP.put(className, Maps.newConcurrentMap());
        }
        if (!PARAM_KEY_2_TIMER_MAP.containsKey(className)) {
            PARAM_KEY_2_TIMER_MAP.put(className, Maps.newConcurrentMap());
        }
        if (!PARAM_KEY_2_LAST_MODIFY_TIME_MAP.containsKey(className)) {
            PARAM_KEY_2_LAST_MODIFY_TIME_MAP.put(className, Maps.newConcurrentMap());
        }
    }
}
