package com.elasticsearch.demo.service.search;

/**
 * @author zhumingli
 * @create 2018-09-06 上午10:34
 * @desc 索引关键词 定义
 **/
public enum  HouseIndexKey {

    HOUSE_ID ( "houseId"),
    TITLE ( "title"),
    PRICE ( "price"),
    AREA ( "area"),
    CREATE_TIME ( "createTime"),
    LAST_UPDATE_TIME ( "lastUpdateTime"),
    CITY_EN_NAME ( "cityEnName"),
    REGION_EN_NAME ( "regionEnName"),
    DIRECTION ( "direction"),
    DISTANCE_TO_SUBWAY ( "distanceToSubway"),
    STREET ( "street"),
    DISTRICT ( "district"),
    DESCRIPTION ( "description"),
    LAYOUT_DESC ( "layoutDesc"),
    TRAFFIC ( "traffic"),
    ROUND_SERVICE ( "roundService"),
    RENT_WAY ( "rentWay"),
    SUBWAY_LINE_NAME ( "subwayLineName"),
    SUBWAY_STATION_NAME ( "subwayStationName"),
    TAGS ( "tags"),
    AGG_DISTRICT ( "agg_district"),
    AGG_REGION ( "agg_region");

    private String value;

    HouseIndexKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
