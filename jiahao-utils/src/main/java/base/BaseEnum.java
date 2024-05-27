package base;

/**
 * <big>自定义枚举基础类接口</big>
 *
 * @author 13684
 * @date 2024/4/3
 */
public interface BaseEnum {
    /**
     * 获取一个值。
     *
     * @return 返回方法的数值。
     */
    int getValue();

    /**
     * 获取描述信息。
     *
     * @return 返回方法的描述字符串。
     */
    String getDesc();

    /**
     * 检查传入的整数值是否与当前对象的值相等。
     *
     * @param value 要比较的整数值。
     * @return 如果相等返回true，否则返回false。当传入的值为null时，总是返回false。
     */
    default boolean equalsValue(Integer value) {
        // 检查传入的value是否为null
        if (value == null) {
            return false;
        }
        // 比较当前对象的值和传入的值是否相等
        return getValue() == value;
    }

}
