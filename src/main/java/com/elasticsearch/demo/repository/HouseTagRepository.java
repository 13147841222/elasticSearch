package com.elasticsearch.demo.repository;

import com.elasticsearch.demo.entity.HouseDetail;
import com.elasticsearch.demo.entity.HouseTag;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author zhumingli
 * @create 2018-08-29 下午10:36
 * @desc
 **/
public interface HouseTagRepository extends CrudRepository<HouseTag, Long> {
    List<HouseTag> findAllByHouseId(Long id);

    List<HouseTag> findAllByHouseIdIn(List<Long> houseIds);
}
