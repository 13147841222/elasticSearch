package com.elasticsearch.demo.emuns;

import lombok.Data;

/**
 * @author zhumingli
 * @create 2018-08-28 下午10:01
 * @desc
 **/
public enum LevelEnum {
    CITY("city"),
    REGION("region");

    private String value;

    LevelEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static LevelEnum of(String value){
        for (LevelEnum level : LevelEnum.values()) {
            if(level.getValue().equals(value)){
                return level;
            }
        }

        throw new IllegalArgumentException();
    }
}
