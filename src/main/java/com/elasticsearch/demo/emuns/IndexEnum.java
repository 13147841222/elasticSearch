package com.elasticsearch.demo.emuns;

/**
 * @author zhumingli
 * @create 2018-09-06 上午10:58
 * @desc
 **/
public enum IndexEnum {

    INDEX_NAME("xunwu"),

    INDEX_TYPE("house")
    ;

    private String value;

    IndexEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
