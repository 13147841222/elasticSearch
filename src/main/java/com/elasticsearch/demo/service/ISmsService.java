package com.elasticsearch.demo.service;

/**
 * @author zhumingli
 * @create 2018-09-20 下午10:21
 * @desc 短信验证码
 **/

public interface ISmsService {

    /**
     * 发送验证码到指定手机  并 缓存请求10分钟 以及 请求间隔 1分钟
     * @param telephone
     * @return
     */
    ServiceResult<String> sendSms(String telephone);

    /**
     * 获取缓存中的验证码
     * @param telephone
     * @return
     */
    ServiceResult<String> getSmsCode(String telephone);

    /**
     * 移除指定手机号的验证码缓存
     * @param telephone
     * @return
     */
    ServiceResult<String> removeSms(String telephone);
}
