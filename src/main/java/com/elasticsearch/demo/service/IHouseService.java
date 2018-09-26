package com.elasticsearch.demo.service;

import com.elasticsearch.demo.emuns.HouseSubscribeStatusEnum;
import com.elasticsearch.demo.entity.House;
import com.elasticsearch.demo.entity.HouseSubscribe;
import com.elasticsearch.demo.repository.HouseSubscribeRepository;
import com.elasticsearch.demo.web.dto.HouseDTO;
import com.elasticsearch.demo.web.dto.HouseSubscribeDTO;
import com.elasticsearch.demo.web.form.DatatableSearch;
import com.elasticsearch.demo.web.form.HouseForm;
import com.elasticsearch.demo.web.form.MapSearch;
import com.elasticsearch.demo.web.form.RentSearch;
import org.springframework.data.util.Pair;

import java.util.Date;

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

    /**
     * 全地图查询
     * @param mapSearch
     * @return
     */
    ServiceMultiResult<HouseDTO>  wholeMapQuery(MapSearch mapSearch);

    /**
     * 精确范围数据查询
     * @param mapSearch
     * @return
     */
    ServiceMultiResult<HouseDTO> boundMapQuery(MapSearch mapSearch);

    /**
     * 加入预约清单
     * @param houseId
     * @return
     */
    ServiceResult addSubscribeOrder(Long houseId);

    /**
     * @param status
     * @param start
     * @param size
     * @return
     */
    ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> querySubscribeList(HouseSubscribeStatusEnum status, int start, int size);

    /**
     * 创建预约
     * @param houseId
     * @param orderTime
     * @param telephone
     * @param desc
     * @return
     */
    ServiceResult subscribe(Long houseId, Date orderTime, String telephone, String desc);

    /**
     * 取消预约
     * @param houseId
     * @return
     */
    ServiceResult cancelSubscribe(Long houseId);

    /**
     * 管理员查询预约列表
     * @param start
     * @param size
     * @return
     */
    ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> findSubscribeList(int start, int size);

    /**
     * 完成预约
     * @param houseId
     * @return
     */
    ServiceResult finishSubscribe(Long houseId);
}
