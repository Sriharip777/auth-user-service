package com.tcon.auth_user_service.repository;

import com.tcon.auth_user_service.entity.AdminProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends MongoRepository<AdminProfile, String> {

    Optional<AdminProfile> findByUserId(String userId);

    void deleteByUserId(String userId);
}
