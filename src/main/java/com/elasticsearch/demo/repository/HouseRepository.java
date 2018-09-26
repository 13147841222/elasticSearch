package com.elasticsearch.demo.repository;

import com.elasticsearch.demo.entity.House;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * @author zhumingli
 * @create 2018-08-29 下午10:31
 * @desc
 **/
public interface HouseRepository extends PagingAndSortingRepository<House, Long> , JpaSpecificationExecutor<House> {

    /**
     * @param id
     * @param cover
     */
    @Modifying
    @Query("update House as house set house.cover = :cover where house.id = :id")
    void updateCover(@Param(value = "id") Long id, @Param(value = "cover") String cover);

    /**
     * @param id
     * @param status
     */
    @Modifying
    @Query("update House as house set house.status = :status where house.id = :id")
    void updateStatus(@Param(value = "id") Long id, @Param(value = "status") int status);

    /**
     * @param houseId
     */
    @Modifying
    @Query("update House as house set house.watchTimes = house.watchTimes + 1 where house.id = :houseId")
    void updateWatchTimes(@Param(value = "id")Long houseId);
}
