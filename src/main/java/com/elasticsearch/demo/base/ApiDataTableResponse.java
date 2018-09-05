package com.elasticsearch.demo.base;

import com.elasticsearch.demo.emuns.ApiResponseEnum;
import lombok.Data;

/**
 * @author zhumingli
 * @create 2018-08-30 下午11:25
 * @desc
 **/
@Data
public class ApiDataTableResponse extends ApiResponse{

    private int draw;

    private Long recordsTotal;

    private Long recordsFiltered;

    public ApiDataTableResponse(ApiResponseEnum apiResponseEnum){
        this(apiResponseEnum.getCode(),apiResponseEnum.getMessage(),null);
    }
    public ApiDataTableResponse(Integer code, String message, Object data) {
        super(code, message, data);
    }


}
