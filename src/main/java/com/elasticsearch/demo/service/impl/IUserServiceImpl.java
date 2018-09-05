package com.elasticsearch.demo.service.impl;

import com.elasticsearch.demo.entity.Role;
import com.elasticsearch.demo.entity.User;
import com.elasticsearch.demo.repository.RoleRepository;
import com.elasticsearch.demo.repository.UserRepository;
import com.elasticsearch.demo.service.IUserService;
import com.elasticsearch.demo.service.ServiceResult;
import com.elasticsearch.demo.web.dto.UserDTO;
import com.sun.prism.paint.Gradient;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
}
