package com.elasticsearch.demo.web.dto;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.Column;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

/**
 * @author zhumingli
 * @create 2018-09-05 下午4:31
 * @desc
 **/
@Data
public class UserDTO {

    private Long id;

    private String name;

    private String email;

    private String phoneNumber;

    private Integer status;

    private Date lastLoginTime;

    private String avatar;


}
