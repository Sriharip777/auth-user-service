package com.tcon.auth_user_service.user.repository;

import com.tcon.auth_user_service.user.entity.AdminRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRoleRepository extends MongoRepository<AdminRole, String> {
    Optional<AdminRole> findByRoleName(String roleName);
    List<AdminRole> findByIsActive(Boolean isActive);
    boolean existsByRoleName(String roleName);
}
