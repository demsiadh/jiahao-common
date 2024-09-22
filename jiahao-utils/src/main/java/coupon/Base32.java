package coupon;

import cn.hutool.core.text.StrBuilder;

/**
 * 将整数转为base32字符的工具，因为是32进制，所以每5个bit位转一次
 *
 * @author 13684
 */
public class Base32 {
    private final static String BASE_CHARS = "6CSB7H8DAKXZF3N95RTMVUQG2YE4JWPL";

    /**
     * 将50位的数字转化为字符
     *
     * @param raw 50位的数字
     * @return 字符
     */
    public static String encode(long raw) {
        // 结果字符串
        StrBuilder sb = new StrBuilder();
        // 只有没有遍历完
        while (raw != 0) {
            // 获取当前最低五位的整数
            int i = (int) (raw & 0b11111);
            // 获取字符
            sb.append(BASE_CHARS.charAt(i));
            // 右移五位
            raw = raw >>> 5;
        }
        // 返回结果
        return sb.toString();
    }

    /**
     * 将字符转化为50位的数字
     *
     * @param code 字符
     * @return 数字
     */
    public static long decode(String code) {
        // 初始化结果
        long r = 0;
        // 兑换码字符数组
        char[] chars = code.toCharArray();
        // 遍历字符数组
        for (int i = chars.length - 1; i >= 0; i--) {
            // 获取当前字符对应的数字
            long n = BASE_CHARS.indexOf(chars[i]);
            // 将n左移 5 * i 位拼接到结果中去
            r = r | (n << (5 * i));
        }
        // 返回解析结果
        return r;
    }
}
