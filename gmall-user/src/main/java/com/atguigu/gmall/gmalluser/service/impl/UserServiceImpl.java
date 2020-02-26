package com.atguigu.gmall.gmalluser.service.impl;

import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.gmalluser.mapper.UmsMemberMapper;
import com.atguigu.gmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author hhy1997
 * 2020/2/22
 */
@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UmsMemberMapper umsMemberMapper;

    @Override
    public List<UmsMember> list() {
//        UmsMemberExample umsMemberExample = new UmsMemberExample();
//        umsMemberExample.setOrderByClause("id asc");
//        return umsMemberMapper.selectByExample(umsMemberExample);
//        return umsMemberMapper.selectAllUser();
        return umsMemberMapper.selectAll();
    }
}
