package com.renxingbao.spring_fivth.mapper;

import com.renxingbao.spring_fivth.domain.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    public List<User> queryUserList();
}
