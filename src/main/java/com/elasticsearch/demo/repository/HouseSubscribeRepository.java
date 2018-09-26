package com.elasticsearch.demo.repository;

import com.elasticsearch.demo.entity.HouseSubscribe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author zhumingli
 * @create 2018-09-25 下午11:26
 * @desc
 **/
public interface HouseSubscribeRepository extends PagingAndSortingRepository<HouseSubscribe, Long> {

    /**
     * @param houseId
     * @param loginUserId
     * @return
     */
    HouseSubscribe findByHouseIdAndUserId(Long houseId, Long loginUserId);

    /**
     * @param userId
     * @param value
     * @param pageable
     * @return
     */
    Page<HouseSubscribe> findByUserIdAndStatus(Long userId, int value, Pageable pageable);
}
