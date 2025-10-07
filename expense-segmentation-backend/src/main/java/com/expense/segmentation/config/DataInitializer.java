package com.expense.segmentation.config;

import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        initializeRoles();
    }

    private void initializeRoles() {
        log.info("Initializing default roles...");

        for (RoleType roleType : RoleType.values()) {
            if (!roleRepository.existsByName(roleType)) {
                Role role = new Role();
                role.setName(roleType);
                role.setDescription(getDescriptionForRole(roleType));
                roleRepository.save(role);
                log.info("Created role: {}", roleType);
            }
        }

        log.info("Role initialization completed");
    }

    private String getDescriptionForRole(RoleType roleType) {
        return switch (roleType) {
            case EMPLOYEE -> "Regular employee with basic access";
            case MANAGER -> "Department manager with team management access";
            case FINANCE -> "Finance team member with expense approval access";
            case ADMIN -> "Administrator with full system access";
            case OWNER -> "Business owner with complete control";
        };
    }

}
