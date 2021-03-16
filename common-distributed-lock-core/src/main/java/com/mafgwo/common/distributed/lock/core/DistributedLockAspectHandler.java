package com.mafgwo.common.distributed.lock.core;

import com.mafgwo.common.distributed.lock.core.annotation.DistributedLock;
import com.mafgwo.common.distributed.lock.core.handler.DistributedLockInvocationException;
import com.mafgwo.common.distributed.lock.core.model.LockInfo;
import com.mafgwo.common.distributed.lock.core.lock.Lock;
import com.mafgwo.common.distributed.lock.core.lock.LockFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 给添加@JKLock切面加锁处理
 *
 * @author chenxiaoqi
 * @since 2019/07/29
 */
@Aspect
@Component
@Order(0)
public class DistributedLockAspectHandler {

    private static final Logger logger = LoggerFactory.getLogger(DistributedLockAspectHandler.class);

    @Autowired
    private LockFactory lockFactory;

    @Autowired
    private LockInfoProvider lockInfoProvider;

    private ThreadLocal<Lock> currentThreadLock = new ThreadLocal<>();
    private ThreadLocal<LockRes> currentThreadLockRes = new ThreadLocal<>();

    @Around(value = "@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        LockInfo lockInfo = lockInfoProvider.get(joinPoint, distributedLock);
        currentThreadLockRes.set(new LockRes(lockInfo, false));
        Lock lock = lockFactory.getLock(lockInfo);
        boolean lockRes = lock.acquire();

        if (!lockRes) {
            if (logger.isWarnEnabled()) {
                logger.warn("Timeout while acquiring Lock({})", lockInfo.getName());
            }

            if (!StringUtils.isEmpty(distributedLock.customLockTimeoutStrategy())) {
                return handleCustomLockTimeout(distributedLock.customLockTimeoutStrategy(), joinPoint);
            } else {
                distributedLock.lockTimeoutStrategy().handle(lockInfo, lock, joinPoint);
                return null;
            }
        }

        currentThreadLock.set(lock);
        currentThreadLockRes.get().setRes(true);

        return joinPoint.proceed();
    }

    @AfterReturning(value = "@annotation(distributedLock)")
    public void afterReturning(JoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {

        releaseLock(distributedLock, joinPoint);
        cleanUpThreadLocal();
    }

    @AfterThrowing(value = "@annotation(distributedLock)", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, DistributedLock distributedLock, Throwable ex) throws Throwable {

        releaseLock(distributedLock, joinPoint);
        cleanUpThreadLocal();
        throw ex;
    }

    /**
     * 处理自定义加锁超时
     */
    private Object handleCustomLockTimeout(String lockTimeoutHandler, JoinPoint joinPoint) throws Throwable {

        // prepare invocation context
        Method currentMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object target = joinPoint.getTarget();
        Method handleMethod = null;
        try {
            handleMethod = joinPoint.getTarget().getClass().getDeclaredMethod(lockTimeoutHandler, currentMethod.getParameterTypes());
            handleMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Illegal annotation param customLockTimeoutStrategy", e);
        }
        Object[] args = joinPoint.getArgs();

        // invoke
        Object res = null;
        try {
            res = handleMethod.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new DistributedLockInvocationException("Fail to invoke custom lock timeout handler: " + lockTimeoutHandler, e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }

        return res;
    }

    /**
     * 释放锁
     */
    private void releaseLock(DistributedLock distributedLock, JoinPoint joinPoint) throws Throwable {
        LockRes lockRes = currentThreadLockRes.get();
        if (lockRes.getRes()) {
            boolean releaseRes = currentThreadLock.get().release();
            // avoid release lock twice when exception happens below
            lockRes.setRes(false);
            if (!releaseRes) {
                handleReleaseTimeout(distributedLock, lockRes.getLockInfo(), joinPoint);
            }
        }
    }


    /**
     * 处理释放锁时已超时
     */
    private void handleReleaseTimeout(DistributedLock distributedLock, LockInfo lockInfo, JoinPoint joinPoint) throws Throwable {

        if (logger.isWarnEnabled()) {
            logger.warn("Timeout while release Lock({})", lockInfo.getName());
        }

        if (!StringUtils.isEmpty(distributedLock.customReleaseTimeoutStrategy())) {

            handleCustomReleaseTimeout(distributedLock.customReleaseTimeoutStrategy(), joinPoint);

        } else {
            distributedLock.releaseTimeoutStrategy().handle(lockInfo);
        }

    }

    /**
     * 处理自定义释放锁时已超时
     */
    private void handleCustomReleaseTimeout(String releaseTimeoutHandler, JoinPoint joinPoint) throws Throwable {

        Method currentMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object target = joinPoint.getTarget();
        Method handleMethod = null;
        try {
            handleMethod = joinPoint.getTarget().getClass().getDeclaredMethod(releaseTimeoutHandler, currentMethod.getParameterTypes());
            handleMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Illegal annotation param customReleaseTimeoutStrategy", e);
        }
        Object[] args = joinPoint.getArgs();

        try {
            handleMethod.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new DistributedLockInvocationException("Fail to invoke custom release timeout handler: " + releaseTimeoutHandler, e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private class LockRes {

        private LockInfo lockInfo;

        private Boolean res;

        LockRes(LockInfo lockInfo, Boolean res) {
            this.lockInfo = lockInfo;
            this.res = res;
        }

        LockInfo getLockInfo() {
            return lockInfo;
        }

        Boolean getRes() {
            return res;
        }

        void setRes(Boolean res) {
            this.res = res;
        }

        void setLockInfo(LockInfo lockInfo) {
            this.lockInfo = lockInfo;
        }
    }

    // avoid memory leak
    private void cleanUpThreadLocal() {

        currentThreadLockRes.remove();
        currentThreadLock.remove();
    }
}
