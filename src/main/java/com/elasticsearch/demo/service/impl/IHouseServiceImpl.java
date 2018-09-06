package com.elasticsearch.demo.service.impl;

import com.elasticsearch.demo.base.HouseSort;
import com.elasticsearch.demo.base.LoginUserUtil;
import com.elasticsearch.demo.emuns.HouseStatusEnum;
import com.elasticsearch.demo.entity.*;
import com.elasticsearch.demo.repository.*;
import com.elasticsearch.demo.service.IHouseService;
import com.elasticsearch.demo.service.IQiNiuService;
import com.elasticsearch.demo.service.ServiceMultiResult;
import com.elasticsearch.demo.service.ServiceResult;
import com.elasticsearch.demo.service.search.ISearchService;
import com.elasticsearch.demo.web.dto.HouseDTO;
import com.elasticsearch.demo.web.dto.HouseDetailDTO;
import com.elasticsearch.demo.web.dto.HousePictureDTO;
import com.elasticsearch.demo.web.form.DatatableSearch;
import com.elasticsearch.demo.web.form.HouseForm;
import com.elasticsearch.demo.web.form.PhotoForm;
import com.elasticsearch.demo.web.form.RentSearch;
import com.google.common.collect.Maps;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author zhumingli
 * @create 2018-08-29 下午11:29
 * @desc
 **/
@Service
@Slf4j
public class IHouseServiceImpl implements IHouseService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private HouseDetailRepository houseDetailRepository;

    @Autowired
    private HousePictureRepository housePictureRepository;

    @Autowired
    private HouseTagRepository houseTagRepository;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private SubwayRepository subwayRepository;

    @Autowired
    private SubwayStationRepository subwayStationRepository;

    @Autowired
    private IQiNiuService iQiNiuService;

    @Autowired
    private ISearchService searchService;

    @Value("${qiniu.cdn.prefix}")
    private String cdnPrefix;

    @Override
    public ServiceResult<HouseDTO> save(HouseForm houseForm) {

        HouseDetail houseDetail = new HouseDetail();

        ServiceResult<HouseDTO> houseDTOServiceResult = wrapperDetailInfo(houseDetail,houseForm);

        if(houseDTOServiceResult != null){
            return houseDTOServiceResult;
        }
        House house = new House();


        modelMapper.map(houseForm, house);

        Date now = new Date();
        house.setCreateTime(now);
        house.setLastUpdateTime(now);
        house.setAdminId(LoginUserUtil.getLoginUserId());

        house = houseRepository.save(house);

        houseDetail.setId(house.getId());

        houseDetail = houseDetailRepository.save(houseDetail);

        List<HousePicture> pictureList = genertePictures(houseForm,house.getId());

        Iterable<HousePicture> pictureIterable = housePictureRepository.save(pictureList);

        HouseDTO houseDTO = modelMapper.map(house,HouseDTO.class);
        HouseDetailDTO houseDetailDTO = modelMapper.map(houseDetail,HouseDetailDTO.class);

        houseDTO.setHouseDetail(houseDetailDTO);

        List<HousePictureDTO> pictureDTOList = new ArrayList<>();
        pictureIterable.forEach(e -> pictureDTOList.add(modelMapper.map(e,HousePictureDTO.class)));

        houseDTO.setPictures(pictureDTOList);
        houseDTO.setCover(this.cdnPrefix + houseDTO.getCover());

        List<String> tags = houseForm.getTags();

        if (tags != null || tags.isEmpty()  ){
            List<HouseTag> houseTags = new ArrayList<>();
            for (String tag : tags) {
                houseTags.add(new HouseTag(house.getId(), tag));
            }
            houseTagRepository.save(houseTags);
            houseDTO.setTags(tags);
        }

        return new ServiceResult<HouseDTO>(true, null,houseDTO);
    }

    @Override
    public ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch datatableSearch) {
        Sort sort = new Sort(Sort.Direction.fromString(datatableSearch.getDirection()), datatableSearch.getOrderBy());

        int page = datatableSearch.getStart() / datatableSearch.getLength() ;

        Pageable pageable = new PageRequest(page, datatableSearch.getLength(), sort);


        List<HouseDTO> houseDTOList = new ArrayList<>();

        Specification<House> specification = (root, query,cb) -> {

            /**
             * 判断  登陆的人  只能查到 当前登陆人的房源
             */
            Predicate predicate = cb.equal(root.get("adminId"), LoginUserUtil.getLoginUserId());


            predicate = cb.and(predicate, cb.notEqual(root.get("status"), HouseStatusEnum.DELETE.getCode()) );

            if(datatableSearch.getCity() != null){
                predicate = cb.and(predicate, cb.equal(root.get("cityName"), datatableSearch.getCity() ));
            }

            if(datatableSearch.getStatus() != null){
                predicate = cb.and(predicate, cb.equal(root.get("status"), datatableSearch.getStatus() ));
            }

            if(datatableSearch.getCreateTimeMin() != null){
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createTime"), datatableSearch.getCreateTimeMin() ));
            }

            if(datatableSearch.getCreateTimeMax() != null){
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createTime"), datatableSearch.getCreateTimeMax() ));
            }

            if(datatableSearch.getTitle() != null){
                predicate = cb.and(predicate, cb.like(root.get("title"), "%" + datatableSearch.getTitle() + "%"  ));
            }

            return predicate;
        };

        Page<House> housePage = houseRepository.findAll(specification, pageable);


        housePage.forEach( e ->{
            HouseDTO houseDTO = modelMapper.map(e,HouseDTO.class);
            houseDTO.setCover(this.cdnPrefix + e.getCover());

            houseDTOList.add(houseDTO);
        });

        return new ServiceMultiResult<>(housePage.getTotalElements(),houseDTOList);
    }

    @Override
    public ServiceResult<HouseDTO> findCompleteOne(Long id) {
        House house = houseRepository.findOne(id);
        if(house == null){
            return ServiceResult.notFound();
        }

        HouseDetail houseDetail = houseDetailRepository.findByHouseId(house.getId()) ;


        List<HousePicture> housePicture = housePictureRepository.findAllByHouseId(house.getId());

        HouseDetailDTO houseDetailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);
        List<HousePictureDTO> housePictureDTOList = new ArrayList<>();

        for (HousePicture picture : housePicture) {
            HousePictureDTO pictureDTO = modelMapper.map(picture, HousePictureDTO.class);
            housePictureDTOList.add(pictureDTO);
        }

        List<HouseTag> houseTagList = houseTagRepository.findAllByHouseId(house.getId());
        List<String> tags = new ArrayList<>();
        for (HouseTag houseTag : houseTagList) {
            tags.add(houseTag.getName());
        }

        HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);

        houseDTO.setHouseDetail(houseDetailDTO);

        houseDTO.setPictures(housePictureDTOList);

        houseDTO.setTags(tags);

        return  ServiceResult.of(houseDTO);
    }

    @Override
    @Transactional
    public ServiceResult update(HouseForm houseForm) {
        House house = houseRepository.findOne(houseForm.getId());
        if(house == null){
            return ServiceResult.notFound();
        }

        HouseDetail detail = this.houseDetailRepository.findByHouseId(houseForm.getId());

        if(detail == null){
            return ServiceResult.notFound();
        }

        ServiceResult wrapperResult = wrapperDetailInfo(detail, houseForm);

        if(wrapperResult != null){
            return wrapperResult;
        }

        houseDetailRepository.save(detail);

        List<HousePicture> pictureList = genertePictures(houseForm, houseForm.getId());

        housePictureRepository.save(pictureList);

        if(houseForm.getCover() == null){
            houseForm.setCover(house.getCover());
        }

        modelMapper.map(houseForm, house);
        house.setLastUpdateTime(new Date());
        houseRepository.save(house);

        if (house.getStatus() == HouseStatusEnum.PASSES.getCode()) {
            boolean flag = searchService.index(house.getId());
        }

        return ServiceResult.success();
    }

    @Override
    public ServiceResult removePhoto(Long id) {
        HousePicture picture = housePictureRepository.findOne(id);

        if(picture == null){
            return  ServiceResult.notFound();
        }

        try {
            Response response = iQiNiuService.delete(picture.getPath());
            if (response.isOK()){
                housePictureRepository.delete(id);
                return ServiceResult.success();
            }else {

                return new ServiceResult(false, response.error);
            }
        } catch (QiniuException e) {
            e.printStackTrace();
            return new ServiceResult(false, e.getMessage());
        }
    }

    @Override
    @Transactional
    public ServiceResult updateCover(Long coverId, Long targetId) {
        HousePicture housePicture = housePictureRepository.findOne(coverId);
        if(housePicture == null){
            return  ServiceResult.notFound();
        }

        houseRepository.updateCover(targetId, housePicture.getPath());

        return ServiceResult.success();
    }

    @Override
    public ServiceResult addTag(Long houseId, String tag) {
        return null;
    }

    @Override
    @Transactional
    public ServiceResult updateStatus(Long houseId, int status) {
        House house = houseRepository.findOne(houseId);
        if (house == null) {
            return  ServiceResult.notFound();
        }

        if(house.getStatus() == status ){
            return new ServiceResult(false, "状态没有发生变化");
        }

        if(house.getStatus() == HouseStatusEnum.RENTED.getCode()){
            return new ServiceResult(false, "已出租的房屋不能修改状态");
        }

        if(house.getStatus() == HouseStatusEnum.DELETE.getCode()){
            return new ServiceResult(false, "已删除的房屋不能修改状态");
        }

        houseRepository.updateStatus(houseId,status);

        /**
         * 上架更新索引 其他情况 删除索引
         */
        if (status == HouseStatusEnum.PASSES.getCode()){
            searchService.index(houseId);
        }else {
            searchService.remove(houseId);
        }
        return ServiceResult.success();
    }

    @Override
    public ServiceMultiResult<HouseDTO> query(RentSearch rentSearch) {
        /**
         *
        Sort sort = new Sort(Sort.Direction.DESC,"lastUpdateTime");
         */
        Sort sort = HouseSort.generateSort(rentSearch.getOrderBy(), rentSearch.getOrderDirection());
        int page = rentSearch.getStart() / rentSearch.getSize();

        Pageable pageable = new PageRequest(page,rentSearch.getSize(),sort);

        Specification<House> specification = (root,criteriaQuery, criteriaBuilder)->{
          Predicate predicate = criteriaBuilder.equal(root.get("status"), HouseStatusEnum.PASSES.getCode());
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("cityEnName"),rentSearch.getCityEnName()));
            if(HouseSort.DISTANCE_TO_SUBWAY_KEY.equals(rentSearch.getOrderBy()) ){
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.gt(root.get(HouseSort.DISTANCE_TO_SUBWAY_KEY), -1));
            }
            return predicate;
        };

        Page<House> housePage = houseRepository.findAll(specification,pageable);

        List<HouseDTO> houseDTOs = new ArrayList<>();
        Map<Long, HouseDTO> idToHashMap = Maps.newHashMap();
        List<Long> houseIds = new ArrayList<>();
        housePage.forEach(e ->{
            HouseDTO houseDTO = modelMapper.map(e, HouseDTO.class);
            houseDTO.setCover(this.cdnPrefix + e.getCover());
            houseDTOs.add(houseDTO);
            houseIds.add(e.getId());
            idToHashMap.put(e.getId(), houseDTO);
        });

        wrapperHosueList(houseIds,idToHashMap);

        return new ServiceMultiResult<>(housePage.getTotalElements(), houseDTOs);
    }

    /**
     * 渲染详细信息
     * @param houseIds
     * @param idToHashMap
     */
    private void wrapperHosueList(List<Long> houseIds,  Map<Long, HouseDTO> idToHashMap ){
        List<HouseDetail> houseDetailList = houseDetailRepository.findAllByHouseIdIn(houseIds);
        houseDetailList.forEach(e->{
            HouseDTO houseDTO = idToHashMap.get(e.getHouseId());
            HouseDetailDTO houseDetailDTO = modelMapper.map(e,HouseDetailDTO.class);
            houseDTO.setHouseDetail(houseDetailDTO);

        });

        List<HouseTag> tagList = houseTagRepository.findAllByHouseIdIn(houseIds);
        tagList.forEach(e -> {
            HouseDTO houseDTO = idToHashMap.get(e.getHouseId());
            houseDTO.getTags().add(e.getName());
        });
    }

    private ServiceResult wrapperDetailInfo(HouseDetail houseDetail,HouseForm houseForm){
        Subway subway = subwayRepository.findOne(houseForm.getSubwayLineId());
        if(subway == null){
            return new ServiceResult<>(false,"not valid subway line");
        }

        SubwayStation subwayStation = subwayStationRepository.findOne(houseForm.getSubwayStationId());
        if(subwayStation == null || !subway.getId().equals(subwayStation.getSubwayId())){
            return new ServiceResult<>(false,"not valid subwayStation line");
        }

        houseDetail.setSubwayLineId(subway.getId());
        houseDetail.setSubwayLineName(subway.getName());

        houseDetail.setSubwayStationId(subwayStation.getSubwayId());
        houseDetail.setSubwayStationName(subwayStation.getName());

        houseDetail.setDescription(houseForm.getDescription());
        houseDetail.setDetailAddress(houseForm.getDetailAddress());
        houseDetail.setLayoutDesc(houseForm.getLayoutDesc());
        houseDetail.setRentWay(houseForm.getRentWay());
        houseDetail.setRoundService(houseForm.getRoundService());
        houseDetail.setTraffic(houseForm.getTraffic());


        return null;
    }


    private List<HousePicture> genertePictures(HouseForm houseForm, long houseId){
        List<HousePicture> pictureList = new ArrayList<>();

        if (houseForm.getPhotos() == null || houseForm.getPhotos().isEmpty()){
            return pictureList;
        }

        for (PhotoForm photoForm : houseForm.getPhotos()) {
            HousePicture picture = new HousePicture();
            picture.setHouseId(houseId);
            picture.setCdnPrefix(cdnPrefix);
            picture.setPath(photoForm.getPath());
            picture.setHeight(photoForm.getHeight());
            picture.setWidth(photoForm.getWidth());
            pictureList.add(picture);
        }


        return pictureList;
    }
}
