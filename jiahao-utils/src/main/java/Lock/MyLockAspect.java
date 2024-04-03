package Lock;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * <big>注解的切面处理类</big>
 *
 * @author 13684
 * @date 2024/4/3
 */
@Component
@Order(0)
@RequiredArgsConstructor
public class MyLockAspect {
    private final MyLockFactory myLockFactory;
    @Around("@annotation(myLock)")
    public Object tryLock(ProceedingJoinPoint pjp, MyLock myLock) throws Throwable {
        RLock lock = myLockFactory.getLock(myLock.lockType(), myLock.name());
        boolean isLock = myLock.lockStrategy().tryLock(lock, myLock);
        if (isLock) {
            return null;
        }

        try {
            return pjp.proceed();
        } finally {
            lock.unlock();
        }

    }
}
