package com.anzo.insurance.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 通用工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        return convertValue(value, clazz);
    }

    public <T> T get(String key, TypeReference<T> typeReference) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return objectMapper.convertValue(value, typeReference);
    }

    public boolean set(String key, Object value) {
        return set(key, value, null, null);
    }

    public boolean set(String key, Object value, Duration duration) {
        if (duration == null) {
            return set(key, value);
        }
        redisTemplate.opsForValue().set(key, value, duration);
        return true;
    }

    public boolean set(String key, Object value, long timeout, TimeUnit unit) {
        return set(key, value, Long.valueOf(timeout), unit);
    }

    public boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
        return Boolean.TRUE.equals(result);
    }

    public boolean setIfPresent(String key, Object value, long timeout, TimeUnit unit) {
        Boolean result = redisTemplate.opsForValue().setIfPresent(key, value, timeout, unit);
        return Boolean.TRUE.equals(result);
    }

    public boolean delete(String key) {
        Boolean result = redisTemplate.delete(key);
        return Boolean.TRUE.equals(result);
    }

    public long delete(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        Long result = redisTemplate.delete(keys);
        return result == null ? 0L : result;
    }

    public boolean exists(String key) {
        Boolean result = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(result);
    }

    public boolean expire(String key, long timeout, TimeUnit unit) {
        Boolean result = redisTemplate.expire(key, timeout, unit);
        return Boolean.TRUE.equals(result);
    }

    public long ttl(String key, TimeUnit unit) {
        Long ttl = redisTemplate.getExpire(key, unit);
        return ttl == null ? -1L : ttl;
    }

    public Set<String> keys(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        return keys == null ? Collections.emptySet() : keys;
    }

    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    public boolean hashPut(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
        return true;
    }

    public void hashPutAll(String key, Map<String, ?> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        redisTemplate.opsForHash().putAll(key, map);
    }

    public Object hashGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    public <T> T hashGet(String key, String hashKey, Class<T> clazz) {
        Object value = hashGet(key, hashKey);
        return convertValue(value, clazz);
    }

    public Map<Object, Object> hashEntries(String key) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        return entries == null ? Collections.emptyMap() : entries;
    }

    public long hashDelete(String key, Object... hashKeys) {
        Long result = redisTemplate.opsForHash().delete(key, hashKeys);
        return result == null ? 0L : result;
    }

    public boolean hashHasKey(String key, String hashKey) {
        Boolean result = redisTemplate.opsForHash().hasKey(key, hashKey);
        return Boolean.TRUE.equals(result);
    }

    public long listLeftPush(String key, Object value) {
        Long result = redisTemplate.opsForList().leftPush(key, value);
        return result == null ? 0L : result;
    }

    public long listRightPush(String key, Object value) {
        Long result = redisTemplate.opsForList().rightPush(key, value);
        return result == null ? 0L : result;
    }

    public <T> List<T> listRange(String key, long start, long end, Class<T> clazz) {
        List<Object> values = redisTemplate.opsForList().range(key, start, end);
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return values.stream().map(item -> convertValue(item, clazz)).toList();
    }

    public long listSize(String key) {
        Long size = redisTemplate.opsForList().size(key);
        return size == null ? 0L : size;
    }

    public long setAdd(String key, Object... values) {
        Long result = redisTemplate.opsForSet().add(key, values);
        return result == null ? 0L : result;
    }

    public long setRemove(String key, Object... values) {
        Long result = redisTemplate.opsForSet().remove(key, values);
        return result == null ? 0L : result;
    }

    public Set<Object> setMembers(String key) {
        Set<Object> members = redisTemplate.opsForSet().members(key);
        return members == null ? Collections.emptySet() : members;
    }

    public boolean setIsMember(String key, Object value) {
        Boolean result = redisTemplate.opsForSet().isMember(key, value);
        return Boolean.TRUE.equals(result);
    }

    private boolean set(String key, Object value, Long timeout, TimeUnit unit) {
        try {
            if (timeout == null || unit == null) {
                redisTemplate.opsForValue().set(key, value);
            } else {
                redisTemplate.opsForValue().set(key, value, timeout, unit);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis 写入失败, key={}", key, e);
            return false;
        }
    }

    private <T> T convertValue(Object value, Class<T> clazz) {
        if (value == null) {
            return null;
        }
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        return objectMapper.convertValue(value, clazz);
    }
}
