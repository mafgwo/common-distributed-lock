package com.mafgwo.common.distributed.lock.example.service;


import com.mafgwo.common.distributed.lock.example.domain.UserVO;

public interface TestService {

    void testLock(String id, UserVO user);

    void testLock2(String id, UserVO user);

    void testLock3(String id, UserVO user);

    void testLock4(String id, UserVO user);

}
