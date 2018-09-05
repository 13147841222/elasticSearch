package com.elasticsearch.demo.service;

import com.elasticsearch.demo.entity.User;
import com.elasticsearch.demo.web.dto.UserDTO;

/**
 * @author zhumingli
 * @create 2018-08-22 下午11:33
 * @desc
 **/
public interface IUserService {

    User findUserByName(String name);

    ServiceResult<UserDTO> findById(Long adminId);
}
