package com.elasticsearch.demo.service.search;

import com.elasticsearch.demo.service.ServiceMultiResult;
import com.elasticsearch.demo.service.ServiceResult;
import com.elasticsearch.demo.web.dto.HouseBucketDTO;
import com.elasticsearch.demo.web.form.RentSearch;

import java.util.List;

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
    void index(Long houseId);

    /**
     * 移除房源索引
     * @param houseId
     */
    void remove(Long houseId);

    /**
     * 查询房源接口
     * @param rentSearch
     * @return
     */
    ServiceMultiResult<Long> query(RentSearch rentSearch);

    /**
     * 自动补全
     * @param prefix
     * @return
     */
    ServiceResult<List<String>> suggest(String prefix);


    /**
     * 聚合特定小区房间数
     * @param cityEnName
     * @param regionEnName
     * @param district
     * @return
     */
    ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName, String district);

    /**
     *
     * @param cityEnName
     * @return
     */
    ServiceMultiResult<HouseBucketDTO> mapAggregate(String cityEnName);
}
