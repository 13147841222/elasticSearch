package com.elasticsearch.demo.service.impl;


import com.elasticsearch.demo.emuns.IndexEnum;
import com.elasticsearch.demo.entity.House;
import com.elasticsearch.demo.entity.HouseDetail;
import com.elasticsearch.demo.entity.HouseTag;
import com.elasticsearch.demo.repository.HouseDetailRepository;
import com.elasticsearch.demo.repository.HouseRepository;
import com.elasticsearch.demo.repository.HouseTagRepository;
import com.elasticsearch.demo.service.search.HouseIndexKey;
import com.elasticsearch.demo.service.search.HouseIndexTemplate;
import com.elasticsearch.demo.service.search.ISearchService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;

import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhumingli
 * @create 2018-09-06 上午10:51
 * @desc
 **/
@Service
@Slf4j
public class ISearchServiceImpl implements ISearchService {

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private HouseDetailRepository houseDetailRepository;

    @Autowired
    private HouseTagRepository houseTagRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TransportClient transportClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean index(Long houseId) {
        House house = houseRepository.findOne(houseId);
        if (house == null) {
            log.error("Index house {} dose not exist!",houseId);
            return false;
        }

        HouseIndexTemplate houseIndexTemplate = new HouseIndexTemplate();

        modelMapper.map(house,houseIndexTemplate);

        HouseDetail houseDetail = houseDetailRepository.findByHouseId(houseId);

        if (houseDetail == null) {

        }

        modelMapper.map(houseDetail, houseIndexTemplate);

        List<HouseTag> tagList = houseTagRepository.findAllByHouseId(houseId);

        if (!tagList.isEmpty() ) {
            List<String> tags = new ArrayList<>();
            tagList.forEach(e ->
                tags.add(e.getName())
            );
            houseIndexTemplate.setTags(tags);
        }


        SearchRequestBuilder requestBuilder = transportClient.prepareSearch(IndexEnum.INDEX_NAME.getValue())
                .setTypes(IndexEnum.INDEX_TYPE.getValue())
                .setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID.getValue(), houseId));

        log.debug("requestBuilder {}", requestBuilder.toString());

        SearchResponse searchResponse = requestBuilder.get();

        boolean flag ;
        long totalHit = searchResponse.getHits().getTotalHits();

        if (totalHit == 0) {
            flag = create(houseIndexTemplate);
        } else if (totalHit == 1){
            String esId = searchResponse.getHits().getAt(0).getId();
            flag = update(houseIndexTemplate, esId);
        } else {
            flag = deleteAndCreate(totalHit,houseIndexTemplate);
        }

        if (flag) {
            log.debug("Index success with houseId {}",houseId);
        }

        return flag;

    }
    /**
     * create
     */

    private boolean create(HouseIndexTemplate houseIndexTemplate){
        try {
            IndexResponse indexResponse = transportClient.prepareIndex(IndexEnum.INDEX_NAME.getValue(),IndexEnum.INDEX_TYPE.getValue())
            .setSource(objectMapper.writeValueAsBytes(houseIndexTemplate), XContentType.JSON).get();

            log.debug("Create index with house: " + houseIndexTemplate.getHouseId());

            if (indexResponse.status() == RestStatus.CREATED) {
                return true;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            log.error("Error index house " + houseIndexTemplate.getHouseId(), e);
            return false;
        }
        return false;
    }
    /**
     * update
     */
    private boolean update(HouseIndexTemplate houseIndexTemplate, String esId){

        try {
            UpdateResponse updateResponse = transportClient.prepareUpdate(IndexEnum.INDEX_NAME.getValue(),IndexEnum.INDEX_TYPE.getValue(), esId)
                    .setDoc(objectMapper.writeValueAsBytes(houseIndexTemplate), XContentType.JSON).get();

            log.debug("Update index with house: " + houseIndexTemplate.getHouseId());

            if (updateResponse.status() == RestStatus.OK) {
                return true;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            log.error("Error index house " + houseIndexTemplate.getHouseId(), e);
            return false;
        }
        return false;
    }
    /**
     * delete & create
     */
    private boolean deleteAndCreate(long total, HouseIndexTemplate houseIndexTemplate){
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE.newRequestBuilder(transportClient)
                .filter(QueryBuilders
                .termQuery(HouseIndexKey.HOUSE_ID.getValue(),houseIndexTemplate.getHouseId()))
                .source(IndexEnum.INDEX_NAME.getValue());

        log.debug("deleteAndCreate DeleteByQueryRequestBuilder {}"+builder);

        BulkByScrollResponse response = builder.get();

        long deleted = response.getDeleted();

        if (deleted != total) {
            log.warn("Need delete {} , but deleted {}",total, deleted);
            return false;
        }

        create(houseIndexTemplate);

        return true;

    }
    @Override
    public boolean remove(Long houseId) {

        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE.newRequestBuilder(transportClient)
                .filter(QueryBuilders
                        .termQuery(HouseIndexKey.HOUSE_ID.getValue(),houseId))
                .source(IndexEnum.INDEX_NAME.getValue());

        log.debug("remove DeleteByQueryRequestBuilder {}"+builder);

        BulkByScrollResponse response = builder.get();

        long deleted = response.getDeleted();

        log.debug("remove deleted {}",deleted);

        return true;
    }
}
