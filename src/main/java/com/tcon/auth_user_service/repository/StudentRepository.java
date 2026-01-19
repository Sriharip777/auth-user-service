package com.tcon.auth_user_service.repository;

import com.tcon.auth_user_service.entity.StudentProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends MongoRepository<StudentProfile, String> {

    Optional<StudentProfile> findByUserId(String userId);

    void deleteByUserId(String userId);
}
