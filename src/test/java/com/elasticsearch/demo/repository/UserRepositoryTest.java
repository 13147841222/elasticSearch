package com.elasticsearch.demo.repository;

import com.elasticsearch.demo.DemoApplicationTests;
import com.elasticsearch.demo.entity.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class UserRepositoryTest extends DemoApplicationTests {

    @Autowired
    private UserRepository userRepository;

    private static final Long ID = 1L;
    @Test
    public void findTestOne(){
        User user = userRepository.findOne(ID);
        Assert.assertEquals(ID,user.getId());
    }

}