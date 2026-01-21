package com.tcon.auth_user_service.user.repository;


import com.tcon.auth_user_service.user.entity.AdminProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends MongoRepository<AdminProfile, String> {

    Optional<AdminProfile> findByUserId(String userId);

    List<AdminProfile> findBySuperAdminTrue();
}


