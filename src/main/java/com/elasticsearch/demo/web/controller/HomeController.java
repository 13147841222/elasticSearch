package com.elasticsearch.demo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author zhumingli
 * @create 2018-08-21 下午11:24
 * @desc
 **/
@Controller
public class HomeController {

    @GetMapping("/")
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
}
