package com.elasticsearch.demo.base;

import com.elasticsearch.demo.emuns.ApiResponseEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhumingli
 * @create 2018-08-30 下午11:25
 * @desc
 **/
@Data
@EqualsAndHashCode(callSuper = false)
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
