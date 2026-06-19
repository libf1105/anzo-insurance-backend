package com.anzo.insurance.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * Redis 配置
 */
@Configuration
public class RedisConfig {

    /**
     * 通用 RedisTemplate，统一使用字符串 key 和 JSON value。
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redisson 客户端，用于分布式锁等高级能力。
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedisProperties redisProperties, Environment environment) {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(buildAddress(redisProperties, environment))
                .setDatabase(resolveDatabase(redisProperties, environment));

        if (resolveTimeout(redisProperties, environment) != null) {
            singleServerConfig.setTimeout((int) resolveTimeout(redisProperties, environment).toMillis());
        }
        if (StringUtils.hasText(resolveUsername(redisProperties, environment))) {
            singleServerConfig.setUsername(resolveUsername(redisProperties, environment));
        }
        if (StringUtils.hasText(resolvePassword(redisProperties, environment))) {
            singleServerConfig.setPassword(resolvePassword(redisProperties, environment));
        }

        return Redisson.create(config);
    }

    private String buildAddress(RedisProperties redisProperties, Environment environment) {
        String host = firstNonBlank(
                environment.getProperty("spring.redis.host"),
                environment.getProperty("spring.data.redis.host"),
                redisProperties.getHost(),
                "localhost"
        );
        int port = firstPositive(
                environment.getProperty("spring.redis.port", Integer.class),
                environment.getProperty("spring.data.redis.port", Integer.class),
                redisProperties.getPort(),
                6379
        );
        return "redis://" + host + ":" + port;
    }

    private int resolveDatabase(RedisProperties redisProperties, Environment environment) {
        return firstPositive(
                environment.getProperty("spring.redis.database", Integer.class),
                environment.getProperty("spring.data.redis.database", Integer.class),
                redisProperties.getDatabase(),
                0
        );
    }

    private java.time.Duration resolveTimeout(RedisProperties redisProperties, Environment environment) {
        java.time.Duration timeout = environment.getProperty("spring.redis.timeout", java.time.Duration.class);
        if (timeout != null) {
            return timeout;
        }
        timeout = environment.getProperty("spring.data.redis.timeout", java.time.Duration.class);
        if (timeout != null) {
            return timeout;
        }
        return redisProperties.getTimeout();
    }

    private String resolveUsername(RedisProperties redisProperties, Environment environment) {
        return firstNonBlank(
                environment.getProperty("spring.redis.username"),
                environment.getProperty("spring.data.redis.username"),
                redisProperties.getUsername(),
                null
        );
    }

    private String resolvePassword(RedisProperties redisProperties, Environment environment) {
        return firstNonBlank(
                environment.getProperty("spring.redis.password"),
                environment.getProperty("spring.data.redis.password"),
                redisProperties.getPassword(),
                null
        );
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private int firstPositive(Integer first, Integer second, int third, int fallback) {
        if (first != null && first >= 0) {
            return first;
        }
        if (second != null && second >= 0) {
            return second;
        }
        if (third >= 0) {
            return third;
        }
        return fallback;
    }
}
