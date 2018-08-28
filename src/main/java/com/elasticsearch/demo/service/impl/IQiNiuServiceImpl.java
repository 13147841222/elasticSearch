package com.elasticsearch.demo.service.impl;

import com.elasticsearch.demo.service.IQiNiuService;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;

/**
 * @author zhumingli
 * @create 2018-08-27 下午11:08
 * @desc
 **/
@Service
@Slf4j
public class IQiNiuServiceImpl implements IQiNiuService , InitializingBean {

    @Autowired
    private UploadManager uploadManager;

    @Autowired
    private BucketManager bucketManager;

    @Autowired
    private Auth auth;

    @Value("${qiniu.Bucket}")
    private String bucket;

    private StringMap putPolicy;

    @Override
    public Response uploadFile(File file) throws QiniuException {
        Response result = this.uploadManager.put(file,null,getUploadToken());
        int retry = 0;
        while (result.needRetry() && retry < 3){
            result = this.uploadManager.put(file,null,getUploadToken());
            retry++;
        }
        return result;
    }

    @Override
    public Response uploadFile(InputStream inputStream) throws QiniuException {
        Response result = this.uploadManager.put(inputStream,null,getUploadToken(),null,null);
        int retry = 0;
        while (result.needRetry() && retry < 3){
            result = this.uploadManager.put(inputStream,null,getUploadToken(),null,null);
            retry++;
        }
        return result;
    }

    @Override
    public Response delete(String key) throws QiniuException {
        Response result = bucketManager.delete(bucket,key);
        int retry = 0;
        while (result.needRetry() && retry < 3) {
            result = bucketManager.delete(bucket,key);
            retry++;
        }
        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.putPolicy = new StringMap();
        putPolicy.put("returnBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"width\":$(imageInfo.width),\"height\":$(imageInfo.height)}");

    }

    /**
     * 获取上传凭证
     * @return
     */
    private String getUploadToken(){
        return this.auth.uploadToken(bucket,null,3600,putPolicy);
    }
}
