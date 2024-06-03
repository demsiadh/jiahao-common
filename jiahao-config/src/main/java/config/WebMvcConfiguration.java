package config;

import intercept.AuthIntercept;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.annotation.Resource;

/**
 * <big>web配置类，注册web层相关组件</big>
 * <p></p>
 *
 * @author 13684
 * @data 2024/6/3 上午11:01
 */
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {
    @Resource
    private AuthIntercept authIntercept;

    @Override
    protected void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        // 内部接口添加认证过滤器
        registry.addInterceptor(authIntercept).addPathPatterns("/schedule/**");
    }
}
