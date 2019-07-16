package com.leofee.springbootlearningredisson;

import com.alibaba.fastjson.JSON;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author leofee
 * @date 2019/7/16
 */
@Component
public class RedissonHelper {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 根据 KEY 从缓存中获取 value
     * <b>缓存未命中则 {@code return null}</b>
     *
     * @param cacheKey 缓存的 key
     * @param clazz    需要返回的对象类型
     * @param <T>      返回值类型
     * @return 缓存的 value, 缓存未命中则 {@code return null}
     */
    public <T> T getCachedObjectOrElseGetNull(String cacheKey, Class<T> clazz) {
        RBucket<String> bucket = redissonClient.getBucket(cacheKey);
        if (bucket.get() == null) {
            return null;
        }
        return JSON.parseObject(bucket.get(), clazz);
    }

    /**
     * 根据 KEY 从缓存中获取 value
     * <p> 缓存未命中则返回 supplier 执行结果
     *
     * @param cacheKey 缓存的 key
     * @param clazz    需要返回的对象类型
     * @param <T>      返回值类型
     * @return 缓存的 value, 缓存未命中则返回 supplier 执行结果
     */
    public <T> T getCachedObjectOrElseGet(String cacheKey, Class<T> clazz, Supplier<T> supplier) {
        RBucket<String> bucket = redissonClient.getBucket(cacheKey);
        if (bucket.get() == null) {
            return supplier.get();
        }
        return JSON.parseObject(bucket.get(), clazz);
    }

    /**
     * 根据 KEY 从缓存中获取value
     *
     * <p> 如果缓存未命中则执行 function 逻辑进行数据库查询，
     * 并且将查询的数据放入缓存中
     *
     * @param cacheKey 缓存的key
     * @param clazz    需要返回的对象类型
     * @param id       主键ID
     * @param function 未命中缓存后，执行从DB中查询的逻辑
     * @param <T>      返回值类型
     * @param <ID>     主键类型
     * @return 缓存的value, 缓存为命中则执行 function
     */
    public <T, ID> T getCachedObjectOrElseFromDB(String cacheKey, Class<T> clazz,
                                                 ID id, Function<ID, T> function) {
        T result;
        RBucket<String> bucket = redissonClient.getBucket(cacheKey);
        if (bucket.get() == null) {
            result = function.apply(id);
            // 加入到缓存中
            cacheObject(cacheKey, result);
        } else {
            result = JSON.parseObject(bucket.get(), clazz);
        }
        return result;
    }

    /**
     * 根据 KEY 从缓存中获取value
     *
     * <p> 如果缓存未命中则执行 function 逻辑进行数据库查询(适用于联合主键查询)，
     * 并且将查询的数据放入缓存中
     *
     * @param cacheKey 缓存的key
     * @param clazz    需要返回的对象类型
     * @param id1      主键ID-1
     * @param id2      主键ID-2
     * @param function 未命中缓存后，执行从DB中查询的逻辑
     * @param <T>      返回值类型
     * @param <ID>     主键类型
     * @return 缓存的value, 缓存为命中则执行 function
     */
    public <T, ID> T getCachedObjectOrElseFromDB(String cacheKey, Class<T> clazz,
                                                 ID id1, ID id2, BiFunction<ID, ID, T> function) {
        T result;
        RBucket<String> bucket = redissonClient.getBucket(cacheKey);
        if (bucket.get() == null) {
            result = function.apply(id1, id2);
            cacheObject(cacheKey, result);
        } else {
            result = JSON.parseObject(bucket.get(), clazz);
        }
        return result;
    }

    /**
     * 将 value 放入缓存中
     *
     * @param cacheKey 缓存的key
     * @param value    缓存的value
     */
    public void cacheObject(String cacheKey, Object value) {
        RBucket<Object> bucket = redissonClient.getBucket(cacheKey);
        bucket.set(JSON.toJSONString(value));
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
