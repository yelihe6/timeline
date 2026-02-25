package com.example.time.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer userId;
    private String email;
    private String nickname;
    @JsonIgnore
    private String password;
    private String phone;
    private Integer operationCount;
}
