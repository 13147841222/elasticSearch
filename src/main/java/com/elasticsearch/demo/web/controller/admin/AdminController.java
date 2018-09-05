package com.elasticsearch.demo.web.controller.admin;

import com.elasticsearch.demo.base.ApiDataTableResponse;
import com.elasticsearch.demo.base.ApiResponse;
import com.elasticsearch.demo.config.FileuploadConfig;
import com.elasticsearch.demo.emuns.ApiResponseEnum;
import com.elasticsearch.demo.emuns.HouseStatusEnum;
import com.elasticsearch.demo.emuns.LevelEnum;
import com.elasticsearch.demo.entity.SupportAddress;
import com.elasticsearch.demo.service.*;
import com.elasticsearch.demo.web.dto.*;
import com.elasticsearch.demo.web.form.DatatableSearch;
import com.elasticsearch.demo.web.form.HouseForm;
import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author zhumingli
 * @create 2018-08-22 下午10:23
 * @desc 换台管理
 **/
@Controller
public class AdminController {

    private static final Integer ADDRESS_SIZE = 2;

    @Autowired
    private IQiNiuService iQiNiuService;

    @Autowired
    private IAddressService iAddressService;

    @Autowired
    private FileuploadConfig fileuploadConfig;

    @Autowired
    private IHouseService houseService;

    @GetMapping("/admin/center")
    public String adminCenterPage(){
        return "admin/center";
    }

    @GetMapping("/admin/welcome")
    public String welcomePage(){
        return "admin/welcome";
    }

    @GetMapping("/admin/login")
    public String adminLoginPage(){
        return "admin/login";
    }

    @Autowired
    private Gson gson;

    /**
     * 添加房源
     * @return
     */
    @GetMapping("admin/add/house")
    public String addHousePage() {
        return "admin/house-add";
    }

    /**
     * 房源列表页
     * @return
     */
    @GetMapping("admin/house/list")
    public String houseListPage() {
        return "admin/house-list";
    }


    @PostMapping(value = "admin/upload/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ApiResponse uploadPhoto(@RequestParam("file")MultipartFile file){
        if(file.isEmpty()){
            return ApiResponse.ofStatus(ApiResponseEnum.NOT_VALID_PARAM);
        }
        //获取文件名
        String fileName = file.getOriginalFilename();

        try {
            InputStream inputStream = file.getInputStream();
            Response response = iQiNiuService.uploadFile(inputStream);
            if (response.isOK()){
                QiNiuPutRet ret = gson.fromJson(response.bodyString(), QiNiuPutRet.class);
                return ApiResponse.ofSuccess(ret);
            }else {
                return ApiResponse.ofMessage(response.statusCode,response.getInfo());
            }
        } catch (QiniuException e){
            e.printStackTrace();
            Response response = e.response;
            return ApiResponse.ofMessage(response.statusCode,response.getInfo());
        }
        catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.ofStatus(ApiResponseEnum.INTERNAL_SERVER_ERROR);
        }
        /*
        //文件存储路径
        File target = new File(fileuploadConfig.getPath() + fileName);

        try {
            file.transferTo(target);
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.ofStatus(ApiResponseEnum.INTERNAL_SERVER_ERROR);
        }
        return ApiResponse.ofSuccess(null);
        */

    }

    @PostMapping("admin/houses")
    @ResponseBody
    public ApiDataTableResponse houses(@ModelAttribute DatatableSearch datatableSearch){

        ServiceMultiResult<HouseDTO> serviceMultiResult =  houseService.adminQuery(datatableSearch);

        ApiDataTableResponse apiDataTableResponse = new ApiDataTableResponse(ApiResponseEnum.SUCCESS);

        apiDataTableResponse.setData(serviceMultiResult.getResult());
        apiDataTableResponse.setRecordsFiltered(serviceMultiResult.getTotal());
        apiDataTableResponse.setRecordsTotal(serviceMultiResult.getTotal());
        apiDataTableResponse.setDraw(datatableSearch.getDraw());
        return apiDataTableResponse;
    }


    @PostMapping("admin/add/house")
    @ResponseBody
    public ApiResponse addHouse(@Valid @ModelAttribute("form-house-add")HouseForm houseForm , BindingResult bindingResult){
        if(bindingResult.hasErrors() ){
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), bindingResult.getAllErrors().get(0).getDefaultMessage(),null);
        }

        if(houseForm.getPhotos() == null || houseForm.getCover() == null ){
            return  ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(),"必须上传图片");
        }

        Map<LevelEnum, SupportAddressDTO> addressMap = iAddressService.findCityAndRegion(houseForm.getCityEnName(),houseForm.getRegionEnName());

        if(addressMap.keySet().size() != ADDRESS_SIZE){
            return ApiResponse.ofSuccess(ApiResponseEnum.NOT_VALID_PARAM);
        }

        ServiceResult<HouseDTO> serviceResult = houseService.save(houseForm);

        if(serviceResult.isSuccess()){
            return ApiResponse.ofSuccess(serviceResult.getResult());
        }

        return ApiResponse.ofSuccess(ApiResponseEnum.NOT_VALID_PARAM);
    }

    @GetMapping("admin/house/edit")
    public String houseEditPage(@RequestParam(value = "id") Long id , Model model){
        if(id == null || id < 1){
                return "404";
        }

        ServiceResult<HouseDTO> serviceResult = houseService.findCompleteOne(id);

        if(!serviceResult.isSuccess()){
            return "404";
        }

        HouseDTO result = serviceResult.getResult();

        model.addAttribute("house", result);

        Map<LevelEnum, SupportAddressDTO> addsMap =iAddressService.findCityAndRegion(
                result.getCityEnName(),
                result.getRegionEnName()
        );

        model.addAttribute("city",addsMap.get(LevelEnum.CITY));
        model.addAttribute("region",addsMap.get(LevelEnum.REGION));

        HouseDetailDTO houseDetailDTO = result.getHouseDetail();
        ServiceResult<SubwayDTO> subwayDTOServiceResult = iAddressService.findSubway(houseDetailDTO.getSubwayLineId());
        if(subwayDTOServiceResult.isSuccess()){
            model.addAttribute("subway", subwayDTOServiceResult.getResult());
        }

        ServiceResult<SubwayStationDTO> subwayStationDTOServiceResult = iAddressService.findSubwayStation(houseDetailDTO.getSubwayStationId());
        if(subwayStationDTOServiceResult.isSuccess()){
            model.addAttribute("subway", subwayStationDTOServiceResult.getResult());
        }
        return "admin/house-edit";
    }


    @PostMapping("admin/house/edit")
    @ResponseBody
    public ApiResponse saveHouse(@Valid @ModelAttribute("form-house-edit") HouseForm houseForm , BindingResult bindingResult){

        if(bindingResult.hasErrors()){
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(),bindingResult.getAllErrors().get(0).getDefaultMessage(),null );
        }

        Map<LevelEnum, SupportAddressDTO> addressMap = iAddressService.findCityAndRegion(houseForm.getCityEnName(), houseForm.getRegionEnName());

        if(addressMap.keySet().size() != 2){
            return ApiResponse.ofSuccess(ApiResponseEnum.NOT_VALID_PARAM);
        }

        ServiceResult serviceResult = houseService.update(houseForm);

        if(serviceResult.isSuccess()){
            return ApiResponse.ofSuccess(null);
        }

        ApiResponse response = ApiResponse.ofStatus(ApiResponseEnum.BAD_REQUEST);
        response.setMessage(serviceResult.getMessage());
        return response;
    }


    /**
     * 审核接口
     * @param id
     * @param operation
     * @return
     */
    @PutMapping("admin/house/operate/{id}/{operation}")
    @ResponseBody
    public ApiResponse operateHouse(@PathVariable(value = "id") Long id,
                                    @PathVariable(value = "operation") int operation ){
        if (operation <= 0){
            return ApiResponse.ofStatus(ApiResponseEnum.NOT_VALID_PARAM);
        }
        if (operation == HouseStatusEnum.PASSES.getCode()){
            houseService.updateStatus(id,operation);
            return ApiResponse.ofSuccess(ApiResponseEnum.SUCCESS);
        }

        return ApiResponse.ofStatus(ApiResponseEnum.BAD_REQUEST);
    }
}
