package com.elasticsearch.demo.emuns;

import lombok.Data;

/**
 * @author zhumingli
 * @create 2018-09-02 下午10:56
 * @desc
 **/
public enum  HouseStatusEnum {

    NOT_AUDITED(0),
    PASSES(1),
    RENTED(2),
    DELETE(3);

    private Integer code;

    HouseStatusEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
