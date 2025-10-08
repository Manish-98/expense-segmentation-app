package com.expense.segmentation.service;

import com.expense.segmentation.dto.RoleResponse;
import com.expense.segmentation.exception.ResourceNotFoundException;
import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.repository.RoleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream().map(this::mapToRoleResponse).toList();
    }

    /**
     * Gets a role entity by name. Used internally by other services.
     *
     * @param roleType the role type
     * @return the role entity
     * @throws ResourceNotFoundException if role not found
     */
    @Transactional(readOnly = true)
    public Role getRoleByName(RoleType roleType) {
        return roleRepository
                .findByName(roleType)
                .orElseThrow(
                        () -> {
                            log.error("Role not found: {}", roleType);
                            return new ResourceNotFoundException(
                                    "Role", "name", roleType.toString());
                        });
    }

    private RoleResponse mapToRoleResponse(Role role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setName(role.getName());
        response.setDescription(role.getDescription());
        return response;
    }
}
