package com.mafgwo.common.distributed.lock.core.handler.release;

import com.mafgwo.common.distributed.lock.core.model.LockInfo;

/**
 * 获取锁超时的处理逻辑接口
 *
 * @author chenxiaoqi
 * @since 2019/07/29
 **/
public interface ReleaseTimeoutHandler {

    void handle(LockInfo lockInfo);
}
