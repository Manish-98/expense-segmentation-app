package com.expense.segmentation.repository;

import com.expense.segmentation.model.User;
import com.expense.segmentation.model.UserStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByStatus(UserStatus status);

    List<User> findByDepartmentId(UUID departmentId);

    List<User> findByRoleId(UUID roleId);

    List<User> findByDepartmentIdAndStatus(UUID departmentId, UserStatus status);

    /**
     * Fetches all users with their role and department eagerly loaded using JOIN FETCH. This
     * prevents N+1 query problems when accessing user.role and user.department.
     *
     * @return list of all users with eagerly loaded relationships
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department LEFT JOIN FETCH u.role")
    List<User> findAllWithDepartmentAndRole();

    /**
     * Fetches users by department ID with their role eagerly loaded using JOIN FETCH. This prevents
     * N+1 query problems when accessing user.role.
     *
     * @param departmentId the department ID to filter by
     * @return list of users in the department with eagerly loaded role
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.department.id = :departmentId")
    List<User> findByDepartmentIdWithRole(@Param("departmentId") UUID departmentId);

    /**
     * Fetches a user by ID with their role and department eagerly loaded using JOIN FETCH. This
     * prevents N+1 query problems when accessing user.role and user.department.
     *
     * @param id the user ID
     * @return optional containing the user with eagerly loaded relationships
     */
    @Query(
            "SELECT u FROM User u LEFT JOIN FETCH u.department LEFT JOIN FETCH u.role WHERE"
                    + " u.id = :id")
    Optional<User> findByIdWithDepartmentAndRole(@Param("id") UUID id);

    /**
     * Fetches a user by email with their role and department eagerly loaded using JOIN FETCH. This
     * prevents N+1 query problems when accessing user.role and user.department.
     *
     * @param email the user email
     * @return optional containing the user with eagerly loaded relationships
     */
    @Query(
            "SELECT u FROM User u LEFT JOIN FETCH u.department LEFT JOIN FETCH u.role WHERE"
                    + " u.email = :email")
    Optional<User> findByEmailWithDepartmentAndRole(@Param("email") String email);
}
