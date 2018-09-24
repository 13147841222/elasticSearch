package com.elasticsearch.demo.service.impl;

import com.elasticsearch.demo.service.ISmsService;
import com.elasticsearch.demo.service.ServiceResult;
import org.springframework.stereotype.Service;

/**
 * @author zhumingli
 * @create 2018-09-20 下午10:25
 * @desc
 **/
@Service
public class ISmsServiceImpl implements ISmsService {
    @Override
    public ServiceResult<String> sendSms(String telephone) {
        return null;
    }

    @Override
    public ServiceResult<String> getSmsCode(String telephone) {
        return null;
    }

    @Override
    public ServiceResult<String> removeSms(String telephone) {
        return null;
    }
}
