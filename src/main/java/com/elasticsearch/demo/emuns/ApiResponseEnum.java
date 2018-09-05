package com.elasticsearch.demo.emuns;

/**
 * @author zhumingli
 * @create 2018-08-22 下午7:20
 * @desc
 **/
public enum  ApiResponseEnum {

    /**
     * 成功
     */
    SUCCESS(200,"OK"),

    /**
     * 请求失败
     */
    BAD_REQUEST(400,"Bad Request"),

    INTERNAL_SERVER_ERROR(500,"Unknown Internal Error"),

    NOT_VALID_PARAM(40005,"Not Valid Params"),

    NOT_SUPPORTED_OPERATION(40006,"Not supported Operation"),

    NOT_LOGIN(50000,"Not Login"),

    NOT_FOUND(404,"Not Found")
    ;


    private int code;

    private String message;

    ApiResponseEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
