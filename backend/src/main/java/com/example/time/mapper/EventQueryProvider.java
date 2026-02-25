package com.example.time.mapper;

import java.util.Map;

public class EventQueryProvider {

    private String buildWhereClause(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder("user_id = #{userId}");
        String name = (String) params.get("name");
        if (name != null && !name.trim().isEmpty()) {
            sb.append(" AND name LIKE CONCAT('%', #{name}, '%')");
        }
        Integer startDate = (Integer) params.get("startDate");
        if (startDate != null) {
            sb.append(" AND (year * 10000 + month * 100 + day) >= #{startDate}");
        }
        Integer endDate = (Integer) params.get("endDate");
        if (endDate != null) {
            sb.append(" AND (year * 10000 + month * 100 + day) <= #{endDate}");
        }
        Integer todayNum = (Integer) params.get("todayNum");
        String status = (String) params.get("status");
        if (todayNum != null && status != null && !status.trim().isEmpty()) {
            switch (status.trim().toLowerCase()) {
                case "today" -> sb.append(" AND (year * 10000 + month * 100 + day) = #{todayNum}");
                case "upcoming" -> sb.append(" AND (year * 10000 + month * 100 + day) > #{todayNum}");
                case "past" -> sb.append(" AND (year * 10000 + month * 100 + day) < #{todayNum}");
            }
        }
        return sb.toString();
    }

    public String queryEvents(Map<String, Object> params) {
        String where = buildWhereClause(params);
        Integer offset = (Integer) params.get("offset");
        Integer size = (Integer) params.get("size");
        int limit = (size != null && size > 0) ? size : 20;
        return "SELECT * FROM events WHERE " + where +
                " ORDER BY year DESC, month DESC, day DESC" +
                (offset != null ? " LIMIT " + offset + ", " + limit : "");
    }

    public String countEvents(Map<String, Object> params) {
        String where = buildWhereClause(params);
        return "SELECT COUNT(*) FROM events WHERE " + where;
    }
}
