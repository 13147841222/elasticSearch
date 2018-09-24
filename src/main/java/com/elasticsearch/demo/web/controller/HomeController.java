package com.elasticsearch.demo.web.controller;

import com.elasticsearch.demo.base.ApiResponse;
import com.elasticsearch.demo.base.LoginUserUtil;

import com.elasticsearch.demo.service.ISmsService;
import com.elasticsearch.demo.service.ServiceResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author zhumingli
 * @create 2018-08-21 下午11:24
 * @desc
 **/
@Controller
public class HomeController {

    @Autowired
    private ISmsService smsService;

    /**
     * {"/","/index"} 表示兼容
     * @param model
     * @return
     */
    @GetMapping({"/","/index"})
    public String index(Model model){

        model.addAttribute("name","zhuml");

        return "index";
    }

    @GetMapping("/404")
    public String notFoundPage(Model model){
        return "404";
    }

    @GetMapping("/403")
    public String accessError(Model model){
        return "403";
    }

    @GetMapping("/500")
    public String internalError(Model model){
        return "500";
    }

    @GetMapping("/logout/page")
    public String logoutPage(Model model){
        return "logout";
    }

    @GetMapping("sms/code")
    @ResponseBody
    public ApiResponse smsCode(@RequestParam("telephone") String telephone ){
        if (LoginUserUtil.checkTelephone(telephone)) {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), "请输入正确的手机号");
        }

        ServiceResult<String> stringServiceResult = smsService.sendSms(telephone);

        if (stringServiceResult.isSuccess()){
            return ApiResponse.ofSuccess(stringServiceResult.getResult());
        } else {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), stringServiceResult.getResult());
        }



    }
}
