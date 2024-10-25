package com.example.demo.Service;

import com.example.demo.DTO.Request.UserDTO;
import com.example.demo.Entity.User;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.junit.internal.requests.FilterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {
    @Transactional
    UserDTO updateUser(Long id, UserDTO userDTO);
    UserDTO createUser(UserDTO userDTO);

    UserDTO getUserById(Long id);

    Page<User> getAllUsers(FilterRequest filterRequest, Pageable pageable);
    void deleteUser(Long id);
    void saveUsers(List<UserDTO> userDTOs);

    void importUsers(MultipartFile file) throws IOException;

    void exportUsers();



}
