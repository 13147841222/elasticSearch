package com.elasticsearch.demo.security;


import com.elasticsearch.demo.base.LoginUserUtil;
import com.elasticsearch.demo.entity.User;
import com.elasticsearch.demo.service.ISmsService;
import com.elasticsearch.demo.service.IUserService;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @author zhumingli
 * @create 2018-09-20 下午10:26
 * @desc
 **/
public class AuthFilter extends UsernamePasswordAuthenticationFilter {

    @Autowired
    private IUserService userService;

    @Autowired
    private ISmsService smsService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String name = obtainUsername(request);
        if (!Strings.isNullOrEmpty(name)){
            request.setAttribute("username",name);
            return super.attemptAuthentication(request, response);
        }

        String telephone = request.getParameter("telephone");
        if (Strings.isNullOrEmpty(telephone) || LoginUserUtil.checkTelephone(telephone)){
            throw new BadCredentialsException("Wrong telephone number for telephone : " + telephone);
        }

        User user = userService.findUserByTelephone(telephone);

        String inputCode = request.getParameter("smsCode");

        String sessionCode = smsService.getSmsCode(inputCode).getResult();

        if (Objects.equals(inputCode, sessionCode)){

            if (user == null) {
                user = userService.addUserByPhone(telephone);
            }

            return new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());
        }

        throw new BadCredentialsException("smsCode Error");
    }
}
