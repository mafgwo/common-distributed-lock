package com.mafgwo.common.distributed.lock.example.service.impl;

import com.mafgwo.common.distributed.lock.core.annotation.DistributedLock;
import com.mafgwo.common.distributed.lock.core.annotation.DistributedLockKey;
import com.mafgwo.common.distributed.lock.core.model.LockTimeoutStrategy;
import com.mafgwo.common.distributed.lock.example.domain.UserVO;
import com.mafgwo.common.distributed.lock.example.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestServiceImpl implements TestService {


    @DistributedLock(name = "test-lock", keys = {"#id"}, leaseTime = -1)
    @Override
    public void testLock(String id, UserVO user) {
        try {
            Thread.sleep(30000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("test-lock finished");
    }

    /**
     * 此方法的锁 与 上面 testLock方法的锁的key是同一个
     * @param id
     * @param user
     */
    @DistributedLock(name = "test-lock", waitTime = 2, lockTimeoutStrategy = LockTimeoutStrategy.FAIL_FAST)
    @Override
    public void testLock2(@DistributedLockKey String id, UserVO user) {
        this.testLock(id, user);
    }

    @DistributedLock(name = "test-lock3")
    @Override
    public void testLock3(String id, @DistributedLockKey("deptVO.id") UserVO user) {
        this.testLock(id, user);
    }

    @DistributedLock(name = "test-lock4")
    @Override
    public void testLock4(String id, @DistributedLockKey("id") UserVO user) {
        this.testLock(id, user);
    }
}
