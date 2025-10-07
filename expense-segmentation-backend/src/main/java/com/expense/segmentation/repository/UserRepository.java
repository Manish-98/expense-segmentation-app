package com.expense.segmentation.repository;

import com.expense.segmentation.model.User;
import com.expense.segmentation.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByStatus(UserStatus status);

    List<User> findByDepartmentId(UUID departmentId);

    List<User> findByRoleId(UUID roleId);

    List<User> findByDepartmentIdAndStatus(UUID departmentId, UserStatus status);

}
