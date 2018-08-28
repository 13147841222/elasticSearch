package com.elasticsearch.demo.service.impl;

import com.elasticsearch.demo.DemoApplicationTests;
import com.elasticsearch.demo.service.IQiNiuService;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

import static org.junit.Assert.*;

public class IQiNiuServiceImplTest extends DemoApplicationTests {

    @Autowired
    private IQiNiuService qiNiuService;

    @Test
    public void uploadFile() {
        String fileName = "/Users/zhumingli/IdeaProjects/demo/tmp/屏幕快照 2018-08-23 下午11.17.30.png";
        File fiel = new File(fileName);
        Assert.assertTrue(fiel.exists());

        try {
            Response response = qiNiuService.uploadFile(fiel);
            Assert.assertTrue(response.isOK());
        } catch (QiniuException e) {
            e.printStackTrace();

        }

    }

    @Test
    public void uploadFile1() {
    }

    @Test
    public void delete() {
    }

    @Test
    public void afterPropertiesSet() {
    }
}