package com.example.demo.Service.Impl;

import com.example.demo.DTO.Request.UserDTO;
import com.example.demo.Entity.Role;
import com.example.demo.Entity.User;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Service.UserService;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.internal.requests.FilterRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;


    // tạo người dùng mới
    @Transactional
    @Override
    public UserDTO createUser(UserDTO userDTO) {
        // kiểm tra sự trùng lặp email
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại: " + userDTO.getEmail());
        }
        // tạo đối tượng user mới
        // ánh xạ dữ liệu từ userDTO sang đối tượng user mới : modelMapper
        User user = modelMapper.map(userDTO, User.class);

         user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

         PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
         user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);

        return modelMapper.map(userRepository.save(user), UserDTO.class);
    }


    @Transactional
    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        // tìm người dùng có id tương ứng
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));


        // Kiểm tra xem email người dùng hiện tại có khác không
        if (!existingUser.getEmail().equals(userDTO.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new IllegalArgumentException("Email đã tồn tại: " + userDTO.getEmail());
            }
        }
        // cập nhật thông tin người dùng
        existingUser.setUsername(userDTO.getUsername());
        existingUser.setEmail(userDTO.getEmail());
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
           existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
       }
        // cập nhật vai trò của người dùng
        if (userDTO.getRoleId() != null) {
            Role role = roleRepository.findById(userDTO.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            existingUser.setRole(role);
        }
        // Lưu và trả về thông tin người dùng đã cập nhật
        return modelMapper.map(userRepository.save(existingUser), UserDTO.class);
    }


    public void saveUsers(List<UserDTO> userDTOs) {
        Set<String> existingEmails = new HashSet<>();

        List<User> users = userDTOs.stream()
                .peek(userDTO -> {
                    if (!existingEmails.add(userDTO.getEmail())) {
                        throw new IllegalArgumentException("Email bị trùng: " + userDTO.getEmail());
                    }
                })
               .map(userDTO -> {
                    User user = modelMapper.map(userDTO, User.class);
               user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                    return user;
                })
                .collect(Collectors.toList());

    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private UserDTO convertToDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }


    @Override
    public UserDTO getUserById(Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDTO(existingUser);
    }

    @Override
    public Page<User> getAllUsers(FilterRequest filterRequest, Pageable pageable) {
        return userRepository.findByFilter(filterRequest, pageable);
    }


    @Transactional
    @Override
    public void importUsers(MultipartFile file) throws IOException {
        InputStream excelStream = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(excelStream);
        Sheet sheet = workbook.getSheetAt(0);

        Set<String> fileEmails = new HashSet<>();
        Set<String> dbEmails = new HashSet<>(userRepository.findAllEmail());

        Map<String, Integer> emailToRowMap = new HashMap<>();
        List<String[]> errors = new ArrayList<>();
        List<UserDTO> userDTOs = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            String email = row.getCell(2).getStringCellValue();

            // Kiểm tra trùng lặp email trong file Excel
            if (fileEmails.contains(email)) {
                int duplicateRow = emailToRowMap.get(email);
                errors.add(new String[]{String.valueOf(i + 1), "Trùng email với hàng " + (duplicateRow + 1)});
                errors.add(new String[]{String.valueOf(duplicateRow + 1), "Trùng email với hàng " + (i + 1)});
            } else {
                fileEmails.add(email);
                emailToRowMap.put(email, i);
            }
            // Kiểm tra trùng lặp email với database
            if (dbEmails.contains(email)) {
                errors.add(new String[]{String.valueOf(i + 1), "Trùng email với database"});
            }
            // Nếu không có lỗi, thêm vào danh sách DTO
            if (!fileEmails.contains(email) && !dbEmails.contains(email)) {
                UserDTO userDTO = new UserDTO();
                userDTO.setUsername(row.getCell(0).getStringCellValue());
                userDTO.setPassword(row.getCell(1).getStringCellValue());
                userDTO.setEmail(email);
                userDTO.setRoleId((long) row.getCell(3).getNumericCellValue());
                userDTOs.add(userDTO);
            }
        }

        // Nếu có lỗi thì tạo file lỗi và trả về
        if (!errors.isEmpty()) {
            workbook.close();
            throw new IOException("Nhập không thành công ");
        }

        saveUsers(userDTOs);

        workbook.close();
    }


    private ByteArrayInputStream generateErrorExcel(List<String[]> errors) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Errors");

        int rowIdx = 0;
        for (String[] error : errors) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue("Hàng số");
            row.createCell(1).setCellValue("Lỗi");
            row.createCell(0).setCellValue(error[0]);
            row.createCell(1).setCellValue(error[1]);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    //expoert
    @Override
    public void exportUsers() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Users");
            List<User> users = userRepository.findAll();
            int rowIndex = 0;
            for (User user : users) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(user.getUsername());
                row.createCell(1).setCellValue(user.getEmail());
                row.createCell(2).setCellValue(user.getRole().getId());
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to export users", e);
        }
    }



}

