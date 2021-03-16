package com.mafgwo.common.distributed.lock.core.handler;

/**
 * @author chenxiaoqi
 * @since 2019/07/29
 **/
public class DistributedLockInvocationException extends RuntimeException {

    public DistributedLockInvocationException() {
    }

    public DistributedLockInvocationException(String message) {
        super(message);
    }

    public DistributedLockInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
