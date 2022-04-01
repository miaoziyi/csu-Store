package com.example.demo;

import com.example.demo.entity.User;
import com.example.demo.persistence.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void testUserMapper() {
        List<User> users = userMapper.selectList(null);
        users.forEach(System.out::println);
    }
}
