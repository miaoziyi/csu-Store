package com.example.demo.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.User;
import org.springframework.stereotype.Repository;

@Repository("userMapper")
public interface UserMapper extends BaseMapper<User> {
}
