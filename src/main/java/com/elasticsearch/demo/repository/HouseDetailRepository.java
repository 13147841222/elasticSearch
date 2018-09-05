package com.elasticsearch.demo.repository;

import com.elasticsearch.demo.entity.HouseDetail;
import org.springframework.data.repository.CrudRepository;
import org.thymeleaf.expression.Ids;

import java.util.List;

/**
 * @author zhumingli
 * @create 2018-08-29 下午10:33
 * @desc
 **/
public interface HouseDetailRepository extends CrudRepository<HouseDetail, Long> {

    /**
     * @param id
     * @return
     */
    HouseDetail findByHouseId(Long id);

    /**
     * @param houseIds
     * @return
     */
    List<HouseDetail> findAllByHouseIdIn(List<Long> houseIds);
}
