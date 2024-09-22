package coupon;

import cn.hutool.core.lang.RegexPool;

/**
 * @author 13684
 */
public interface RegexConstants extends RegexPool {
    /**
     * 兑换码模板
     */
    String COUPON_CODE_PATTERN = "^[23456789ABCDEFGHJKLMNPQRSTUVWXYZ]{8,10}$";
}
