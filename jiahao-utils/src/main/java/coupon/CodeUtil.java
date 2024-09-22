package coupon;

import cn.hutool.core.util.StrUtil;

import java.util.Random;

/**
 * <big>生成/解析兑换码的工具</big>
 *
 * @author 13684
 * @date 2024/9/21
 */
public class CodeUtil {
    /**
     * 进行异或运算的质数，让生成更加随机
     */
    private static final long[] XOR_TABLE = {
            61261925471L, 61261925523L, 58169127203L, 64169927267L,
            64169927199L, 61261925629L, 58169127227L, 64169927363L,
            59169127063L, 64169927359L, 58169127291L, 61261925739L,
            59169127133L, 55139281911L, 56169127077L, 59169127167L
    };

    /**
     * 序列号加权运算的秘钥表
     */
    private final static int[][] PRIME_TABLE = {
            {23, 59, 241, 61, 607, 67, 977, 1217, 1289, 1601},
            {79, 83, 107, 439, 313, 619, 911, 1049, 1237, 1368},
            {173, 211, 499, 673, 823, 941, 1039, 1213, 1429, 1259},
            {31, 293, 311, 349, 431, 577, 757, 883, 1009, 1657},
            {353, 23, 367, 499, 599, 661, 719, 929, 1301, 1511},
            {103, 179, 353, 467, 577, 691, 811, 947, 1153, 1453},
            {213, 439, 257, 313, 571, 619, 743, 829, 983, 1103},
            {31, 151, 241, 349, 607, 677, 769, 823, 967, 1049},
            {61, 83, 109, 137, 151, 521, 701, 827, 1123, 1253},
            {23, 61, 199, 223, 479, 647, 739, 811, 947, 1019},
            {31, 109, 311, 467, 613, 743, 821, 881, 1031, 1171},
            {41, 173, 367, 401, 569, 683, 761, 883, 1009, 1181},
            {127, 283, 467, 577, 661, 773, 881, 967, 1097, 1289},
            {59, 137, 257, 347, 439, 547, 641, 839, 977, 1009},
            {61, 199, 313, 421, 613, 739, 827, 941, 1087, 1307},
            {19, 127, 241, 353, 499, 607, 811, 919, 1031, 1301}
    };

    /**
     * 4位掩码
     */
    private static final long FOUR_BIT_MASK = 0xFL;
    /**
     * 14位掩码
     */
    private static final long FOURTEEN_BIT_MASK = 0x3FFFL;
    /**
     * 32位掩码
     */
    private static final long THIRTY_TWO_BIT_MASK = 0xFFFFFFFFL;
    /**
     * 36位掩码
     */
    private static final long THIRTY_SIX_BIT_MASK = 0xFFFFFFFFFL;
    /**
     * 原始数据所占的位数(自增序列+新鲜值=36位)
     */
    private static final int SOURCE_NUM_BITS = 36;

    /**
     * 自增序列占的位数
     */
    private static final int SERIAL_NUM_BITS = 32;
    /**
     * 验证码所占的位数
     */
    private static final int CHECK_CODE_BITS = 14;


    public static void main(String[] args) {
        System.out.println(generateCode(1, 1));
        System.out.println(parseCode(generateCode(1, 1)));
    }

    /**
     * 根据自增序列和业务id生成兑换码
     * @param serialNum 自增序列
     * @param fresh 业务id
     * @return 兑换码
     */
    public static String generateCode(long serialNum, long fresh) {
        // 1.根据业务id取后四位作为新鲜值
        fresh = fresh & FOUR_BIT_MASK;
        // 2.拼接新鲜值和自增序列
        long payload = fresh << SERIAL_NUM_BITS | (serialNum & THIRTY_TWO_BIT_MASK);
        // 3.通过新鲜值和当前的拼接结果计算出验证码
        long checkCode = calcCheckCode(payload, (int) fresh);
        // 4.将36位数字与较大的质数进行异或运算，混淆数据
        payload ^= XOR_TABLE[(int) (checkCode & FOUR_BIT_MASK)];
        // 5.拼接36位数字和验证码
        long code = checkCode << SOURCE_NUM_BITS | payload;
        return Base32.encode(code);
    }

    /**
     * 根据验证码获取自增序列
     * @param code 验证码
     * @return 自增序列
     */
    private static long parseCode(String code) {
        if (StrUtil.isEmpty(code) || !code.matches(RegexConstants.COUPON_CODE_PATTERN)) {
            System.out.println("兑换码格式错误！");
        }
        // 1.解析出验证码
        long decode = Base32.decode(code);
        // 2.取出校验码(前面14位)
        long checkCode = decode >> SOURCE_NUM_BITS & FOURTEEN_BIT_MASK;
        // 3.取出原始数据(后面36位)
        long payload = decode & THIRTY_SIX_BIT_MASK;
        // 4.通过异或运算获取原始数据
        payload ^= XOR_TABLE[(int) (checkCode & FOUR_BIT_MASK)];
        // 5.获取新鲜值
        long fresh = payload >> SERIAL_NUM_BITS & FOUR_BIT_MASK;
        // 6.校验验证码
        if (calcCheckCode(payload, (int) fresh) != checkCode) {
            System.out.println("兑换码被篡改！");
        }
        return payload & THIRTY_TWO_BIT_MASK;
    }

    /**
     * 根据新鲜值和自增序列拼接结果和新鲜值计算验证码
     * @param payload 36位数字
     * @param fresh 新鲜值
     * @return 14位验证码
     */
    private static long calcCheckCode(long payload, int fresh) {
        // 根据新鲜值选取密钥
        int[] secretKey = PRIME_TABLE[fresh];
        // 生成校验码，每四位进行加权求和，取后14位为结果
        long sum = 0;
        int index = 0;
        while (payload > 0) {
            sum += (payload & FOUR_BIT_MASK) * secretKey[index++];
            payload >>>= 4;
        }
        return sum & THIRTY_TWO_BIT_MASK;
    }

}
