package Lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * <big>锁工厂</big>
 *
 * @author 13684
 * @date 2024/4/3
 */
@Component
public class MyLockFactory {
    /**
     * 锁处理器映射，键为锁类型，值为根据锁名称获取相应锁的函数。
     */
    private final Map<MyLockType, Function<String, RLock>> lockHandlers;

    /**
     * MyLockFactory构造函数。
     * 初始化锁处理器映射，根据不同的锁类型注册相应的锁获取函数。
     *
     * @param redissonClient Redisson客户端，用于获取各种类型的锁。
     */
    public MyLockFactory(RedissonClient redissonClient) {
        this.lockHandlers = new EnumMap<>(MyLockType.class);
        // 注册可重入锁的获取函数
        this.lockHandlers.put(MyLockType.RE_ENTRANT_LOCK, redissonClient::getLock);
        // 注册公平锁的获取函数
        this.lockHandlers.put(MyLockType.FAIR_LOCK, redissonClient::getFairLock);
        // 注册读锁的获取函数
        this.lockHandlers.put(MyLockType.READ_LOCK, name -> redissonClient.getReadWriteLock(name).readLock());
        // 注册写锁的获取函数
        this.lockHandlers.put(MyLockType.WRITE_LOCK, name -> redissonClient.getReadWriteLock(name).writeLock());
    }

    /**
     * 根据锁类型和名称获取相应的锁。
     *
     * @param lockType 锁类型，决定获取哪种类型的锁。
     * @param name 锁的名称，用于标识具体的锁实例。
     * @return 返回根据锁类型和名称获取的Redisson锁实例。
     */
    public RLock getLock(MyLockType lockType, String name) {
        return lockHandlers.get(lockType).apply(name);
    }

}
