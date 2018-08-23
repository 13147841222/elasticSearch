package com.elasticsearch.demo.repository;

import com.elasticsearch.demo.entity.User;
import org.springframework.data.repository.CrudRepository;

/**
 * @author zhumingli
 * @create 2018-08-21 下午3:11
 * @desc
 **/

public interface UserRepository extends CrudRepository<User,Long> {

    /**
     * @param name
     * @return User
     */
    User findByName(String name);
}
