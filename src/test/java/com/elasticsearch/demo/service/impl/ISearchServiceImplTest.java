package com.elasticsearch.demo.service.impl;

import com.elasticsearch.demo.DemoApplicationTests;
import com.elasticsearch.demo.service.ServiceMultiResult;
import com.elasticsearch.demo.service.search.ISearchService;
import com.elasticsearch.demo.web.form.RentSearch;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class ISearchServiceImplTest extends DemoApplicationTests {

    @Autowired
    private ISearchService searchService;

    private static final Long HOUSE_ID = 15L;
    @Test
    public void index() {
        searchService.index(HOUSE_ID);
    }

    @Test
    public void remove() {
        searchService.remove(HOUSE_ID);
    }

    @Test
    public void query(){
        RentSearch rentSearch = new RentSearch();
        rentSearch.setCityEnName("bj");
        rentSearch.setStart(0);
        rentSearch.setSize(10);

        ServiceMultiResult<Long> serviceMultiResult = searchService.query(rentSearch);

        Assert.assertNotEquals(0, serviceMultiResult.getResultSize());
    }
}