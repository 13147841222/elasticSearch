package com.elasticsearch.demo.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @author zhumingli
 * @create 2018-08-22 下午11:47
 * @desc
 **/
@Entity
@Table(name = "role")
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String name;
}
