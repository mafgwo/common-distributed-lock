package com.mafgwo.common.distributed.lock.core.handler.lock;

import com.mafgwo.common.distributed.lock.core.model.LockInfo;
import com.mafgwo.common.distributed.lock.core.lock.Lock;
import org.aspectj.lang.JoinPoint;

/**
 * 获取锁超时的处理逻辑接口
 *
 * @author chenxiaoqi
 * @since 2019/07/29
 **/
public interface LockTimeoutHandler {

    void handle(LockInfo lockInfo, Lock lock, JoinPoint joinPoint);
}
