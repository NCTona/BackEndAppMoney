package com.example.expensestracker.repositories;

import com.example.expensestracker.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);
    //select * from users where phoneNumber=?
    @Query("SELECT u.userId FROM UserEntity u")
    List<Long> findAllUserIds();
    Optional<UserEntity> findByEmail(String email);
}
