package com.leofee.springbootlearningredisson.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author leofee
 * @date 2019/7/16
 */
@Slf4j
@Component
public class RedissonCacheUtils {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 根据 KEY 从缓存中获取 value
     * <p>
     * 缓存未命中则 {@code return null}
     *
     * @param cacheKey 缓存的 key
     * @param <T>      返回值类型
     * @return 缓存的 value, 缓存未命中则 {@code return null}
     */
    public <T> T getCachedObjectOrElseNull(String cacheKey) {
        RBucket<T> bucket = redissonClient.getBucket(cacheKey);
        if (bucket.get() == null) {
            return null;
        }
        return bucket.get();
    }

    /**
     * 根据 KEY 从缓存中获取value
     *
     * <p>
     * 如果缓存未命中则执行 supplier 逻辑进行数据库查询，
     * 并且将查询的数据放入缓存中
     *
     * @param cacheKey 缓存的key
     * @param supplier 未命中缓存后，执行从DB中查询的逻辑
     * @param <T>      返回值类型
     * @return 缓存的value, 缓存未命中则执行 supplier 返回
     */
    public <T> T getCachedObjectOrElseGet(String cacheKey, @NotNull Supplier<T> supplier) {
        T result;
        RBucket<T> bucket = redissonClient.getBucket(cacheKey);
        if (bucket.get() == null) {
            result = supplier.get();
            // 加入到缓存中
            if (result != null) {
                cacheObject(cacheKey, result);
            }
        } else {
            result = bucket.get();
        }
        return result;
    }

    /**
     * 根据 KEY 从缓存中获取List
     *
     * <p>
     * 如果缓存未命中则执行 supplier 逻辑进行数据库查询，
     * 并且将查询的数据放入缓存中
     *
     * @param cacheKey 缓存的key
     * @param <T>      返回值类型
     * @return 缓存的List, 缓存未命中则执行 supplier 返回
     */
    public <T> List<T> getCachedListOrElseGet(String cacheKey, @NotNull Supplier<List<T>> supplier) {
        List<T> result;
        RBucket<List<T>> bucket = redissonClient.getBucket(cacheKey);
        if (bucket.get() == null || bucket.get().size() == 0) {
            result = supplier.get();
            // 加入到缓存中
            if (!CollectionUtils.isEmpty(result)) {
                cacheObject(cacheKey, result);
            }
        } else {
            result = bucket.get();
        }
        return result;
    }

    /**
     * 将 value 放入缓存中
     *
     * @param cacheKey 缓存的key
     * @param supplier 获取缓存的value的逻辑
     */
    public <T> void cacheObject(String cacheKey, Supplier<T> supplier) {
        T value = supplier.get();
        if (value == null) {
            if (log.isInfoEnabled()) {
                log.info("cacheKey [{}] , value 为 null !", cacheKey);
            }
            return;
        }
        cacheObject(cacheKey, value);
    }

    /**
     * 将 value 放入缓存中
     *
     * @param cacheKey 缓存的key
     * @param value    缓存的value
     */
    public void cacheObject(String cacheKey, Object value) {
        if (value == null) {
            if (log.isInfoEnabled()) {
                log.info("cacheKey [{}] , value 为 null !", cacheKey);
            }
            return;
        }
        RBucket<Object> bucket = redissonClient.getBucket(cacheKey);
        if (log.isInfoEnabled()) {
            log.info("cacheKey:[{}], value:[{}]", cacheKey, JSON.toJSONString(value));
        }
        bucket.set(value);
    }


    /**
     * 根据 key 删除缓存
     *
     * @param cacheKey 缓存key
     */
    public void deleteCachedObject(String cacheKey) {
        RBucket<Object> bucket = redissonClient.getBucket(cacheKey);
        bucket.delete();
    }
}
