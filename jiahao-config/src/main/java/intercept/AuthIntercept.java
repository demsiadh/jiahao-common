package intercept;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import util.AuthUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <big>认证拦截器，用于内部服务调用接口</big>
 * <p></p>
 *
 * @author 13684
 * @data 2024/6/3 上午10:56
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthIntercept implements HandlerInterceptor {
    private final AuthUtil authUtil;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ts = request.getHeader("ts");
        // 获取时间戳
        Long timeStamp = StrUtil.isNotBlank(ts)? Long.valueOf(ts) : null;
        String appId = request.getHeader("appId");
        String accessId = request.getHeader("accessId");
        String accessKey = request.getHeader("accessKey");
        boolean res = authUtil.checkHeaderInfo(timeStamp, appId, accessId, accessKey);
        if (!res) {
            log.error("没有权限! accessId: {}, appId: {}", accessId, appId);
        }
        return res;
    }
}
