package com.example.time.mapper;

import com.example.time.pojo.Event;
import com.example.time.pojo.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DataMapper {

    @Select("SELECT * FROM events WHERE user_id = #{userId}")
    List<Event> eventIndex(int userId);

    @Select("SELECT * FROM events WHERE user_id = #{userId} AND name = #{name}")
    Event event(int userId, String name);

    @Select("SELECT * FROM events WHERE user_id = #{userId} AND name = #{name} AND year = #{year} AND month = #{month} AND day = #{day}")
    Event eventByUserAndNameAndDate(@Param("userId") int userId, @Param("name") String name,
                                     @Param("year") int year, @Param("month") int month, @Param("day") int day);

    @Select("SELECT * FROM events WHERE event_id = #{eventId}")
    Event eventById(@Param("eventId") int eventId);

    @Select("SELECT * FROM `user` WHERE user_id = #{userId}")
    User userId(int userId);

    @Select("SELECT * FROM events")
    List<Event> findAll();

    @Update("UPDATE `user` SET operation_count = #{operationCount} WHERE user_id = #{userId}")
    int opCount(@Param("operationCount") int operationCount, @Param("userId") int userId);

    @Update("UPDATE `user` SET nickname = #{nickname} WHERE user_id = #{userId}")
    int updateNickname(@Param("userId") int userId, @Param("nickname") String nickname);

    @Update("UPDATE `user` SET nickname = #{nickname}, phone = #{phone} WHERE user_id = #{userId}")
    int updateUserInfo(@Param("userId") int userId, @Param("nickname") String nickname, @Param("phone") String phone);

    @Update("UPDATE `user` SET password = #{password} WHERE user_id = #{userId}")
    int updatePassword(@Param("userId") int userId, @Param("password") String password);

    @Select("SELECT * FROM events WHERE user_id = #{userId} AND year = #{year}")
    List<Event> eventDate1(@Param("userId") int userId, @Param("year") int year);

    @Select("SELECT * FROM events WHERE user_id = #{userId} AND year = #{year} LIMIT #{offset}, 10")
    List<Event> eventeachDate1(@Param("userId") int userId, @Param("year") int year, @Param("offset") int offset);

    @Select("SELECT * FROM events WHERE user_id = #{userId} AND month = #{month}")
    List<Event> eventDate2(@Param("userId") int userId, @Param("month") int month);

    @Select("SELECT * FROM events WHERE user_id = #{userId} AND month = #{month} LIMIT #{offset}, 10")
    List<Event> eventeachDate2(@Param("userId") int userId, @Param("month") int month, @Param("offset") int offset);

    @Select("SELECT * FROM events WHERE user_id = #{userId} AND day = #{day}")
    List<Event> eventDate3(@Param("userId") int userId, @Param("day") int day);

    @Select("SELECT * FROM events WHERE user_id = #{userId} AND day = #{day} LIMIT #{offset}, 10")
    List<Event> eventeachDate3(@Param("userId") int userId, @Param("day") int day, @Param("offset") int offset);

    @Select("SELECT * FROM events WHERE user_id = #{userId} AND day = #{day} AND year = #{year}")
    List<Event> eventDate4(@Param("userId") int userId, @Param("day") int day, @Param("year") int year);

    @Select("SELECT * FROM events WHERE user_id = #{userId} AND day = #{day} AND year = #{year} LIMIT #{offset}, 10")
    List<Event> eventeachDate4(@Param("userId") int userId, @Param("day") int day, @Param("year") int year, @Param("offset") int offset);

    @Select("SELECT * FROM events WHERE user_id = #{userId} AND day = #{day} AND month = #{month}")
    List<Event> eventDate5(@Param("userId") int userId, @Param("day") int day, @Param("month") int month);

    @Select("SELECT * FROM events WHERE user_id = #{userId} AND day = #{day} AND month = #{month} LIMIT #{offset}, 10")
    List<Event> eventeachDate5(@Param("userId") int userId, @Param("day") int day, @Param("month") int month, @Param("offset") int offset);

    @Select("SELECT * FROM events WHERE user_id = #{userId} AND year = #{year} AND month = #{month}")
    List<Event> eventDate6(@Param("userId") int userId, @Param("year") int year, @Param("month") int month);

    @Select("SELECT * FROM events WHERE user_id = #{userId} AND year = #{year} AND month = #{month} LIMIT #{offset}, 10")
    List<Event> eventeachDate6(@Param("userId") int userId, @Param("year") int year, @Param("month") int month, @Param("offset") int offset);

    @Select("SELECT * FROM events WHERE user_id = #{userId} AND year = #{year} AND month = #{month} AND day = #{day}")
    List<Event> eventDate7(@Param("userId") int userId, @Param("year") int year, @Param("month") int month, @Param("day") int day);

    @Select("SELECT * FROM events WHERE user_id = #{userId} AND year = #{year} AND month = #{month} AND day = #{day} LIMIT #{offset}, 10")
    List<Event> eventeachDate7(@Param("userId") int userId, @Param("year") int year, @Param("month") int month, @Param("day") int day, @Param("offset") int offset);

    @Insert("INSERT INTO events(user_id, name, description, year, month, day) VALUES (#{userId},#{name},#{description},#{year},#{month},#{day})")
    int addEvent(@Param("userId") int userId, @Param("name") String name, @Param("description") String description,
                 @Param("year") int year, @Param("month") int month, @Param("day") int day);

    @Delete("DELETE FROM events WHERE event_id = #{eventId}")
    int delEventSingle(@Param("eventId") int eventId);

    @Delete("DELETE FROM events WHERE user_id = #{userId} AND (year*10000+month*100+day) BETWEEN #{startDate} AND #{endDate}")
    int deleteByDateRange(@Param("userId") int userId, @Param("startDate") int startDate, @Param("endDate") int endDate);

    @Select("SELECT * FROM events WHERE user_id = #{userId} LIMIT #{offset}, 10")
    List<Event> eventPage(@Param("userId") int userId, @Param("offset") int offset);

    @Select("SELECT * FROM events WHERE user_id = #{userId} AND name LIKE CONCAT('%',#{name},'%')")
    List<Event> eventNameConcat(@Param("userId") int userId, @Param("name") String name);

    @Select("SELECT * FROM events WHERE user_id = #{userId} AND name LIKE CONCAT('%',#{name},'%') LIMIT #{offset}, 10")
    List<Event> eventnamePage(@Param("userId") int userId, @Param("name") String name, @Param("offset") int offset);

    @Update("UPDATE events SET name = #{name}, year = #{year}, month = #{month}, day = #{day}, description = #{description} WHERE event_id = #{eventId}")
    int update(@Param("name") String name, @Param("year") int year, @Param("month") int month, @Param("day") int day,
              @Param("description") String description, @Param("eventId") int eventId);

    @SelectProvider(type = EventQueryProvider.class, method = "queryEvents")
    List<Event> queryEventsByCondition(@Param("userId") int userId, @Param("name") String name,
                                       @Param("startDate") Integer startDate, @Param("endDate") Integer endDate,
                                       @Param("todayNum") Integer todayNum, @Param("status") String status,
                                       @Param("offset") Integer offset, @Param("size") Integer size);

    @SelectProvider(type = EventQueryProvider.class, method = "countEvents")
    int countEventsByCondition(@Param("userId") int userId, @Param("name") String name,
                               @Param("startDate") Integer startDate, @Param("endDate") Integer endDate,
                               @Param("todayNum") Integer todayNum, @Param("status") String status);
}
