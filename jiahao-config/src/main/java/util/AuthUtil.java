package util;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * <big>身份认证工具类</big>
 * <p>用于鉴定权限</p>
 *
 * @author 13684
 * @data 2024/6/3 下午4:14
 */
@Component
@RequiredArgsConstructor
public class AuthUtil {
    private final StringRedisTemplate redisTemplate;
    private final String KEY = "jiahao:auth:{}:{}";
    private final String SIGN_AUTH_STR = "ts={}&appId={}&accessId={}&accessKey={}";
    private final AESCoderUtil aesCoderUtil;

    /**
     * 验证请求头信息
     * @param timeStamp 时间戳
     * @param appId     请求的应用ID
     * @param accessId  请求的用户ID
     * @param accessKey 请求的用户密钥
     * @return
     */
    public boolean checkHeaderInfo(Long timeStamp, String appId, String accessId, String accessKey) {
        if (ObjectUtil.hasNull(timeStamp, appId, accessId, accessKey)) {
            return false;
        }

        // 添加Redis来防止重复请求 如果已有请求则拒绝请求(这里可以添加个提示不要重复请求)
        String s = redisTemplate.opsForValue().get(StrUtil.format(KEY, appId, accessId));
        if (StrUtil.isNotBlank(s)) {
            return false;
        }

        // 如果请求超过5分钟，则拒绝请求
        if (System.currentTimeMillis() - timeStamp > 5 * 60 * 1000) {
            return false;
        }

        // 验签(假设这里有个用户中心，则调用用户中心请求，并将字符串加密)
        return isAvailable(aesCoderUtil.encryptWithAES(StrUtil.format(SIGN_AUTH_STR, timeStamp, appId, accessId, accessKey)));
    }

    private boolean isAvailable(String str) {
        return true;
    }
}
