package com.mafgwo.common.distributed.lock.core.lock;

/**
 * @author chenxiaoqi
 * @since 2019/07/29
 */
public interface Lock {

    boolean acquire();

    boolean release();
}

