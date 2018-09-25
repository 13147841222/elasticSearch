package com.elasticsearch.demo.service.impl;

import com.elasticsearch.demo.base.LoginUserUtil;
import com.elasticsearch.demo.entity.Role;
import com.elasticsearch.demo.entity.User;
import com.elasticsearch.demo.repository.RoleRepository;
import com.elasticsearch.demo.repository.UserRepository;
import com.elasticsearch.demo.service.IUserService;
import com.elasticsearch.demo.service.ServiceResult;
import com.elasticsearch.demo.web.dto.UserDTO;
import com.google.common.collect.Lists;
import com.sun.prism.paint.Gradient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author zhumingli
 * @create 2018-08-22 下午11:34
 * @desc
 **/
@Service
@Slf4j
public class IUserServiceImpl implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ModelMapper modelMapper;

    private final Md5PasswordEncoder md5PasswordEncoder = new Md5PasswordEncoder();

    @Override
    public User findUserByName(String name) {
        log.info("IUserServiceImpl==>findUserByName: start==> name: "+name);
        User user = userRepository.findByName(name);
        log.info("IUserServiceImpl==>findUserByName==>user:{}",user);
        if(user == null){
            return null;
        }
        List<Role> roleList = roleRepository.findRolesByUserId(user.getId());
        if(roleList.isEmpty()){
            throw new DisabledException("权限非法");
        }
        List<GrantedAuthority> authorityList = new ArrayList<>();
        roleList.forEach(role -> authorityList.add(new SimpleGrantedAuthority("ROLE_"+role.getName())));
        user.setAuthorityList(authorityList);
        log.info("IUserServiceImpl==>findUserByName: end");
        return user;
    }

    @Override
    public ServiceResult<UserDTO> findById(Long adminId) {
         User user = userRepository.findOne(adminId);
         if(user == null){
             return ServiceResult.notFound();
         }
         UserDTO userDTO = modelMapper.map(user, UserDTO.class);



        return ServiceResult.of(userDTO);
    }

    @Override
    public User findUserByTelephone(String telephone) {
        User user = userRepository.findUserByPhoneNumber(telephone);
        if (user == null) {
            return null;
        }
        List<Role> roleList = roleRepository.findRolesByUserId(user.getId());
        if (roleList.isEmpty() || roleList == null) {
            throw new DisabledException("权限非法");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        roleList.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName())));
        user.setAuthorityList(authorities);
        return user;
    }

    @Override
    @Transactional
    public User addUserByPhone(String telephone) {
        User user = new User();
        user.setPhoneNumber(telephone);
        user.setName(telephone.substring(0,3) + "****" + telephone.substring(7,11));
        Date date = new Date();
        user.setCreateTime(date);
        user.setLastLoginTime(date);
        user.setLastUpdateTime(date);
        User result = userRepository.save(user);
        Role role = new Role();
        role.setName("USER");
        role.setUserId(result.getId());
        roleRepository.save(role);
        result.setAuthorityList(Lists.newArrayList(new SimpleGrantedAuthority("ROLE_USER")));
        return result;
    }

    @Override
    public ServiceResult modifyUserProfile(String profile, String value) {
        Long id = LoginUserUtil.getLoginUserId();
        switch (profile) {
            case "name":
                userRepository.updateUsername(id,value);
                break;
            case "email":
                userRepository.updateEmail(id,value);
                break;
            case "password":
                userRepository.updatePassword(id,md5PasswordEncoder.encodePassword(value, id));
                break;
            default:
                return new ServiceResult(false,"修改失败");
        }
        return ServiceResult.success();
    }
}
