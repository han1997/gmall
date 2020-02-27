package com.atguigu.gmall.user.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.user.mapper.UmsMemberMapper;
import com.atguigu.gmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;

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
