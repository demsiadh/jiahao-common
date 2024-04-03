package Lock;

import org.redisson.api.RLock;

/**
 * <big>锁失败策略枚举</big>
 *
 * @author 13684
 * @date 2024/4/3
 */
public enum MyLockStrategy {
    // 尝试立即获取锁，如果获取失败，则直接返回false。
    SKIP_FAST(){
        @Override
        public boolean tryLock(RLock rLock, MyLock myLock) throws InterruptedException {
            return rLock.tryLock(0, myLock.leaseTime(), myLock.unit());
        }
    },
    // 尝试立即获取锁，如果获取失败，则抛出运行时异常。
    FAIL_FAST(){
        @Override
        public boolean tryLock(RLock rLock, MyLock myLock) throws InterruptedException {
            boolean b = rLock.tryLock(0, myLock.leaseTime(), myLock.unit());
            if (!b) {
                throw new RuntimeException("请求太频繁");
            }
            return true;
        }
    },
    // 不停尝试获取锁，直到成功为止。
    KEEP_TRYING(){
        @Override
        public boolean tryLock(RLock rLock, MyLock myLock) throws InterruptedException {
            boolean b = rLock.tryLock(myLock.leaseTime(), myLock.unit());
            return true;
        }
    },
    // 尝试获取锁，如果失败，则等待指定的等待时间后再次尝试，直到成功或超过最大等待时间。
    SKIP_AFTER_RETRY_TIMEOUT(){
        @Override
        public boolean tryLock(RLock lock, MyLock prop) throws InterruptedException {
            return lock.tryLock(prop.waitTime(), prop.leaseTime(), prop.unit());
        }
    },
    // 尝试获取锁，如果失败，则等待指定的等待时间后再次尝试，如果再次失败，则抛出运行时异常。
    FAIL_AFTER_RETRY_TIMEOUT(){
        @Override
        public boolean tryLock(RLock lock, MyLock prop) throws InterruptedException {
            boolean isLock = lock.tryLock(prop.waitTime(), prop.leaseTime(), prop.unit());
            if (!isLock) {
                throw new RuntimeException("请求太频繁");
            }
            return true;
        }
    },
    ;

    /**
     * 尝试获取锁的方法。
     * @param rLock 分布式锁对象。
     * @param myLock 自定义锁属性对象，包含锁的租期、单位和可选的等待时间。
     * @return 如果成功获取锁返回true，否则根据不同的策略返回false或抛出异常。
     * @throws InterruptedException 如果获取锁的过程中线程被中断则抛出此异常。
     */
    public abstract boolean tryLock(RLock rLock, MyLock myLock) throws InterruptedException;

}
