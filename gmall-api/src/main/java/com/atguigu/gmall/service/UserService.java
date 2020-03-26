package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UmsMember;

import java.util.List;

/**
 * @author hhy1997
 * 2020/2/22
 */
public interface UserService {
    List<UmsMember> list();

    UmsMember login(UmsMember umsMember);

    void addUserToken(String token, Long memberId);
}
