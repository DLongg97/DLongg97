package com.example.demo.DTO.Request;

import lombok.Data;

@Data
public class FilterRequest {
    private int page;
    private int size;
    private String username;
    private String email;
    private String role;

}