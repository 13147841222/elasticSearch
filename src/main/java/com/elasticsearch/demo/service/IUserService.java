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

    /**
     * 根据电话号码查找用户
     * @param telephone
     * @return
     */
    User findUserByTelephone(String telephone);


    /**
     * 通过手机号添加用户
     * @param telephone
     * @return
     */
    User addUserByPhone(String telephone);

    /**
     * 修改用户信息
     * @param profile 属性
     * @param value 值
     * @return
     */
    ServiceResult modifyUserProfile(String profile, String value);
}
