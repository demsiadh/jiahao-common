package dataMaskUtil.enums;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <big>脱敏的注解，用于加载需要脱敏的字段</big>
 * <p></p>
 *
 * @author 13684
 * @data 2024/7/31 上午11:14
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@JacksonAnnotationsInside
// 自定义序列化注解
@JsonSerialize(using = DesensitizationSerialize.class)
public @interface Desensitization {
    /**
     * <big>脱敏规则</big>
     * <p></p>
     *
     * @return 脱敏规则
     */
    DesensitizationTypeEnum type() default DesensitizationTypeEnum.MY_RULE;
    /**
     * 脱敏开始位置（包含）给自定义规则用的
     */
    int startInclude() default 0;

    /**
     * 脱敏结束位置（不包含）同上
     */
    int endExclude() default 0;
}
