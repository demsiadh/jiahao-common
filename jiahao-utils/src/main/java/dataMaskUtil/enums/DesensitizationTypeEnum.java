package dataMaskUtil.enums;

import base.BaseEnum;

/**
 * <big>数据脱敏策略的枚举类</big>
 * <p>定义了脱敏的类型</p>
 *
 * @author 13684
 * @data 2024/7/30 下午4:24
 */
public enum DesensitizationTypeEnum implements BaseEnum {
    //自定义
    MY_RULE(0, "自定义"),
    //用户id
    USER_ID(1, "用户id"),
    //中文名
    CHINESE_NAME(2, "中文名"),
    //身份证号
    ID_CARD(3, "身份证号"),
    //座机号
    FIXED_PHONE(4, "座机号"),
    //手机号
    MOBILE_PHONE(5, "手机号"),
    //地址
    ADDRESS(6, "地址"),
    //电子邮件
    EMAIL(7, "电子邮件"),
    //密码
    PASSWORD(8, "密码"),
    //中国大陆车牌，包含普通车辆、新能源车辆
    CAR_LICENSE(9, "中国大陆车牌"),
    //银行卡
    BANK_CARD(10, "银行卡");
    private final int value;
    private final String desc;
    private DesensitizationTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
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
