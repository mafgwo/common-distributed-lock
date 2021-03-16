package com.mafgwo.common.distributed.lock.core.model;


import com.mafgwo.common.distributed.lock.core.handler.DistributedLockTimeoutException;
import com.mafgwo.common.distributed.lock.core.handler.release.ReleaseTimeoutHandler;

/**
 * @author chenxiaoqi
 * @since 2019/07/29
 **/
public enum ReleaseTimeoutStrategy implements ReleaseTimeoutHandler {

    /**
     * 继续执行业务逻辑，不做任何处理
     */
    NO_OPERATION() {
        @Override
        public void handle(LockInfo lockInfo) {
            // do nothing
        }
    },
    /**
     * 快速失败
     */
    FAIL_FAST() {
        @Override
        public void handle(LockInfo lockInfo) {

            String errorMsg = String.format("Found Lock(%s) already been released while lock lease time is %d s", lockInfo.getName(), lockInfo.getLeaseTime());
            throw new DistributedLockTimeoutException(errorMsg);
        }
    }
}
