package com.elasticsearch.demo.emuns;

/**
 * @author zhumingli
 * @create 2018-09-19 下午9:39
 * @desc
 **/
public enum BaiduMapEnum {

    BAIDU_MAP_KEY("ui0Ak0KRauHCRHjhoY7p1xxlfHbMx3ls"),
    BAIDU_MAP_GEOCONV_API("http://api.map.baidu.com/geocoder/v2/?"),
    LBS_CREATE_API("http://api.map.baidu.com/geodata/v3/poi/create"),
    LBS_QUERY_API("http://api.map.baidu.com/geodata/v3/poi/list?"),
    LBS_UPDATE_API("http://api.map.baidu.com/geodata/v3/poi/update"),
    LBS_DELETE_API("http://api.map.baidu.com/geodata/v3/poi/delete"),
    GEOTABLE_ID("")
    ;
    private String value;

    BaiduMapEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
