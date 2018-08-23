package com.elasticsearch.demo.security;

import com.elasticsearch.demo.emun.ApiResponseEnum;
import com.elasticsearch.demo.entity.User;
import com.elasticsearch.demo.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * @author zhumingli
 * @create 2018-08-22 下午11:30
 * @desc 自定义认证实现
 **/
@Configuration
@Slf4j
public class AuthProvider implements AuthenticationProvider {

    @Autowired
    private IUserService iUserService;

    private final Md5PasswordEncoder md5PasswordEncoder = new Md5PasswordEncoder();
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("AuthProvider===>authenticate:start");
        String userName = authentication.getName();
        String inputPassword = (String)authentication.getCredentials();
        User user = iUserService.findUserByName(userName);
        if(user == null){
            log.info("AuthProvider===>authenticate:end case AuthenticationCredentialsNotFoundException");
            throw new AuthenticationCredentialsNotFoundException("authError");
        }

        if(this.md5PasswordEncoder.isPasswordValid(user.getPassword(),inputPassword,user.getId())){
            log.info("AuthProvider===>authenticate:end");
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        }
        log.info("AuthProvider===>authenticate:end case BadCredentialsException");
        throw new BadCredentialsException("authError");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }
}
