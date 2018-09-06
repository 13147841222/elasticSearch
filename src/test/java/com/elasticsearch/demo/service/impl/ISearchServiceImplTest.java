package com.elasticsearch.demo.service.impl;

import com.elasticsearch.demo.DemoApplicationTests;
import com.elasticsearch.demo.service.search.ISearchService;
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
        boolean flag = searchService.index(HOUSE_ID);
        Assert.assertTrue(flag);
    }

    @Test
    public void remove() {
        boolean flag = searchService.remove(HOUSE_ID);
        Assert.assertTrue(flag);
    }
}