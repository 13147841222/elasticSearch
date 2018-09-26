package com.elasticsearch.demo.repository;

import com.elasticsearch.demo.emuns.HouseSubscribeStatusEnum;
import com.elasticsearch.demo.entity.HouseSubscribe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

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

    /**
     * 通过管理员ID和状态查询所有预约
     * @param userId
     * @param inOrderList
     * @return
     */
    Page<HouseSubscribe> findAllByAdminIdAndStstus(Long userId, HouseSubscribeStatusEnum inOrderList, Pageable pageable);

    /**
     *
     * @param adminId
     * @param houseId
     * @return
     */
    HouseSubscribe findAllByAdminIdAndHouseId(Long adminId, Long houseId);

    @Modifying
    @Query("update HouseSubscribe as houseSubscribe set houseSubscribe.status = :status where houseSubscribe.id = :id")
    void updateStatus(@Param(value = "id") Long id, @Param(value = "status") int status);
}
