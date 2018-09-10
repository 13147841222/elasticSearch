package com.elasticsearch.demo.service.impl;


import com.elasticsearch.demo.base.HouseSort;
import com.elasticsearch.demo.emuns.IndexEnum;
import com.elasticsearch.demo.entity.House;
import com.elasticsearch.demo.entity.HouseDetail;
import com.elasticsearch.demo.entity.HouseTag;
import com.elasticsearch.demo.repository.HouseDetailRepository;
import com.elasticsearch.demo.repository.HouseRepository;
import com.elasticsearch.demo.repository.HouseTagRepository;
import com.elasticsearch.demo.service.ServiceMultiResult;
import com.elasticsearch.demo.service.search.HouseIndexKey;
import com.elasticsearch.demo.service.search.HouseIndexMessage;
import com.elasticsearch.demo.service.search.HouseIndexTemplate;
import com.elasticsearch.demo.service.search.ISearchService;

import com.elasticsearch.demo.web.form.RentSearch;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;

import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.elasticsearch.demo.base.RentValueBlock.ALL;

/**
 * @author zhumingli
 * @create 2018-09-06 上午10:51
 * @desc
 **/
@Service
@Slf4j
public class ISearchServiceImpl implements ISearchService {

    public static final String INDEX_TOPIC =  "house_build";

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

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = INDEX_TOPIC)
    private void handleMessage(String content){
        try {
            HouseIndexMessage houseIndexMessage =objectMapper.readValue(content, HouseIndexMessage.class);

            switch (houseIndexMessage.getOperation()){
                case HouseIndexMessage.INDEX:
                    this.createOrUpdateIndex(houseIndexMessage);
                    break;
                case HouseIndexMessage.REMOVE:
                    this.removeIndex(houseIndexMessage);
                    break;
                default:
                    log.warn("Not content {}",content);
                    break;
            }
        } catch (IOException e) {
            log.error("Cannot parse json for {}" ,content);
        }


    }

    private void createOrUpdateIndex(HouseIndexMessage message){

        Long houseId = message.getHouseId();

        House house = houseRepository.findOne(houseId);
        if (house == null) {
            log.error("Index house {} dose not exist!",houseId);
            index(houseId, message.getRetry() + 1);
            return;
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

    }

    private void removeIndex(HouseIndexMessage message){

        Long houseId = message.getHouseId();

        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE.newRequestBuilder(transportClient)
                .filter(QueryBuilders
                        .termQuery(HouseIndexKey.HOUSE_ID.getValue(),houseId))
                .source(IndexEnum.INDEX_NAME.getValue());

        log.debug("remove DeleteByQueryRequestBuilder {}"+builder);

        BulkByScrollResponse response = builder.get();

        long deleted = response.getDeleted();

        log.debug("remove deleted {}",deleted);

        if (deleted <= 0){
            this.remove(houseId, message.getRetry() + 1);
        }
    }



    @Override
    public void index(Long houseId) {

        this.index(houseId, 0);

    }

    public void index(Long houseId, int retry) {
        if (retry > HouseIndexMessage.MAX_RETRY){
            log.error("Retry index times over 3 for hosue :" + houseId + " Please check it!");
            return;
        }

        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.INDEX, retry);
        try {
            kafkaTemplate.send(IndexEnum.INDEX_TOPIC.getValue(), objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            log.error("json encode error for " + message);
        }

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
    public void remove(Long houseId) {
        this.remove(houseId, 0);
    }

    private void remove(Long houseId , int retry){
        if (retry > HouseIndexMessage.MAX_RETRY){
            log.error("Retry remove times over 3 for hosue :" + houseId + " Please check it!");
            return;
        }

        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.REMOVE, retry);
        try {
            kafkaTemplate.send(IndexEnum.INDEX_TOPIC.getValue(), objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            log.error("json encode error for " + message);
        }

    }

    @Override
    public ServiceMultiResult<Long> query(RentSearch rentSearch) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        boolQueryBuilder.filter(
                QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME.getValue(),rentSearch.getCityEnName())
        );
        if (rentSearch.getRegionEnName() != null && !ALL.getKey().equals(rentSearch.getRegionEnName())){
            boolQueryBuilder.filter(
                    QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME.getValue(),rentSearch.getRegionEnName())
            );
        }

        SearchRequestBuilder searchRequestBuilder = transportClient.prepareSearch(IndexEnum.INDEX_NAME.getValue())
                .setTypes(IndexEnum.INDEX_TYPE.getValue())
                .setQuery(boolQueryBuilder)
                .addSort(HouseSort.getSortKey(rentSearch.getOrderBy()), SortOrder.fromString(rentSearch.getOrderDirection()))
                .setFrom(rentSearch.getStart())
                .setSize(rentSearch.getSize());

        log.debug("searchRequestBuilder {}",searchRequestBuilder.toString());

        List<Long> houseIds = new ArrayList<>();
        SearchResponse searchResponse = searchRequestBuilder.get();

        if (searchResponse.status() != RestStatus.OK ) {
            log.warn("Search status is no ok for " + boolQueryBuilder);
            return new ServiceMultiResult<>(0,houseIds);
        }

        for (SearchHit hit : searchResponse.getHits()) {
            houseIds.add(Longs.tryParse( String.valueOf( hit.getSourceAsMap().get(HouseIndexKey.HOUSE_ID.getValue()))));
        }


        return new ServiceMultiResult<>(searchResponse.getHits().getTotalHits(), houseIds);
    }
}
