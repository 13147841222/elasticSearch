package com.elasticsearch.demo.service;

import com.elasticsearch.demo.web.dto.HouseDTO;
import com.elasticsearch.demo.web.form.DatatableSearch;
import com.elasticsearch.demo.web.form.HouseForm;
import com.elasticsearch.demo.web.form.RentSearch;

/**
 * @author zhumingli
 * @create 2018-08-29 下午10:40
 * @desc 房屋管理服务接口
 **/
public interface IHouseService {

    /**
     * @param houseForm
     * @return
     */
    ServiceResult<HouseDTO> save(HouseForm houseForm);

    /**
     * @param datatableSearch
     * @return
     */
    ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch datatableSearch);

    /**
     * @param id
     * @return
     */
    ServiceResult<HouseDTO> findCompleteOne(Long id);


    /**
     * 更新
     * @param houseForm
     * @return
     */
    ServiceResult update(HouseForm houseForm);


    /**
     * 移除照片
     * @param id
     * @return
     */
    ServiceResult removePhoto(Long id);


    /**
     * 更新封面
     * @param coverId
     * @param targetId
     * @return
     */
    ServiceResult updateCover(Long coverId, Long targetId);

    /**
     * 添加标签
     * @param houseId
     * @param tag
     * @return
     */
    ServiceResult addTag(Long houseId, String tag);


    /**
     * 更新房源状态
     * @param houseId
     * @param status
     * @return
     */
    ServiceResult updateStatus(Long houseId, int status);

    /**
     * 查询房源信息集
     * @param rentSearch
     * @return
     */
    ServiceMultiResult<HouseDTO> query(RentSearch rentSearch);

}
