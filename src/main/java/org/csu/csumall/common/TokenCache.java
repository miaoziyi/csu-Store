package org.csu.csumall.common;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TokenCache {

    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

    public static final String TOKEN_PREFIX = "token_";

    //LRU算法淘汰未使用的Token,本地缓存最大值10000
    private static LoadingCache<String,String> localCache = CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                //默认数据的加载实现，当调用GET取值的时候，若key无对应的值，则调用此方法
                @Override
                public String load(String s) throws Exception {
                    return "null";
                }
            } );

    public static void setKey(String key, String value)
    {
        localCache.put(key,value);
    }

    public static String getKey(String key)
    {
        String value = null;
        try {
            value = localCache.get(key);
            if(  "null".equals(value) )
            {
                return null;
            }
            return value;
        }catch (Exception e)
        {
            logger.error("localCache get error", e);
        }
        return null;
    }

}
