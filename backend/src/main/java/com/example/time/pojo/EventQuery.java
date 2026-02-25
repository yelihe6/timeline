package com.example.time.pojo;

import lombok.Data;

@Data
public class EventQuery {
    private Integer userId;
    private Integer page = 1;
    private Integer size = 10;
    private String name;
    private Integer year;
    private Integer month;
    private Integer day;
    private Integer startYear;
    private Integer startMonth;
    private Integer startDay;
    private Integer endYear;
    private Integer endMonth;
    private Integer endDay;
    private String status;
}
