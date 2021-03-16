package com.mafgwo.common.distributed.lock.core.model;

/**
 * 锁类型
 *
 * @author chenxiaoqi
 * @since 2019/07/29
 */
public enum LockType {
    /**
     * 可重入锁
     */
    Reentrant,
    /**
     * 公平锁
     */
    Fair,
    /**
     * 读锁
     */
    Read,
    /**
     * 写锁
     */
    Write;
}
