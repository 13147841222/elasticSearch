package com.elasticsearch.demo.web.dto;

import lombok.Data;

/**
 * @author zhumingli
 * @create 2018-09-12 下午9:56
 * @desc
 **/
@Data
public class HouseBucketDTO {

    /**
     * 聚合bucket的key
     */
    private String key;

    private Long count;

    public HouseBucketDTO(String key, Long count) {
        this.key = key;
        this.count = count;
    }
}
