package com.example.demo.Repository;

import com.example.demo.Entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.junit.internal.requests.FilterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);



 //   Page<User> findByUsernameContaining(String username, Pageable pageable);
 @Query("SELECT u FROM User u " +
         "WHERE " +
         "(:#{#request.username} IS NULL OR :#{#request.username} = '' OR lower(u.username) LIKE concat('%', lower(:#{#request.username}), '%')) AND " +
         "(:#{#request.email} IS NULL OR :#{#request.email} = '' OR lower(u.email) LIKE concat('%', lower(:#{#request.email}), '%')) AND " +
         "(:#{#request.role} IS NULL OR :#{#request.role} = '' OR lower(u.role) LIKE concat('%', lower(:#{#request.role}), '%'))"
 )
    Page<User> findByFilter(FilterRequest request, Pageable pageable);


    @Query("select u.email from User u ")
    Set<String> findAllEmail();

    Optional<User> findByUsername(String username);




}