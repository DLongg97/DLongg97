package com.example.demo.Controller;

import com.example.demo.DTO.Request.UserDTO;
import com.example.demo.DTO.Response.Response;
import com.example.demo.Service.UserService;


import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.example.demo.Common.ResponseCode.ERROR_CODE;
import static com.example.demo.Common.ResponseCode.SUCCESS_CODE;
import static com.example.demo.Common.ResponseMessage.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/get/{id}")
    public ResponseEntity<Response<UserDTO>> getUser(@PathVariable Long id) {
        try {
            UserDTO user = userService.getUserById(id);
            return ResponseEntity.ok().body(
                    new Response<>(SUCCESS_CODE, "User found", user)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, "User not found", null)
            );
        }
    }


    @PostMapping("/create")
    public ResponseEntity<Response<UserDTO>> createUser(@RequestBody UserDTO userDTO) {
        try {
            UserDTO user = userService.createUser(userDTO);
            return ResponseEntity.ok().body(
                    new Response<>(SUCCESS_CODE, CREATE_USER_SUCCESSFULLY, user)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, CREATE_USER_FAILED, null)
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().body(
                    new Response<>(SUCCESS_CODE, DELETE_USER_SUCCESSFULLY, null)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, DELETE_USER_FAILED, null)
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<UserDTO>> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        try {
            UserDTO updatedUser = userService.updateUser(id, userDTO);
            return ResponseEntity.ok().body(
                    new Response<>(SUCCESS_CODE, UPDATE_USER_SUCCESSFULLY, updatedUser)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, UPDATE_USER_FAILED, null)
            );
        }
    }

    //  @ApiOperation(value = "Get User by Id", response = UserDTO.class)
//        @GetMapping("/id")
//        public ResponseEntity<Response<UserDTO>> getUserById(@PathVariable Long id) {
//        try {
//            UserDTO user = userService.getUserById(id);
//            return ResponseEntity.ok().body(
//                    new Response<>(SUCCESS_CODE, "User found", user));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(
//                    new Response<>(ERROR_CODE, e.getMessage(), null));
//        }
//
//    }
 /*
  @GetMapping("/{id}")
    public void saveUser(@RequestBody UserDTO userDTO) {
        return userService.saveUsers(List<UserDTO> userDTOs);
    }
  */

 //   @ApiOperation(value = "Import users from an Excel file")
    @PostMapping("/import")
    public ResponseEntity<Response<String>> importUsers(@RequestParam("file") MultipartFile file) {
        try {
            userService.importUsers(file);
            return ResponseEntity.ok().body(
                    new Response<>(SUCCESS_CODE, IMPORT_USER_SUCCESSFULLY, null)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, IMPORT_USER_FAILED, e.getMessage())
            );
        }
    }

    @GetMapping("/export")
    public ResponseEntity<Response<String>> exportUsers() {
        try {
            userService.exportUsers();
            return ResponseEntity.ok().body(
                    new Response<>(SUCCESS_CODE, EXPORT_USER_SUCCESSFULLY, null)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, EXPORT_USER_FAILED, e.getMessage())
            );
        }
    }

    }


