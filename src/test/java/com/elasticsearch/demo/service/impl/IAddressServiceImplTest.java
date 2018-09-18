package com.elasticsearch.demo.service.impl;

import com.elasticsearch.demo.DemoApplicationTests;
import com.elasticsearch.demo.service.IAddressService;
import com.elasticsearch.demo.service.ServiceResult;
import com.elasticsearch.demo.service.search.BaiduMapLocation;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class IAddressServiceImplTest extends DemoApplicationTests {

    @Autowired
    private IAddressService addressService;

    @Test
    public void getBaiduMapLocation() {
        String city = "北京";
        String address = "北京市丰台区西马场北里";

        ServiceResult<BaiduMapLocation> serviceResult = addressService.getBaiduMapLocation(city,address);

        Assert.assertTrue(serviceResult.isSuccess());



    }
}