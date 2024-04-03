package Lock;

/**
 * <big>锁的类型</big>
 *
 * @author 13684
 * @date 2024/4/3
 */
public enum MyLockType {
    // 定义锁的类型
    RE_ENTRANT_LOCK,      // 可重入锁，允许同一个线程多次获取锁
    FAIR_LOCK,            // 公平锁，按照线程等待时间顺序获取锁
    READ_LOCK,            // 读锁，允许多个线程同时获取读锁，但阻止其他线程获取写锁
    WRITE_LOCK,           // 写锁，只允许一个线程获取写锁，阻塞其他所有读写锁请求

    ;
}
