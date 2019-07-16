package com.leofee.springbootlearningredisson;

import com.alibaba.fastjson.JSON;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * @author leofee
 * @date 2019/7/16
 */
@Component
public class RedissonHelper {

    @Autowired
    private RedissonClient redissonClient;


    public <T, ID> T getCachedObject(String cacheKey, Class<T> clazz, ID id, Function<ID, T> function) {
        RBucket<String> bucket = redissonClient.getBucket(cacheKey);
        if (bucket.get() == null) {
            return function.apply(id);
        }
        return JSON.parseObject(bucket.get(), clazz);
    }

    public void cacheObject(String cacheKey, Object src) {
        RBucket<String> bucket = redissonClient.getBucket(cacheKey);
        bucket.set(JSON.toJSONString(src));
    }

    public <T, ID> T getCachedObjectOrElseFromDB(String cacheKey, Class<T> clazz, ID id, Function<ID, T> function) {
        T result;
        RBucket<String> bucket = redissonClient.getBucket(cacheKey);
        if (bucket.get() == null) {
            result = function.apply(id);
        } else {
            result = JSON.parseObject(bucket.get(), clazz);
        }
        cacheObject(cacheKey, result);
        return result;
    }
}
