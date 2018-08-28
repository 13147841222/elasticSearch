package com.elasticsearch.demo.web.controller.admin;

import com.elasticsearch.demo.base.ApiResponse;
import com.elasticsearch.demo.config.FileuploadConfig;
import com.elasticsearch.demo.emun.ApiResponseEnum;
import com.elasticsearch.demo.service.IQiNiuService;
import com.elasticsearch.demo.web.dto.QiNiuPutRet;
import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
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
import java.io.InputStream;

/**
 * @author zhumingli
 * @create 2018-08-22 下午10:23
 * @desc 换台管理
 **/
@Controller
public class AdminController {

    @Autowired
    private IQiNiuService iQiNiuService;

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

    @Autowired
    private Gson gson;

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

        try {
            InputStream inputStream = file.getInputStream();
            Response response = iQiNiuService.uploadFile(inputStream);
            if (response.isOK()){
                QiNiuPutRet ret = gson.fromJson(response.bodyString(), QiNiuPutRet.class);
                return ApiResponse.ofSuccess(ret);
            }else {
                return ApiResponse.ofMessage(response.statusCode,response.getInfo());
            }
        } catch (QiniuException e){
            e.printStackTrace();
            Response response = e.response;
            return ApiResponse.ofMessage(response.statusCode,response.getInfo());
        }
        catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.ofStatus(ApiResponseEnum.INTERNAL_SERVER_ERROR);
        }
        /*
        //文件存储路径
        File target = new File(fileuploadConfig.getPath() + fileName);

        try {
            file.transferTo(target);
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.ofStatus(ApiResponseEnum.INTERNAL_SERVER_ERROR);
        }
        return ApiResponse.ofSuccess(null);
        */

    }

}
