package org.csu.csumall.utils;

import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
@Data
public class RedisUtil {
    @Resource
    private RedisTemplate redisTemplate;
}


