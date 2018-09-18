package com.elasticsearch.demo.service;

import com.elasticsearch.demo.emuns.LevelEnum;
import com.elasticsearch.demo.entity.SupportAddress;
import com.elasticsearch.demo.service.search.BaiduMapLocation;
import com.elasticsearch.demo.web.dto.SubwayDTO;
import com.elasticsearch.demo.web.dto.SubwayStationDTO;
import com.elasticsearch.demo.web.dto.SupportAddressDTO;

import java.util.List;
import java.util.Map;


/**
 * @author zhumingli
 * @create 2018-08-28 下午10:24
 * @desc
 **/
public interface IAddressService {
    /**
     * 获取所有支持的城市列表
     * @return
     */
    ServiceMultiResult<SupportAddressDTO> findAllCities();

    /**
     * 根据英文简写获取具体区域的信息
     * @param cityEnName
     * @param regionEnName
     * @return
     */
    Map<LevelEnum, SupportAddressDTO> findCityAndRegion(String cityEnName, String regionEnName);

    /**
     * 根据城市英文简写获取该城市所有支持的区域信息
     * @param cityName
     * @return
     */
    ServiceMultiResult findAllRegionsByCityName(String cityName);

    /**
     * 获取该城市所有的地铁线路
     * @param cityEnName
     * @return
     */
    List<SubwayDTO> findAllSubwayByCity(String cityEnName);

    /**
     * 获取地铁线路所有的站点
     * @param subwayId
     * @return
     */
    List<SubwayStationDTO> findAllStationBySubway(Long subwayId);

    /**
     * 获取地铁线信息
     * @param subwayId
     * @return
     */
    ServiceResult<SubwayDTO> findSubway(Long subwayId);

    /**
     * 获取地铁站点信息
     * @param stationId
     * @return
     */
    ServiceResult<SubwayStationDTO> findSubwayStation(Long stationId);

    /**
     * 根据城市英文简写获取城市详细信息
     * @param cityEnName
     * @return
     */
    ServiceResult<SupportAddressDTO> findCity(String cityEnName);


    /**
     * 根据城市以及具体地理位置获取百度地图的经纬度
     */
    ServiceResult<BaiduMapLocation> getBaiduMapLocation(String city, String address);
}
