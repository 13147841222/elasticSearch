package com.elasticsearch.demo.web.controller.user;

import com.elasticsearch.demo.base.ApiResponse;
import com.elasticsearch.demo.base.LoginUserUtil;
import com.elasticsearch.demo.emuns.ApiResponseEnum;

import com.elasticsearch.demo.emuns.HouseSubscribeStatusEnum;
import com.elasticsearch.demo.service.IHouseService;
import com.elasticsearch.demo.service.IUserService;
import com.elasticsearch.demo.service.ServiceMultiResult;
import com.elasticsearch.demo.service.ServiceResult;
import com.elasticsearch.demo.web.dto.HouseDTO;
import com.elasticsearch.demo.web.dto.HouseSubscribeDTO;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * @author zhumingli
 * @create 2018-08-23 下午6:42
 * @desc
 **/
@Controller
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IHouseService houseService;

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

    @PostMapping(value = "api/user/house/subscribe")
    @ResponseBody
    public ApiResponse subscribeHouse(@RequestParam(value = "house_id") Long houseId ){
        ServiceResult result = houseService.addSubscribeOrder(houseId);
        if (result.isSuccess()){
            return ApiResponse.ofSuccess("");
        }
        return ApiResponse.ofMessage(HttpStatus.SC_BAD_REQUEST, result.getMessage());
    }

    @GetMapping(value = "api/user/house/subscribe/list")
    @ResponseBody
    public ApiResponse subscribeList(@RequestParam(value = "start", defaultValue = "0") int start,
                                     @RequestParam(value = "size", defaultValue = "0") int size,
                                     @RequestParam(value = "status", defaultValue = "0") int status){
        ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> serviceMultiResult = houseService.querySubscribeList(HouseSubscribeStatusEnum.of(status),start,size);

        if (serviceMultiResult.getResultSize() == 0) {
            return ApiResponse.ofSuccess(serviceMultiResult.getResult());
        }
        ApiResponse response = ApiResponse.ofSuccess(serviceMultiResult.getResult());
        response.setMore(serviceMultiResult.getTotal() > (size + start));
        return response;
    }

    @PostMapping(value = "api/user/house/subscribe/date")
    @ResponseBody
    public ApiResponse subscribeDate(@RequestParam(value = "houseId") Long houseId,
                                     @RequestParam(value = "orderTime")@DateTimeFormat(pattern = "yyyy-MM-dd") Date orderTime,
                                     @RequestParam(value = "desc", required = false) String desc,
                                     @RequestParam(value = "telephone") String telephone
                                     ){
        if (orderTime == null) {
            return ApiResponse.ofMessage(HttpStatus.SC_BAD_REQUEST, "请选择预约时间");
        }

        if (!LoginUserUtil.checkTelephone(telephone)){
            return ApiResponse.ofMessage(HttpStatus.SC_BAD_REQUEST, "电话号码不正确");
        }

        ServiceResult result = houseService.subscribe(houseId,orderTime,telephone,desc);
        if (result.isSuccess()){
            return ApiResponse.ofSuccess(ApiResponseEnum.SUCCESS);
        }

        return ApiResponse.ofMessage(HttpStatus.SC_BAD_REQUEST,result.getMessage());
    }

    @DeleteMapping(value = "api/user/house/subscribe")
    @ResponseBody
    public ApiResponse cancelSubscribe(@RequestParam(value = "houseId") Long houseId){

        ServiceResult result = houseService.cancelSubscribe(houseId);

        if (result.isSuccess()) {
            return ApiResponse.ofSuccess(ApiResponseEnum.SUCCESS);
        }

        return ApiResponse.ofMessage(HttpStatus.SC_BAD_REQUEST,result.getMessage());
    }

}
