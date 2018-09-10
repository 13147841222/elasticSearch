package com.elasticsearch.demo.service.search;

import lombok.Data;

/**
 * @author zhumingli
 * @create 2018-09-07 下午9:35
 * @desc
 **/
@Data
public class HouseIndexMessage {

    public static final String INDEX = "index";

    public static final String REMOVE ="remove";

    public static final int MAX_RETRY = 3 ;
    private Long houseId;
    private String operation;
    private int retry = 0;

    public HouseIndexMessage(Long houseId, String operation, int retry) {
        this.houseId = houseId;
        this.operation = operation;
        this.retry = retry;
    }

    /**
     * 默认构造器  防止json序列化失败
     */
    public HouseIndexMessage() {

    }
}
