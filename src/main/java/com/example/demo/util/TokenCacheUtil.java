package com.example.demo.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class TokenCacheUtil {

    private static Logger logger = LoggerFactory.getLogger(TokenCacheUtil.class);

    private static LoadingCache<String,String> loadingCache = CacheBuilder.newBuilder().
            initialCapacity(100).maximumSize(500).expireAfterWrite(12, TimeUnit.MINUTES).build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) throws Exception {
                    return null;
                }
            });

    public static void setLoadingCache(String key,String value){
        loadingCache.put(key, value);
    }

    public static String getToken(String key){
        String token = null;
        try {
            token = loadingCache.get(key);
            loadingCache.invalidate(key);
        } catch (ExecutionException e) {
            logger.error("Guava缓存获取token异常",e);
        }
        return token;
    }
}
