package com.anzo.insurance.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis 分布式锁工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockUtil {

    private final RedissonClient redissonClient;

    public RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }

    public boolean tryLock(String lockKey) {
        return tryLock(lockKey, 0, 30, TimeUnit.SECONDS);
    }

    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("获取 Redis 锁被中断: lockKey={}", lockKey, e);
            return false;
        }
    }

    public void unlock(String lockKey) {
        unlock(getLock(lockKey));
    }

    public boolean isLocked(String lockKey) {
        return getLock(lockKey).isLocked();
    }

    public boolean isHeldByCurrentThread(String lockKey) {
        return getLock(lockKey).isHeldByCurrentThread();
    }

    public void executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Runnable runnable) {
        executeWithLock(lockKey, waitTime, leaseTime, unit, () -> {
            runnable.run();
            return null;
        });
    }

    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> supplier) {
        RLock lock = getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(waitTime, leaseTime, unit);
            if (!locked) {
                throw new IllegalStateException("获取 Redis 锁失败: " + lockKey);
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("获取 Redis 锁被中断: " + lockKey, e);
        } finally {
            if (locked) {
                unlock(lock);
            }
        }
    }

    private void unlock(RLock lock) {
        try {
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (Exception e) {
            log.warn("释放 Redis 锁失败", e);
        }
    }
}
