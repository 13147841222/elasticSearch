package com.elasticsearch.demo.base;

import com.elasticsearch.demo.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author zhumingli
 * @create 2018-08-29 下午11:34
 * @desc
 **/
public class LoginUserUtil {

    public static User load(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(principal != null && principal instanceof User){
            return (User)principal;
        }
        return null;
    }

    public static Long getLoginUserId(){
        User user = load();
        if(user == null){
            return -1L;
        }
        return user.getId();
    }
}
