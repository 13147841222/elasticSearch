package com.elasticsearch.demo.web.controller.user;

import com.elasticsearch.demo.base.ApiResponse;
import com.elasticsearch.demo.base.LoginUserUtil;
import com.elasticsearch.demo.emuns.ApiResponseEnum;

import com.elasticsearch.demo.service.IUserService;
import com.elasticsearch.demo.service.ServiceResult;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author zhumingli
 * @create 2018-08-23 下午6:42
 * @desc
 **/
@Controller
public class UserController {

    @Autowired
    private IUserService userService;

    @GetMapping("/user/login")
    public String loginPage(){
        return "user/login";
    }

    @GetMapping("/user/center")
    public String centerPage(){
        return "user/center";
    }

    @PostMapping(value = "api/user/info")
    @ResponseBody
    public ApiResponse updateUserInfo(@RequestParam(value = "profile") String profile, @RequestParam(value = "value") String value){
        if (value.isEmpty()){
            return ApiResponse.ofStatus(ApiResponseEnum.BAD_REQUEST);
        }

        if ("email".equals(profile) && !LoginUserUtil.checkEmail(value)) {
            return ApiResponse.ofMessage(HttpStatus.SC_BAD_REQUEST,"不支持的邮箱格式");
        }

        ServiceResult result = userService.modifyUserProfile(profile, value);
        if (result.isSuccess()) {
            return ApiResponse.ofSuccess(result);
        }
        return ApiResponse.ofMessage(HttpStatus.SC_BAD_REQUEST, result.getMessage());
    }

}
