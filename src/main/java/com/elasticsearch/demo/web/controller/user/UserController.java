package com.elasticsearch.demo.web.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author zhumingli
 * @create 2018-08-23 下午6:42
 * @desc
 **/
@Controller
public class UserController {

    @GetMapping("/user/login")
    public String loginPage(){
        return "user/login";
    }

    @GetMapping("/user/center")
    public String centerPage(){
        return "user/center";
    }


}
