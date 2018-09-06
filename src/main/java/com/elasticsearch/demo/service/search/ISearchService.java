package com.elasticsearch.demo.service.search;

/**
 * @author zhumingli
 * @create 2018-09-06 上午10:49
 * @desc 检索接口
 **/
public interface ISearchService {

    /**
     * 索引目标房源
     * @param houseId
     */
    boolean index(Long houseId);

    /**
     * 移除房源索引
     * @param houseId
     */
    boolean remove(Long houseId);
}
