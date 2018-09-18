package com.elasticsearch.demo.service.impl;


import com.elasticsearch.demo.base.HouseSort;
import com.elasticsearch.demo.base.HouseSuggest;
import com.elasticsearch.demo.base.RentValueBlock;
import com.elasticsearch.demo.emuns.HouseStatusEnum;
import com.elasticsearch.demo.emuns.IndexEnum;
import com.elasticsearch.demo.emuns.LevelEnum;
import com.elasticsearch.demo.entity.House;
import com.elasticsearch.demo.entity.HouseDetail;
import com.elasticsearch.demo.entity.HouseTag;
import com.elasticsearch.demo.entity.SupportAddress;
import com.elasticsearch.demo.repository.HouseDetailRepository;
import com.elasticsearch.demo.repository.HouseRepository;
import com.elasticsearch.demo.repository.HouseTagRepository;
import com.elasticsearch.demo.repository.SupportAddressRepository;
import com.elasticsearch.demo.service.IAddressService;
import com.elasticsearch.demo.service.ServiceMultiResult;
import com.elasticsearch.demo.service.ServiceResult;
import com.elasticsearch.demo.service.search.*;

import com.elasticsearch.demo.web.dto.HouseBucketDTO;
import com.elasticsearch.demo.web.form.RentSearch;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeAction;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequestBuilder;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;

import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.elasticsearch.demo.base.RentValueBlock.ALL;

/**
 * @author zhumingli
 * @create 2018-09-06 上午10:51
 * @desc a
 **/
@Service
@Slf4j
public class ISearchServiceImpl implements ISearchService {

    private static final String INDEX_TOPIC =  "house_build";

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private HouseDetailRepository houseDetailRepository;

    @Autowired
    private IAddressService addressService;

    @Autowired
    private SupportAddressRepository supportAddressRepository;

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
            log.info("houseDetail is null with houseId {}",houseId);
        }

        modelMapper.map(houseDetail, houseIndexTemplate);

        SupportAddress city = supportAddressRepository.findByEnNameAndLevel(house.getCityEnName(), LevelEnum.CITY.getValue());

        SupportAddress region = supportAddressRepository.findByEnNameAndLevel(house.getRegionEnName(), LevelEnum.REGION.getValue());


        String address = city.getEnName() + region.getEnName() + house.getStreet() + house.getDistrict() + houseDetail.getDetailAddress() ;

        ServiceResult<BaiduMapLocation> locatioin = addressService.getBaiduMapLocation(city.getEnName(), address);

        if(!locatioin.isSuccess()){
            index(message.getHouseId(), message.getRetry() + 1);
            return;
        }

        houseIndexTemplate.setLocation(locatioin.getResult());

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
        if (!updateSuggest(houseIndexTemplate)){
            return false;
        }
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
        if (!updateSuggest(houseIndexTemplate)){
            return false;
        }

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

        RentValueBlock area = RentValueBlock.matchArea(rentSearch.getAreaBlock());

        if (!RentValueBlock.ALL.equals(area)){
            RangeQueryBuilder rangeQueryBuilder =QueryBuilders.rangeQuery(HouseIndexKey.AREA.getValue());
            if (area.getMax() > 0) {
                rangeQueryBuilder.lte(area.getMax());
            }
            if (area.getMin() > 0) {
                rangeQueryBuilder.gte(area.getMin());
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        RentValueBlock price = RentValueBlock.matchPrice(rentSearch.getPriceBlock());

        if (!RentValueBlock.ALL.equals(price)){
            RangeQueryBuilder rangeQueryBuilder =QueryBuilders.rangeQuery(HouseIndexKey.PRICE.getValue());
            if (price.getMax() > 0) {
                rangeQueryBuilder.lte(price.getMax());
            }
            if (price.getMin() > 0) {
                rangeQueryBuilder.gte(price.getMin());
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        if (rentSearch.getDirection() > 0) {
            boolQueryBuilder.filter(
                    QueryBuilders.termQuery(HouseIndexKey.DESCRIPTION.getValue(),rentSearch.getDirection())
            );
        }

        if (rentSearch.getRentWay() > -1) {
            boolQueryBuilder.filter(
                    QueryBuilders.termQuery(HouseIndexKey.RENT_WAY.getValue(),rentSearch.getRentWay())
            );
        }

        boolQueryBuilder.must(QueryBuilders.multiMatchQuery(rentSearch.getKeywords(),
                HouseIndexKey.TITLE.getValue(),
                HouseIndexKey.TRAFFIC.getValue(),
                HouseIndexKey.DISTRICT.getValue(),
                HouseIndexKey.ROUND_SERVICE.getValue(),
                HouseIndexKey.SUBWAY_LINE_NAME.getValue(),
                HouseIndexKey.SUBWAY_STATION_NAME.getValue()
        ));



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

    @Override
    public ServiceResult<List<String>> suggest(String prefix) {
        CompletionSuggestionBuilder suggestionBuilder = SuggestBuilders.completionSuggestion("suggest").prefix(prefix).size(5);

        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("autocomplete",suggestionBuilder);
        SearchRequestBuilder searchRequestBuilder = transportClient.prepareSearch(IndexEnum.INDEX_NAME.getValue()).
                setTypes(IndexEnum.INDEX_TYPE.getValue())
                .suggest(suggestBuilder);

        log.debug("searchRequestBuilder : "+ searchRequestBuilder.toString());


        SearchResponse searchResponse = searchRequestBuilder.get();
        Suggest suggest = searchResponse.getSuggest();
        Suggest.Suggestion suggestion = suggest.getSuggestion("autocomplete");

        int max = 0;
        Set<String> set = new HashSet<>();

        for (Object entry : suggestion.getEntries()) {
            if (entry instanceof CompletionSuggestion.Entry){
                CompletionSuggestion.Entry item = (CompletionSuggestion.Entry)entry;
                if (item.getOptions().isEmpty()){
                    continue;
                }

                for (CompletionSuggestion.Entry.Option option : item.getOptions()) {
                    if (set.contains(option.getText().string())){
                        continue;
                    }
                    set.add(option.getText().string());
                    max++;
                }
            }

            if (max > 5) {
                break;
            }
        }

        List<String> suggests = Lists.newArrayList( set.toArray(new String[]{}));

        return ServiceResult.of(suggests);
    }

    @Override
    public ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName, String district) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().
                filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME.getValue(),cityEnName))
                .filter(QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME.getValue(),regionEnName))
                .filter(QueryBuilders.termQuery(HouseIndexKey.DISTRICT.getValue(),district));

        SearchRequestBuilder searchRequestBuilder = transportClient.prepareSearch(IndexEnum.INDEX_NAME.getValue()).
                setTypes(IndexEnum.INDEX_TYPE.getValue()).
                setQuery(boolQueryBuilder).
                addAggregation(AggregationBuilders.terms(HouseIndexKey.AGG_DISTRICT.getValue()).field(HouseIndexKey.DISTRICT.getValue())).setSize(0);

        log.debug("searchRequestBuilder : " + searchRequestBuilder.toString());
        SearchResponse searchResponse = searchRequestBuilder.get();

        if (searchResponse.status() == RestStatus.OK ) {
            Terms terms =searchResponse.getAggregations().get(HouseIndexKey.AGG_DISTRICT.getValue());
            if (terms.getBuckets() != null && terms.getBuckets().isEmpty() ) {
                return ServiceResult.of(terms.getBucketByKey(district).getDocCount());
            }
        } else {
            log.warn("Failed to Aggregate for " + HouseIndexKey.AGG_DISTRICT.getValue());

        }

        return ServiceResult.of(0L);
    }

    @Override
    public ServiceMultiResult<HouseBucketDTO> mapAggregate(String cityEnName) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME.getValue(),cityEnName));

        AggregationBuilder aggregationBuilder =AggregationBuilders.terms(HouseIndexKey.AGG_REGION.getValue()).field(HouseIndexKey.REGION_EN_NAME.getValue() );

        SearchRequestBuilder requestBuilder = transportClient.prepareSearch(IndexEnum.INDEX_NAME.getValue())
                .setTypes(IndexEnum.INDEX_TYPE.getValue())
                .setQuery(boolQueryBuilder)
                .addAggregation(aggregationBuilder);

        log.debug("requestBuilder : " + requestBuilder.toString());

        SearchResponse response = requestBuilder.get();

        List<HouseBucketDTO> bucketDTOS = new ArrayList<>();

        if (response.status() !=  RestStatus.OK) {
            log.warn("Aggregation status is not ok for " + aggregationBuilder);
            return new ServiceMultiResult<>(0,bucketDTOS);
        }

        Terms terms = response.getAggregations().get(HouseIndexKey.AGG_REGION.getValue());

        for (Terms.Bucket bucket : terms.getBuckets()) {
            bucketDTOS.add(new HouseBucketDTO(bucket.getKeyAsString(), bucket.getDocCount()));
        }

        return new ServiceMultiResult<>(response.getHits().getTotalHits(), bucketDTOS);
    }

    private boolean updateSuggest(HouseIndexTemplate houseIndexTemplate){
        AnalyzeRequestBuilder requestBuilder = new AnalyzeRequestBuilder(transportClient,
                AnalyzeAction.INSTANCE,
                IndexEnum.INDEX_NAME.getValue(),
                houseIndexTemplate.getTitle(),
                houseIndexTemplate.getDistrict(),
                houseIndexTemplate.getLayoutDesc(),
                houseIndexTemplate.getRoundService(),
                houseIndexTemplate.getDescription(),
                houseIndexTemplate.getSubwayLineName(),
                houseIndexTemplate.getSubwayStationName());

        requestBuilder.setAnalyzer("ik_smart");
        AnalyzeResponse response = requestBuilder.get();

        List<AnalyzeResponse.AnalyzeToken> analyzeTokens = response.getTokens();


        if (analyzeTokens == null) {
            log.warn("Can not Analyzer token for house : "+houseIndexTemplate.getHouseId());
            return false;
        }

        List<HouseSuggest> suggests = new ArrayList<>();
        for (AnalyzeResponse.AnalyzeToken token : analyzeTokens) {
            /**
             * 排除数字类型 || 小于2个字符的分词结果
             */
            if ("<NUM>".equals(token.getType()) || token.getTerm().length() < 2){
                continue;
            }

            HouseSuggest suggest = new HouseSuggest();
            suggest.setInput(token.getTerm());
            suggests.add(suggest);

        }

        /**
         * 定制化数据
         */
        HouseSuggest suggest = new HouseSuggest();
        suggest.setInput(houseIndexTemplate.getDistrict());
        suggests.add(suggest);

        houseIndexTemplate.setSuggests(suggests);


        return true;

    }
}
