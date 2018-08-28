package com.elasticsearch.demo.service;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;

import java.io.File;
import java.io.InputStream;

/**
 * @author zhumingli
 * @create 2018-08-27 下午11:01
 * @desc 七牛云
 **/
public interface IQiNiuService {

    Response uploadFile(File file) throws QiniuException ;

    Response uploadFile(InputStream inputStream) throws QiniuException;

     Response delete(String key) throws QiniuException;

}
