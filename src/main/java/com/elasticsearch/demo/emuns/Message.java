package com.elasticsearch.demo.emuns;

/**
 * @author zhumingli
 * @create 2018-08-29 下午10:41
 * @desc
 **/
public enum Message {
    NOT_FOUND("Not Found Resource!"),
    NOT_LOGIN("User not login!"),

    ;

    private String value;

    Message(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}