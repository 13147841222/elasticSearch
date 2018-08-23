package com.elasticsearch.demo.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhumingli
 * @create 2018-08-23 下午10:00
 * @desc 登陆认证失败处理器
 **/

public class LoginAuthFailHandler extends SimpleUrlAuthenticationFailureHandler {
    private final LoginUrlEntryPoint loginUrlEntryPoint;


    public LoginAuthFailHandler(LoginUrlEntryPoint loginUrlEntryPoint) {
        this.loginUrlEntryPoint = loginUrlEntryPoint;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        //获取 登陆的 url
        String targetUrl = this.loginUrlEntryPoint.determineUrlToUseForThisRequest(request, response, exception);
        //处理url 连接异常信息
        targetUrl += "?" +exception.getMessage();

        super.setDefaultFailureUrl(targetUrl);
        super.onAuthenticationFailure(request,response,exception);

    }
}
