package com.mafgwo.common.distributed.lock.example.controller;


import com.mafgwo.common.distributed.lock.example.domain.DeptVO;
import com.mafgwo.common.distributed.lock.example.domain.UserVO;
import com.mafgwo.common.distributed.lock.example.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping
    public String test(String id) {
//        id = new Random().nextInt(100) + "";
        testService.testLock(id, new UserVO(1L, "zhangsan", 18, new DeptVO(1L, "交易平台")));
        return "ok";
    }

    @GetMapping("/test2")
    public String test2(String id) {
//        id = new Random().nextInt(100) + "";
        testService.testLock2(id, new UserVO(1L, "zhangsan", 18, new DeptVO(1L, "交易平台")));
        return "ok";
    }

    @GetMapping("/test3")
    public String test3(String id) {
//        id = new Random().nextInt(100) + "";
        testService.testLock3(id, new UserVO(1L, "zhangsan", 18, new DeptVO(1L, "交易平台")));
        return "ok";
    }

    @GetMapping("/test4")
    public String test4(String id) {
//        id = new Random().nextInt(100) + "";
        testService.testLock4(id, new UserVO(1L, "zhangsan", 18, new DeptVO(1L, "交易平台")));
        return "ok";
    }
}
