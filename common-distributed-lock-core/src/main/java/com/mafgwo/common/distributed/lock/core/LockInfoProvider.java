package com.mafgwo.common.distributed.lock.core;

import com.mafgwo.common.distributed.lock.core.annotation.DistributedLock;
import com.mafgwo.common.distributed.lock.core.config.DistributedLockProperties;
import com.mafgwo.common.distributed.lock.core.model.LockInfo;
import com.mafgwo.common.distributed.lock.core.model.LockType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

/**
 * @author chenxiaoqi
 * @since 2019/07/29
 */
public class LockInfoProvider {

    public static final String LOCK_NAME_SEPARATOR = ":lock:";

    @Autowired
    private DistributedLockProperties distributedLockProperties;

    @Autowired
    private BusinessKeyProvider businessKeyProvider;

    /**
     * 默认锁的前缀
     */
    @Value("${spring.application.name:distributed-lock}")
    private String defaultLockPrefix;

    private static final Logger logger = LoggerFactory.getLogger(LockInfoProvider.class);

    public LockInfo get(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
        LockType type = distributedLock.lockType();
        String businessKeyName = businessKeyProvider.getKeyName(joinPoint, distributedLock);
        String lockPrefix = StringUtils.hasText(distributedLockProperties.getLockPrefix()) ? distributedLockProperties.getLockPrefix() : defaultLockPrefix;
        String lockName = lockPrefix + LOCK_NAME_SEPARATOR + distributedLock.name() + businessKeyName;
        long waitTime = getWaitTime(distributedLock);
        long leaseTime = getLeaseTime(distributedLock);

        if (leaseTime == -1 && logger.isWarnEnabled()) {
            logger.warn("Trying to acquire Lock({}) with no expiration, " +
                "DistributedLock will keep prolong the lock expiration while the lock is still holding by current thread. " +
                "This may cause dead lock in some circumstances.", lockName);
        }

        return new LockInfo(type, lockName, waitTime, leaseTime);
    }

    private long getWaitTime(DistributedLock lock) {
        return lock.waitTime() == Long.MIN_VALUE ?
            distributedLockProperties.getWaitTime() : lock.waitTime();
    }

    private long getLeaseTime(DistributedLock lock) {
        return lock.leaseTime() == Long.MIN_VALUE ?
            distributedLockProperties.getLeaseTime() : lock.leaseTime();
    }
}
