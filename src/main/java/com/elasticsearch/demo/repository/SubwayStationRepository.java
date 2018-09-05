package com.elasticsearch.demo.repository;

import java.util.List;

import com.elasticsearch.demo.entity.SubwayStation;
import org.springframework.data.repository.CrudRepository;


/**
 * @author zhumingli
 */
public interface SubwayStationRepository extends CrudRepository<SubwayStation, Long> {
    List<SubwayStation> findAllBySubwayId(Long subwayId);
}
