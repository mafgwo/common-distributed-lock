package com.mafgwo.common.distributed.lock.example.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserVO {

    private Long id;

    private String name;

    private Integer age;

    private DeptVO deptVO;

}
