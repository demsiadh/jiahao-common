package util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * <big>加密工具</big>
 * <p></p>
 *
 * @author 13684
 * @data 2024/6/3 下午4:33
 */
public class AESCoderUtil {
    private static final String AES_KEY = "jiahao";
    /**
     * 使用AES算法加密字符串。
     *
     * @param str 需要加密的字符串。
     * @return 加密后的字符串，以Base64编码表示。
     * @throws RuntimeException 如果加密过程中发生异常。
     */
    public String encryptWithAES(String str) {
        try {
            // 实例化AES加密算法
            Cipher cipher = Cipher.getInstance("AES");
            // 生成AES密钥
            SecretKey secretKey = generateAESKey(AES_KEY);
            // 初始化加密模式
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            // 执行加密操作
            byte[] bytes = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));
            // 将加密后的字节序列转换为Base64编码的字符串
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            // 如果加密过程中发生异常，抛出运行时异常
            throw new RuntimeException(e);
        }
    }


    /**
     * 使用AES算法解密字符串。
     *
     * @param str 待解密的字符串，先经过Base64编码。
     * @return 解密后的原始字符串。
     * @throws RuntimeException 如果解密过程中发生异常。
     */
    public String decryptWithAES(String str) {
        try {
            // 实例化AES加密器
            Cipher cipher = Cipher.getInstance("AES");
            // 生成AES密钥
            SecretKey secretKey = generateAESKey(AES_KEY);
            // 初始化加密器为解密模式
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            // 解密Base64编码的字符串
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(str));
            // 返回解密后的字符串
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 如果发生异常，抛出运行时异常
            throw new RuntimeException(e);
        }
    }


    /**
     * 根据密码生成AES加密所需的密钥。
     *
     * 使用SHA-256算法对密码进行哈希处理，以增强密钥的安全性。
     * AES密钥长度被初始化为128位，符合AES加密算法的标准要求。
     *
     * @param password 用户输入的密码，用于生成密钥。
     * @return 生成的AES密钥，用于加密和解密操作。
     * @throws RuntimeException 如果加密算法实例化或初始化失败，则抛出运行时异常。
     */
    private SecretKey generateAESKey(String password) {
        try {
            // 实例化AES密钥生成器
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            // 初始化AES密钥生成器，设置密钥长度为128位
            keyGenerator.init(128);

            // 实例化SHA-256消息摘要算法
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            // 使用UTF-8编码将预定义的字符串转换为字节数组，并对其进行SHA-256哈希处理
            byte[] digest = messageDigest.digest(AES_KEY.getBytes(StandardCharsets.UTF_8));

            // 基于哈希处理后的字节数组，创建AES密钥对象
            return new SecretKeySpec(digest, "AES");
        } catch (Exception e) {
            // 如果在加密算法的实例化或初始化过程中出现异常，则抛出运行时异常
            throw new RuntimeException(e);
        }
    }

}
