package com.elasticsearch.demo.service.impl;

import com.elasticsearch.demo.emuns.BaiduMapEnum;
import com.elasticsearch.demo.emuns.LevelEnum;
import com.elasticsearch.demo.entity.Subway;
import com.elasticsearch.demo.entity.SubwayStation;
import com.elasticsearch.demo.entity.SupportAddress;
import com.elasticsearch.demo.repository.SubwayRepository;
import com.elasticsearch.demo.repository.SubwayStationRepository;
import com.elasticsearch.demo.repository.SupportAddressRepository;
import com.elasticsearch.demo.service.IAddressService;
import com.elasticsearch.demo.service.ServiceMultiResult;
import com.elasticsearch.demo.service.ServiceResult;
import com.elasticsearch.demo.service.search.BaiduMapLocation;
import com.elasticsearch.demo.web.dto.SubwayDTO;
import com.elasticsearch.demo.web.dto.SubwayStationDTO;
import com.elasticsearch.demo.web.dto.SupportAddressDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhumingli
 * @create 2018-08-28 下午10:30
 * @desc
 **/
@Service
@Slf4j
public class IAddressServiceImpl implements IAddressService {

    @Autowired
    private SupportAddressRepository supportAddressRepository;

    @Autowired
    private SubwayRepository subwayRepository;

    @Autowired
    private SubwayStationRepository subwayStationRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public ServiceMultiResult<SupportAddressDTO> findAllCities() {
        List<SupportAddress> supportAddressList = supportAddressRepository.findAllByLevel(LevelEnum.CITY.getValue());
        List<SupportAddressDTO> supportAddressDTOList = new ArrayList<>();
        for (SupportAddress supportAddress : supportAddressList) {
            SupportAddressDTO target = modelMapper.map(supportAddress,SupportAddressDTO.class);
            supportAddressDTOList.add(target);
        }

        return new ServiceMultiResult<>(supportAddressDTOList.size(),supportAddressDTOList) ;
    }

    @Override
    public Map<LevelEnum, SupportAddressDTO> findCityAndRegion(String cityEnName, String regionEnName) {
        Map<LevelEnum, SupportAddressDTO> result = new HashMap<>();

        SupportAddress city = supportAddressRepository.findByEnNameAndLevel(cityEnName, LevelEnum.CITY
                .getValue());
        SupportAddress region = supportAddressRepository.findByEnNameAndBelongTo(regionEnName, city.getEnName());

        result.put(LevelEnum.CITY, modelMapper.map(city, SupportAddressDTO.class));
        result.put(LevelEnum.REGION, modelMapper.map(region, SupportAddressDTO.class));
        return result;
    }

    @Override
    public ServiceMultiResult<SupportAddressDTO> findAllRegionsByCityName(String cityName) {
        if (cityName == null) {
            return new ServiceMultiResult<>(0, null);
        }

        List<SupportAddressDTO> result = new ArrayList<>();

        List<SupportAddress> regions = supportAddressRepository.findAllByLevelAndBelongTo(LevelEnum.REGION
                .getValue(), cityName);
        for (SupportAddress region : regions) {
            result.add(modelMapper.map(region, SupportAddressDTO.class));
        }
        return new ServiceMultiResult<>(regions.size(), result);
    }

    @Override
    public List<SubwayDTO> findAllSubwayByCity(String cityEnName) {
        List<SubwayDTO> result = new ArrayList<>();
        List<Subway> subways = subwayRepository.findAllByCityEnName(cityEnName);
        if (subways.isEmpty()) {
            return result;
        }

        subways.forEach(subway -> result.add(modelMapper.map(subway, SubwayDTO.class)));
        return result;
    }

    @Override
    public List<SubwayStationDTO> findAllStationBySubway(Long subwayId) {
        List<SubwayStationDTO> result = new ArrayList<>();
        List<SubwayStation> stations = subwayStationRepository.findAllBySubwayId(subwayId);
        if (stations.isEmpty()) {
            return result;
        }

        stations.forEach(station -> result.add(modelMapper.map(station, SubwayStationDTO.class)));
        return result;
    }

    @Override
    public ServiceResult<SubwayDTO> findSubway(Long subwayId) {
        if (subwayId == null) {
            return ServiceResult.notFound();
        }
        Subway subway = subwayRepository.findOne(subwayId);
        if (subway == null) {
            return ServiceResult.notFound();
        }
        return ServiceResult.of(modelMapper.map(subway, SubwayDTO.class));
    }

    @Override
    public ServiceResult<SubwayStationDTO> findSubwayStation(Long stationId) {
        if (stationId == null) {
            return ServiceResult.notFound();
        }
        SubwayStation station = subwayStationRepository.findOne(stationId);
        if (station == null) {
            return ServiceResult.notFound();
        }
        return ServiceResult.of(modelMapper.map(station, SubwayStationDTO.class));
    }

    @Override
    public ServiceResult<SupportAddressDTO> findCity(String cityEnName) {
        if (cityEnName == null) {
            return ServiceResult.notFound();
        }

        SupportAddress supportAddress = supportAddressRepository.findByEnNameAndLevel(cityEnName, LevelEnum.CITY.getValue());
        if (supportAddress == null) {
            return ServiceResult.notFound();
        }

        SupportAddressDTO addressDTO = modelMapper.map(supportAddress, SupportAddressDTO.class);

        return ServiceResult.of(addressDTO);
    }

    @Override
    public ServiceResult<BaiduMapLocation> getBaiduMapLocation(String city, String address) {

        String encodeAddress ;
        String encodeCity;

        try {
            encodeAddress = URLEncoder.encode(address,"UTF-8");
            encodeCity = URLEncoder.encode(city,"UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log.error("Error to encode house address,", e);
            return new ServiceResult<BaiduMapLocation>(false,"Error to encode house address");
        }

        HttpClient httpClient = HttpClients.createDefault();
        StringBuilder sb = new StringBuilder(BaiduMapEnum.BAIDU_MAP_GEOCONV_API.getValue());

        sb.append("address=").append(encodeAddress).append("&")
                .append("city=").append(encodeCity).append("&")
                .append("output=json&").append("ak=").append(BaiduMapEnum.BAIDU_MAP_KEY.getValue());

        URL url1 = null;
        HttpGet httpGet = null;
        try {
            url1 = new URL(sb.toString());
            URI uri = new URI(url1.getProtocol(), url1.getHost(), url1.getPath(), url1.getQuery(), null);
            httpGet = new HttpGet(uri);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);

            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
                return new ServiceResult<BaiduMapLocation>(false,"Can not get baidu map location");
            }
            String result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

            JsonNode jsonNode = objectMapper.readTree(result);
            int status = jsonNode.get("status").asInt();
            if (status != 0) {
                return new ServiceResult<BaiduMapLocation>(false,"Error tp  get baidu map location for status : " + status);
            }else {
                BaiduMapLocation baiduMapLocation = new BaiduMapLocation();
                JsonNode jsonLocation = jsonNode.get("result").get("location");
                baiduMapLocation.setLongitude(jsonLocation.get("lng").asDouble());
                baiduMapLocation.setLatitude(jsonLocation.get("lat").asDouble());

                return ServiceResult.of(baiduMapLocation);
            }



        } catch (IOException e) {
            e.printStackTrace();
            log.error("Error to fetch baidumap api ,", e);
            return new ServiceResult<BaiduMapLocation>(false,"Error to fetch baidumap api ");
        }

    }

    @Override
    public ServiceResult lbsUpload(BaiduMapLocation location, String title, String address, Long houseId, int price, int area) {
        HttpClient httpClient = HttpClients.createDefault();

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair(("latitude"),String.valueOf(location.getLatitude())));
        nvps.add(new BasicNameValuePair(("longitude"),String.valueOf(location.getLongitude())));
        nvps.add(new BasicNameValuePair(("coord_type"),"3"));
        nvps.add(new BasicNameValuePair(("geotable_id"),BaiduMapEnum.GEOTABLE_ID.getValue()));
        nvps.add(new BasicNameValuePair(("ak"),BaiduMapEnum.BAIDU_MAP_KEY.getValue()));
        nvps.add(new BasicNameValuePair(("houseId"),String.valueOf(houseId)));
        nvps.add(new BasicNameValuePair(("title"),String.valueOf(title)));
        nvps.add(new BasicNameValuePair(("address"),String.valueOf(address)));
        nvps.add(new BasicNameValuePair(("price"),String.valueOf(price)));
        nvps.add(new BasicNameValuePair(("area"),String.valueOf(area)));

        HttpPost httpPost;
        if (isLbsDataExists(houseId)){
            httpPost = new HttpPost(BaiduMapEnum.LBS_UPDATE_API.getValue());
        }else {
            httpPost = new HttpPost(BaiduMapEnum.LBS_CREATE_API.getValue());
        }

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nvps,"UTF-8"));
            HttpResponse response = httpClient.execute(httpPost);
            String result = EntityUtils.toString(response.getEntity(),"UTF-8");
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK ){
                log.error("Can not update lbs data for response: " + result);
                return new ServiceResult(false,"Can not upload baidu lbs data");
            }
            JsonNode jsonNode = objectMapper.readTree(result);
            int status = jsonNode.get("status").asInt();
            if (status != 0){
                String message = jsonNode.get("message").asText();
                log.error("Can not update lbs data for status : {} and message: {} " ,status, message);
                return new ServiceResult(false,message);
            }

            return  ServiceResult.success();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ServiceResult(false);
    }

    private boolean isLbsDataExists(Long houseId){
        HttpClient httpClient = HttpClients.createDefault();

        StringBuilder sb = new StringBuilder(BaiduMapEnum.LBS_QUERY_API.getValue());
        sb.append("geotable_id=").append(BaiduMapEnum.GEOTABLE_ID.getValue())
                .append("&ak=").append(BaiduMapEnum.BAIDU_MAP_KEY.getValue())
                .append("&houseId=").append(houseId).append(",").append(houseId);
        HttpGet get = new HttpGet(sb.toString());
        try {
            HttpResponse response = httpClient.execute(get);
            String result = EntityUtils.toString(response.getEntity(),"UTF-8");
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK ){
                log.error("Can not get lbs data for response: " + result);
                return false;
            }
            JsonNode jsonNode = objectMapper.readTree(result);
            int status = jsonNode.get("status").asInt();
            if (status != 0){
                log.error("Can not get lbs data for status: " + status);
                return false;
            }else {
                long size = jsonNode.get("size").asLong();
                if (size > 0){
                    return true;
                }
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public ServiceResult removeLbs(Long houseId) {
        HttpClient httpClient = HttpClients.createDefault();

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair(("geotable_id"),BaiduMapEnum.GEOTABLE_ID.getValue()));
        nvps.add(new BasicNameValuePair(("ak"),BaiduMapEnum.BAIDU_MAP_KEY.getValue()));
        nvps.add(new BasicNameValuePair(("houseId"),String.valueOf(houseId)));

        HttpPost delete = new HttpPost(BaiduMapEnum.LBS_DELETE_API.getValue());

        try {
            delete.setEntity(new UrlEncodedFormEntity(nvps,"UTF-8"));

            HttpResponse response = httpClient.execute(delete);
            String result = EntityUtils.toString(response.getEntity(),"UTF-8");
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK ){
                log.error("Can not delete lbs data for response: " + result);
                return new ServiceResult(false,"Can not delete baidu lbs data");
            }
            JsonNode jsonNode = objectMapper.readTree(result);
            int status = jsonNode.get("status").asInt();
            if (status != 0){
                String message = jsonNode.get("message").asText();
                log.error("Can not delete lbs data for status : {} and message: {} " ,status, message);
                return new ServiceResult(false,message);
            }

            return ServiceResult.success();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ServiceResult(false);
    }

}
