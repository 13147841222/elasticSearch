package com.elasticsearch.demo.web.controller.house;

import com.elasticsearch.demo.base.ApiResponse;
import com.elasticsearch.demo.base.RentValueBlock;
import com.elasticsearch.demo.emuns.ApiResponseEnum;
import com.elasticsearch.demo.emuns.LevelEnum;
import com.elasticsearch.demo.entity.SupportAddress;
import com.elasticsearch.demo.service.*;
import com.elasticsearch.demo.service.search.ISearchService;
import com.elasticsearch.demo.web.dto.*;
import com.elasticsearch.demo.web.form.MapSearch;
import com.elasticsearch.demo.web.form.RentSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zhumingli
 * @create 2018-08-28 下午9:54
 * @desc
 **/
@Controller
@RequestMapping
public class HouseController {

    @Autowired
    private IAddressService iAddressService;

    @Autowired
    private IHouseService iHouseService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ISearchService searchService;

    @GetMapping("rent/house/autoComplete")
    public ApiResponse autoComplete(@RequestParam(value = "prefix") String prefix ){
        if (prefix.isEmpty()){
            return ApiResponse.ofStatus(ApiResponseEnum.BAD_REQUEST);
        }

        List<String> result = new ArrayList<>();

        ServiceResult<List<String>> serviceResult = searchService.suggest(prefix);


        return ApiResponse.ofSuccess(serviceResult);
    }


    @GetMapping("address/support/cities")
    @ResponseBody
    public ApiResponse getSupportCities(){
        ServiceMultiResult<SupportAddressDTO> result = iAddressService.findAllCities();
        if(result.getResultSize() == 0){
            return ApiResponse.ofMessage(ApiResponseEnum.NOT_FOUND.getCode(),ApiResponseEnum.NOT_FOUND.getMessage());
        }
        return ApiResponse.ofSuccess(result.getResult());
    }

    /**
     * 获取对应城市支持区域列表
     * @param cityEnName
     * @return
     */
    @GetMapping("address/support/regions")
    @ResponseBody
    public ApiResponse getSupportRegions(@RequestParam(name = "city_name") String cityEnName) {
        ServiceMultiResult<SupportAddressDTO> addressResult = iAddressService.findAllRegionsByCityName(cityEnName);
        if (addressResult.getResult() == null || addressResult.getTotal() < 1) {
            return ApiResponse.ofStatus(ApiResponseEnum.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(addressResult.getResult());
    }

    /**
     * 获取具体城市所支持的地铁线路
     * @param cityEnName
     * @return
     */
    @GetMapping("address/support/subway/line")
    @ResponseBody
    public ApiResponse getSupportSubwayLine(@RequestParam(name = "city_name") String cityEnName) {
        List<SubwayDTO> subways = iAddressService.findAllSubwayByCity(cityEnName);
        if (subways.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponseEnum.NOT_FOUND);
        }

        return ApiResponse.ofSuccess(subways);
    }

    /**
     * 获取对应地铁线路所支持的地铁站点
     * @param subwayId
     * @return
     */
    @GetMapping("address/support/subway/station")
    @ResponseBody
    public ApiResponse getSupportSubwayStation(@RequestParam(name = "subway_id") Long subwayId) {
        List<SubwayStationDTO> stationDTOS = iAddressService.findAllStationBySubway(subwayId);
        if (stationDTOS.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponseEnum.NOT_FOUND);
        }

        return ApiResponse.ofSuccess(stationDTOS);
    }

    @GetMapping("rent/house")
    public String rentHousePage(@ModelAttribute RentSearch rentSearch, Model model, HttpSession httpSession, RedirectAttributes redirectAttributes){

        if(rentSearch.getCityEnName() == null){
            String cityEnNameInSession = (String)httpSession.getAttribute("cityEnName");
            if(cityEnNameInSession == null){
                redirectAttributes.addAttribute("msg","must_chose_city");
                return "redirect:/index";
            } else {
                rentSearch.setCityEnName(cityEnNameInSession);
            }
        } else {
            httpSession.setAttribute("cityName",rentSearch.getCityEnName());
        }

        ServiceResult<SupportAddressDTO> addressDTOServiceResult = iAddressService.findCity(rentSearch.getCityEnName());
        if (!addressDTOServiceResult.isSuccess()){
            redirectAttributes.addAttribute("msg","must_chose_city");
            return "redirect:/index";
        }

        model.addAttribute("currentCity", addressDTOServiceResult.getResult());

        ServiceMultiResult<SupportAddressDTO> addressResult = iAddressService.findAllRegionsByCityName(rentSearch.getCityEnName());

        if (addressResult.getResult() == null || addressResult.getTotal() < 1){
            redirectAttributes.addAttribute("msg","must_chose_city");
            return "redirect:/index";
        }

        ServiceMultiResult<HouseDTO> serviceMultiResult = iHouseService.query(rentSearch);

        model.addAttribute("total",serviceMultiResult.getTotal());
        model.addAttribute("houses",serviceMultiResult.getResult());

        if(rentSearch.getRegionEnName() == null){
            rentSearch.setRegionEnName("*");
        }
        model.addAttribute("searchBody",rentSearch);
        model.addAttribute("regions", addressResult.getResult());

        model.addAttribute("priceBlocks", RentValueBlock.PRICE_BLOCK);
        model.addAttribute("areaBlocks", RentValueBlock.AREA_BLOCK);

        model.addAttribute("currentPriceBlock", RentValueBlock.matchPrice(rentSearch.getPriceBlock()));
        model.addAttribute("currentAreaBlock", RentValueBlock.matchArea(rentSearch.getAreaBlock()));


        return "rent-list";
    }

    @GetMapping("rent/house/show/{id}")
    public String show(@PathVariable(value = "id") Long id,
                       Model model){

        ServiceResult<HouseDTO> houseDTOServiceResult = iHouseService.findCompleteOne(id);
        if (!houseDTOServiceResult.isSuccess()) {
            return "404";
        }

        HouseDTO houseDTO = houseDTOServiceResult.getResult();

        Map<LevelEnum, SupportAddressDTO> addressDTOMap = iAddressService.findCityAndRegion(houseDTO.getCityEnName(), houseDTO.getRegionEnName());

        SupportAddressDTO city = addressDTOMap.get(LevelEnum.CITY);
        SupportAddressDTO region = addressDTOMap.get(LevelEnum.REGION);

        model.addAttribute("house",houseDTO);
        model.addAttribute("city",city);
        model.addAttribute("region",region);

        ServiceResult<UserDTO> userDTOServiceResult = userService.findById(houseDTO.getAdminId());

        if(!userDTOServiceResult.isSuccess()){

        }
        model.addAttribute("agent",userDTOServiceResult.getResult());

        ServiceResult<Long> serviceResult = searchService.aggregateDistrictHouse(city.getEnName(),region.getEnName(),houseDTO.getDistrict());
        model.addAttribute("houseCountInDistrict", serviceResult.getResult());

        return "house-detail";
    }

    @GetMapping("rent/house/map")
    public String rentMappage(@RequestParam(value = "cityEnName") String cityEnName,
                              Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttributes){
        ServiceResult<SupportAddressDTO> cityResult = iAddressService.findCity(cityEnName);
        if (!cityResult.isSuccess()){
            return "redirect:/index";
        }else {
            session.setAttribute("cityEnName",cityEnName);
            model.addAttribute("city",cityResult.getResult());
        }

        ServiceMultiResult regionsResult = iAddressService.findAllRegionsByCityName(cityEnName);

        ServiceMultiResult<HouseBucketDTO> bucketDTOs = searchService.mapAggregate(cityEnName);

        model.addAttribute("aggData", bucketDTOs.getResult());
        model.addAttribute("total", bucketDTOs.getTotal());
        model.addAttribute("regions", regionsResult.getResult());

        return "rent-map";
    }

    @GetMapping("rent/house/map/houses")
    @ResponseBody
    public ApiResponse rentMapHouse(@ModelAttribute MapSearch mapSearch){

        if (mapSearch.getCityEnName() == null){
            return ApiResponse.ofStatus(ApiResponseEnum.BAD_REQUEST);
        }
        ServiceMultiResult<HouseDTO>  serviceMultiResult;
        if (mapSearch.getLevel() < 13){
            serviceMultiResult = iHouseService.wholeMapQuery(mapSearch);
        }else {
            serviceMultiResult = iHouseService.boundMapQuery(mapSearch);
        }
        ApiResponse apiResponse = ApiResponse.ofSuccess(serviceMultiResult.getResult());
        apiResponse.setMore(serviceMultiResult.getTotal() > (mapSearch.getStart() + mapSearch.getSize()));
        return apiResponse;
    }

}
