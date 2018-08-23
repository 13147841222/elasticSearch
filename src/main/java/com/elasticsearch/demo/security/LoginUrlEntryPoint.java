package com.elasticsearch.demo.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhumingli
 * @create 2018-08-23 下午6:45
 * @desc 基于角色的登陆点
 **/
@Configuration
@Slf4j
public class LoginUrlEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private PathMatcher pathMatcher = new AntPathMatcher();

    private final Map<String, String> authEntryPointMap ;

    /**
     * @param loginFormUrl URL where the login page can be found. Should either be
     *                     relative to the web-app context path (include a leading {@code /}) or an absolute
     *                     URL.
     */
    public LoginUrlEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
        authEntryPointMap = new HashMap<>();

        //普通用户登陆入口映射
        authEntryPointMap.put("/user/**","/user/login");
        //管理员登陆入口映射
        authEntryPointMap.put("/admin/**","/admin/login");
    }

    /**
     * 根据请求跳转到指定的页面，父类是默认使用loginFormUrl
     * @param request
     * @param response
     * @param exception
     * @return
     */
    @Override
    protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {

        //获取 uri
        String uri = request.getRequestURI().replace(request.getContextPath(),"");

        //遍历所有 登陆映射
        for(Map.Entry<String,String> authEntry : this.authEntryPointMap.entrySet()){
            //若果获取的uri与 配置的映射相匹配  就跳转相应的页面
            if( this.pathMatcher.match(authEntry.getKey(), uri) ){
                return authEntry.getValue();
            }

        }


        return super.determineUrlToUseForThisRequest(request, response, exception);
    }
}
