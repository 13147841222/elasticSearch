package com.elasticsearch.demo.web.dto;

import lombok.Data;

/**
 * @author zhumingli
 * @create 2018-08-28 下午11:15
 * @desc
 **/
@Data
public class SubwayStationDTO {

    private Long id;
    private Long subwayId;
    private String name;
}
