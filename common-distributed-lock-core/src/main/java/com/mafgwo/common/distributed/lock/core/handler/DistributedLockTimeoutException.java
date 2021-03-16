package com.mafgwo.common.distributed.lock.core.handler;

/**
 * @author chenxiaoqi
 * @since 2019/07/29
 **/
public class DistributedLockTimeoutException extends RuntimeException {

    public DistributedLockTimeoutException() {
    }

    public DistributedLockTimeoutException(String message) {
        super(message);
    }

    public DistributedLockTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
