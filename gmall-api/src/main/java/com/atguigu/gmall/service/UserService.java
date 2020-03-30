package com.atguigu.gmall.service;

import com.atguigu.gmall.beans.UmsMember;
import com.atguigu.gmall.beans.UmsMemberReceiveAddress;

import java.util.List;

/**
 * @author hhy1997
 * 2020/2/22
 */
public interface UserService {
    List<UmsMember> list();

    UmsMember login(UmsMember umsMember);

    void addUserToken(String token, String memberId);

    UmsMember addOauthUser(UmsMember umsMember);

    UmsMember checkOauthUser(UmsMember checkUser);

    List<UmsMemberReceiveAddress> getAddressListByMemberId(String memberId);

    UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId);
}
