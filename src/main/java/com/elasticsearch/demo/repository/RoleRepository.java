package com.elasticsearch.demo.repository;

import com.elasticsearch.demo.entity.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author zhumingli
 * @create 2018-08-23 上午8:30
 * @desc 角色数据DAO
 **/
public interface RoleRepository extends CrudRepository<Role, Long> {
    List<Role> findRolesByUserId(Long id);
}
