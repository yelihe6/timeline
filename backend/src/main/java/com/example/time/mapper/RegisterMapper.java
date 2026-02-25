package com.example.time.mapper;

import com.example.time.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RegisterMapper {

    @Insert("INSERT INTO `user`(email, password, nickname, operation_count) VALUES(#{email}, #{password}, #{nickname}, 0)")
    int userRegister(@Param("email") String email, @Param("password") String password, @Param("nickname") String nickname);

    @Select("SELECT * FROM `user` WHERE email = #{email}")
    User findByEmail(@Param("email") String email);
}
