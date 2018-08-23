package com.elasticsearch.demo.service;

import com.elasticsearch.demo.entity.User;

/**
 * @author zhumingli
 * @create 2018-08-22 下午11:33
 * @desc
 **/
public interface IUserService {

    User findUserByName(String name);
}
