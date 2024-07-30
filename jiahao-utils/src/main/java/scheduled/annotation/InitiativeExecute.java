package scheduled.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <big>允许主动调用的标识</big>
 * <p>不做任何处理，仅仅用来标志，无此标志则不可主动调用</p>
 *
 * @author 13684
 * @data 2024/7/15 上午9:53
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface InitiativeExecute {
}
