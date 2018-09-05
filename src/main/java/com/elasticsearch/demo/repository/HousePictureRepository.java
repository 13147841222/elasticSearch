package com.elasticsearch.demo.repository;

import com.elasticsearch.demo.entity.HousePicture;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author zhumingli
 * @create 2018-08-29 下午10:35
 * @desc
 **/
public interface HousePictureRepository extends CrudRepository<HousePicture , Long> {

    List<HousePicture> findAllByHouseId(Long id);
}
