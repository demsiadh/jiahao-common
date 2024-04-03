package Lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;


/**
 * 定义一个名为MyLock的注解，用于方法上，以提供锁的配置。
 * 这个注解可以在运行时被读取，允许指定锁的名称、等待时间、租约时间和时间单位。
 * <p>
 * &#064;Retention(RetentionPolicy.RUNTIME)  指定这个注解的生命周期为运行时，意味着可以在运行时通过反射读取到这个注解。
 * &#064;Target(ElementType.METHOD)  指定这个注解适用于方法级别。
 *
 * @author 13684
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MyLock {
    /**
     * 锁的名称。这是一个必填项，没有默认值。
     *
     * @return 返回锁的名称。
     */
    String name();

    /**
     * 等待获取锁的最大时间。默认值为1秒。
     *
     * @return 返回等待获取锁的最大时间。
     */
    long waitTime() default 1;

    /**
     * 锁的租约时间。默认值为-1，表示没有设置租约时间。
     *
     * @return 返回锁的租约时间。
     */
    long leaseTime() default -1;

    /**
     * 时间单位。默认单位为秒。
     *
     * @return 返回时间单位。
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 获取锁的类型，默认为可重入锁。
     *
     * @return MyLockType 锁的类型，这里限定为MyLockType枚举类中的一个值。
     * 默认返回MyLockType.RE_ENTRANT_LOCK，代表可重入锁。
     */
    MyLockType lockType() default MyLockType.RE_ENTRANT_LOCK;

    /**
     * 定义使用的锁策略的函数。
     * 这个函数用于指定一个默认的锁策略，如果没有显式地在使用的地方指定锁策略，系统将使用此默认策略。
     *
     * @return MyLockStrategy 返回锁策略的枚举值。这里指定了如果尝试获取锁失败后，应该采取的策略是跳过还是继续尝试，或者立即失败。
     * 默认返回的是SKIP_AFTER_RETRY_TIMEOUT策略，意味着在尝试获取锁失败后，会等待一段时间后再尝试，如果再次失败则跳过。
     */
    MyLockStrategy lockStrategy() default MyLockStrategy.SKIP_AFTER_RETRY_TIMEOUT;

}

