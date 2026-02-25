package com.example.time.service;

import com.example.time.mapper.DataMapper;
import com.example.time.pojo.Event;
import com.example.time.pojo.EventQuery;
import com.example.time.pojo.PageResult;
import com.example.time.pojo.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    private final DataMapper dataMapper;

    public EventService(DataMapper dataMapper) {
        this.dataMapper = dataMapper;
    }

    public User getUser(int userId) {
        return dataMapper.userId(userId);
    }

    public Event getEvent(int userId, String name) {
        return dataMapper.event(userId, name);
    }

    public Event getEventByUserAndNameAndDate(int userId, String name, int year, int month, int day) {
        return dataMapper.eventByUserAndNameAndDate(userId, name, year, month, day);
    }

    public Event getEventById(int eventId) {
        return dataMapper.eventById(eventId);
    }

    public List<Event> listEvents(int userId) {
        return dataMapper.eventIndex(userId);
    }

    public PageResult<Event> queryEvents(EventQuery query) {
        int userId = query.getUserId();
        int page = query.getPage() != null ? query.getPage() : 1;
        int size = query.getSize() != null ? query.getSize() : 20;
        if (size != 8 && size != 20 && size != 50) size = 20;
        int offset = (page - 1) * size;

        String name = query.getName();
        if (name != null) name = name.trim();
        if (name != null && name.isEmpty()) name = null;

        Integer startDate = toDateNum(query.getStartYear(), query.getStartMonth(), query.getStartDay());
        Integer endDate = toDateNum(query.getEndYear(), query.getEndMonth(), query.getEndDay());
        String status = query.getStatus();
        if (status != null) status = status.trim();
        if (status != null && status.isEmpty()) status = null;

        java.util.Calendar cal = java.util.Calendar.getInstance();
        int todayNum = cal.get(java.util.Calendar.YEAR) * 10000
                + (cal.get(java.util.Calendar.MONTH) + 1) * 100
                + cal.get(java.util.Calendar.DAY_OF_MONTH);

        List<Event> list = dataMapper.queryEventsByCondition(userId, name, startDate, endDate, todayNum, status, offset, size);
        int total = dataMapper.countEventsByCondition(userId, name, startDate, endDate, todayNum, status);
        int totalPages = (total + size - 1) / size;

        return new PageResult<>(list, total, page, totalPages);
    }

    private Integer toDateNum(Integer year, Integer month, Integer day) {
        if (year == null || month == null || day == null) return null;
        return year * 10000 + month * 100 + day;
    }

    public int addEvent(int userId, String name, String description, int year, int month, int day) {
        return dataMapper.addEvent(userId, name, description, year, month, day);
    }

    public int updateEvent(int eventId, String name, int year, int month, int day, String description) {
        return dataMapper.update(name, year, month, day, description, eventId);
    }

    public int deleteEvent(int eventId) {
        return dataMapper.delEventSingle(eventId);
    }

    public int deleteEvents(List<Integer> eventIds) {
        int count = 0;
        for (Integer id : eventIds) {
            count += dataMapper.delEventSingle(id);
        }
        return count;
    }

    public int deleteByDateRange(int userId, int startDate, int endDate) {
        return dataMapper.deleteByDateRange(userId, startDate, endDate);
    }
}
