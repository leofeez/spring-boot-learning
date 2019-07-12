package com.leofee.springbootlearningredis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

/**
 * redis 配置类
 *
 * @author leofee
 * @date 2019/6/28
 */
@EnableCaching
@Configuration
public class RedisConfig extends CachingConfigurerSupport {

    @Value("${spring.redis.expire}")
    private static long REDIS_CACHE_EXPIRE_TERM;

    /**
     * redis template 的配置
     *
     * <ul>
     * <li>1. {@link ObjectMapper} : jackson 中用于 json 和 对象 之间的转换
     * <li>2. {@link Jackson2JsonRedisSerializer} 用于指定 redis 中存储 value
     * 的序列化方式, 如果不指定则默认使用 {@link JdkSerializationRedisSerializer}
     * </ul>
     *
     * 关于{@link RedisConnectionFactory}
     * redis 客户端目前有三种:
     * 1. Jedis：是老牌的Redis的Java实现客户端，提供了比较全面的Redis命令的支持
     *
     * 2. lettuce , {@link LettuceConnectionFactory}，高级Redis客户端，
     * 用于线程安全同步，异步和响应使用，支持集群，Sentinel，管道和编码器。
     *
     * 3. redisson , RedissonConnectionFactory
     * 促使使用者对Redis的关注分离，提供很多分布式相关操作服务，例如，分布式锁，分布式集合
     * ，可通过Redis支持延迟队列
     *
     * @param connectionFactory redis 连接配置
     * @return redis template
     */
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();

        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
                new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(redisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashKeySerializer(redisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        return template;
    }

    /**
     * 在没有指定key 的时候，生成 redis 缓存的 key 的策略
     *
     * @return {@link KeyGenerator}
     */
    @Bean
    public KeyGenerator getKeyGenerator() {
        return (o, method, objects) -> {
            StringBuilder builder = new StringBuilder(o.getClass().getName() + method.getName());
            if (objects != null) {

                for (Object object : objects) {
                    builder.append(object == null ? "" : object.toString());
                }
            }
            return builder.toString();
        };
    }

    /**
     * 设置默认的缓存
     *
     * @param connectionFactory redis 连接
     * @return 默认的缓存
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // key 序列化方式
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();

        // value 序列化方式
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
                new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        // 配置序列化
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(REDIS_CACHE_EXPIRE_TERM))
                .serializeKeysWith(fromSerializer(redisSerializer))
                .serializeValuesWith(fromSerializer(jackson2JsonRedisSerializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }
}
