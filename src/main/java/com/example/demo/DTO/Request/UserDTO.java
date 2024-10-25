package com.example.demo.DTO.Request;

import com.example.demo.Entity.User;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class UserDTO extends User {
    private Long roleId;
    private String username;

    private String email;

    private String password;
    private String firstName;
    private String lastName;

}