package com.elasticsearch.demo.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.Column;

/**
 * @author zhumingli
 * @create 2018-08-28 下午10:26
 * @desc
 **/
@Data
public class SupportAddressDTO {

    private Long id;

    @JsonProperty(value = "belong_to")
    private String belongTO;

    /**
     *
     */
    @JsonProperty(value = "en_name")
    private String enName;

    /**
     *
     */
    @JsonProperty(value = "cn_name")
    private String cnName;

    /**
     * 行政级别
     */
    private String level;


    private double baiduMapLongtitue;


    private double baiduMapLatitude;
}
