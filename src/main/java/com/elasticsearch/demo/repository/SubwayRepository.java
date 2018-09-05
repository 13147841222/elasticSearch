package com.elasticsearch.demo.repository;

import java.util.List;

import com.elasticsearch.demo.entity.Subway;
import org.springframework.data.repository.CrudRepository;


/**
 * @author zhumingli
 */
public interface SubwayRepository extends CrudRepository<Subway, Long>{
    List<Subway> findAllByCityEnName(String cityEnName);
}
