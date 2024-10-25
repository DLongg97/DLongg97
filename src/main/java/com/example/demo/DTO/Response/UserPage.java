package com.example.demo.DTO.Response;

import com.example.demo.Entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserPage {
    private List<User> users;
    private int page;
    private int size;
    private long totalPages;
    private long totalElements;
}