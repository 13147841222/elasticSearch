package com.elasticsearch.demo.base;

import com.elasticsearch.demo.emuns.ApiResponseEnum;
import lombok.Data;

/**
 * @author zhumingli
 * @create 2018-08-22 下午7:14
 * @desc APi 格式封装
 **/
@Data
public class ApiResponse {

    /**
     * 请求响应码
     */
    private Integer code;

    /**
     * 请求响应描述
     */
    private String message;

    /**
     * 请求响应数据
     */
    private Object data;

    /**
     * 是否有更多信息
     */
    private boolean more;

    public ApiResponse(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ApiResponse() {
        this.code = ApiResponseEnum.SUCCESS.getCode();
        this.message = ApiResponseEnum.SUCCESS.getMessage();
    }


    public static ApiResponse ofMessage(Integer code, String message){
        return new ApiResponse(code, message, null);
    }

    public static ApiResponse ofSuccess(Object data){
        return new ApiResponse(ApiResponseEnum.SUCCESS.getCode(), ApiResponseEnum.SUCCESS.getMessage(), data);
    }

    public static ApiResponse ofStatus(ApiResponseEnum apiResponseEnum){
        return new ApiResponse(apiResponseEnum.getCode(),apiResponseEnum.getMessage(),null);
    }
}
