package com.example.time.controller;

import com.example.time.pojo.*;
import com.example.time.service.EventService;
import com.example.time.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final UserService userService;

    public EventController(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }

    @GetMapping("/check-name")
    public ApiResult<Map<String, Boolean>> checkName(
            @RequestParam int userId,
            @RequestParam String name,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day,
            @RequestParam(required = false) Integer excludeEventId) {
        if (name == null || name.isBlank()) {
            return ApiResult.success(java.util.Map.of("exists", false));
        }
        Event exist = eventService.getEventByUserAndNameAndDate(userId, name.trim(), year, month, day);
        boolean exists = exist != null && (excludeEventId == null || !java.util.Objects.equals(exist.getEventId(), excludeEventId));
        return ApiResult.success(java.util.Map.of("exists", exists));
    }

    @GetMapping
    public ApiResult<PageResult<Event>> list(
            @RequestParam int userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer startMonth,
            @RequestParam(required = false) Integer startDay,
            @RequestParam(required = false) Integer endYear,
            @RequestParam(required = false) Integer endMonth,
            @RequestParam(required = false) Integer endDay,
            @RequestParam(required = false) String status) {

        EventQuery query = new EventQuery();
        query.setUserId(userId);
        query.setPage(page);
        query.setSize(size);
        query.setName(name);
        query.setYear(year);
        query.setMonth(month);
        query.setDay(day);
        query.setStartYear(startYear);
        query.setStartMonth(startMonth);
        query.setStartDay(startDay);
        query.setEndYear(endYear);
        query.setEndMonth(endMonth);
        query.setEndDay(endDay);
        query.setStatus(status);

        PageResult<Event> result = eventService.queryEvents(query);
        return ApiResult.success(result);
    }

    @PostMapping
    public ApiResult<Object> add(@RequestBody Map<String, Object> params) {
        int userId = getInt(params, "userId");
        String name = getStr(params, "name");
        String description = getStr(params, "description");
        int year = getInt(params, "year");
        int month = getInt(params, "month");
        int day = getInt(params, "day");

        if (name == null || name.isBlank()) {
            return ApiResult.error("事件名称不能为空");
        }

        Event exist = eventService.getEventByUserAndNameAndDate(userId, name, year, month, day);
        if (exist != null) {
            return ApiResult.error("该日期下事件名称已存在");
        }

        User user = eventService.getUser(userId);
        int opCount = user.getOperationCount() != null ? user.getOperationCount() : 0;
        userService.updateOpCount(opCount + 1, userId);

        eventService.addEvent(userId, name, description != null ? description : "", year, month, day);
        return ApiResult.success("添加成功");
    }

    @PutMapping("/{id}")
    public ApiResult<String> update(@PathVariable int id, @RequestBody Map<String, Object> params) {
        String name = getStr(params, "name");
        int year = getInt(params, "year");
        int month = getInt(params, "month");
        int day = getInt(params, "day");
        String description = getStr(params, "description");

        if (name == null || name.isBlank()) {
            return ApiResult.error("事件名称不能为空");
        }

        Event current = eventService.getEventById(id);
        if (current == null) {
            return ApiResult.error("事件不存在");
        }
        Event exist = eventService.getEventByUserAndNameAndDate(current.getUserId(), name, year, month, day);
        if (exist != null && !java.util.Objects.equals(exist.getEventId(), id)) {
            return ApiResult.error("该日期下事件名称已存在");
        }

        int rows = eventService.updateEvent(id, name, year, month, day, description != null ? description : "");
        return rows > 0 ? ApiResult.success("更新成功") : ApiResult.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public ApiResult<String> delete(@PathVariable int id, @RequestParam int userId, @RequestParam(defaultValue = "0") int opCount) {
        userService.updateOpCount(Math.max(0, opCount - 1), userId);
        int rows = eventService.deleteEvent(id);
        return rows > 0 ? ApiResult.success("删除成功") : ApiResult.error("删除失败");
    }

    @PostMapping("/batch-delete")
    public ApiResult<String> batchDelete(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) params.get("eventIds");
        int userId = getInt(params, "userId");
        int opCount = getInt(params, "opCount");

        if (ids == null || ids.isEmpty()) {
            return ApiResult.error("请选择要删除的事件");
        }

        int count = eventService.deleteEvents(ids);
        userService.updateOpCount(Math.max(0, opCount - ids.size()), userId);
        return ApiResult.success("成功删除 " + count + " 条记录");
    }

    @PostMapping("/batch-delete-by-range")
    public ApiResult<Integer> batchDeleteByRange(@RequestBody Map<String, Object> params) {
        int userId = getInt(params, "userId");
        int opCount = getInt(params, "opCount");
        int startYear = getInt(params, "startYear");
        int startMonth = getInt(params, "startMonth");
        int startDay = getInt(params, "startDay");
        int endYear = getInt(params, "endYear");
        int endMonth = getInt(params, "endMonth");
        int endDay = getInt(params, "endDay");

        int startDate = startYear * 10000 + startMonth * 100 + startDay;
        int endDate = endYear * 10000 + endMonth * 100 + endDay;
        if (startDate > endDate) {
            return ApiResult.error("开始日期不能晚于结束日期");
        }

        int count = eventService.deleteByDateRange(userId, startDate, endDate);
        userService.updateOpCount(Math.max(0, opCount - count), userId);
        return ApiResult.success("成功删除 " + count + " 条记录", count);
    }

    private String getStr(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : null;
    }

    private int getInt(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).intValue();
        try {
            return Integer.parseInt(v.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
