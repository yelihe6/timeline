package com.example.time.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private Integer eventId;
    private Integer userId;
    private String name;
    private String description;
    private Integer year;
    private Integer month;
    private Integer day;
}
