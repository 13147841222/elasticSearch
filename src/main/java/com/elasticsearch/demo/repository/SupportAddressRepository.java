package com.elasticsearch.demo.repository;

import com.elasticsearch.demo.entity.SupportAddress;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author zhumingli
 * @create 2018-08-28 下午10:08
 * @desc
 **/
public interface SupportAddressRepository extends CrudRepository<SupportAddress, Long> {

    /**
     *
     * @param level
     * @return
     */
    List<SupportAddress> findAllByLevel(String level);

    SupportAddress findByEnNameAndLevel(String enName, String level);

    SupportAddress findByEnNameAndBelongTo(String enName, String belongTo);

    List<SupportAddress> findAllByLevelAndBelongTo(String level, String belongTo);

}
