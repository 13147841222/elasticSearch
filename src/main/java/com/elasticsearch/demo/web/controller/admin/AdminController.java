package com.elasticsearch.demo.web.controller.admin;

import com.elasticsearch.demo.base.ApiResponse;
import com.elasticsearch.demo.config.FileuploadConfig;
import com.elasticsearch.demo.emun.ApiResponseEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author zhumingli
 * @create 2018-08-22 下午10:23
 * @desc 换台管理
 **/
@Controller
public class AdminController {

    @Autowired
    private FileuploadConfig fileuploadConfig;

    @GetMapping("/admin/center")
    public String adminCenterPage(){
        return "admin/center";
    }

    @GetMapping("/admin/welcome")
    public String welcomePage(){
        return "admin/welcome";
    }

    @GetMapping("/admin/login")
    public String adminLoginPage(){
        return "admin/login";
    }


    /**
     * 添加房源
     * @return
     */
    @GetMapping("admin/add/house")
    public String addHousePage() {
        return "admin/house-add";
    }

    /**
     * 房源列表页
     * @return
     */
    @GetMapping("admin/house/list")
    public String houseListPage() {
        return "admin/house-list";
    }


    @PostMapping(value = "admin/upload/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ApiResponse uploadPhoto(@RequestParam("file")MultipartFile file){
        if(file.isEmpty()){
            return ApiResponse.ofStatus(ApiResponseEnum.NOT_VALID_PARAM);
        }
        //获取文件名
        String fileName = file.getOriginalFilename();
        //文件存储路径
        File target = new File(fileuploadConfig.getPath() + fileName);

        try {
            file.transferTo(target);
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.ofStatus(ApiResponseEnum.INTERNAL_SERVER_ERROR);
        }

        return ApiResponse.ofSuccess(null);

    }

}
