package com.elasticsearch.demo.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @author zhumingli
 * @create 2018-08-28 下午9:55
 * @desc
 **/
@Entity
@Data
@Table(name= "support_address")
public class SupportAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 上一级行政单位
     */
    @Column(name = "belong_to")
    private String belongTo;

    /**
     *
     */
    @Column(name = "en_name")
    private String enName;

    /**
     *
     */
    @Column(name = "cn_name")
    private String cnName;

    /**
     * 行政级别
     */
    private String level;


}
