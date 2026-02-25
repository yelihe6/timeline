package com.example.time.mapper;

import com.example.time.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LoginMapper {

    @Select("SELECT * FROM `user` WHERE email = #{email}")
    User findByEmail(@Param("email") String email);
}
