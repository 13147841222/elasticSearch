package com.elasticsearch.demo.base;

import lombok.Data;

/**
 * @author zhumingli
 * @create 2018-09-11 下午8:51
 * @desc
 **/
@Data
public class HouseSuggest {

    private String input;

    private int weight = 10;
}
