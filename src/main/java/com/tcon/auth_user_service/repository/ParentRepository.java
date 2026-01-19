package com.tcon.auth_user_service.repository;

import com.tcon.auth_user_service.entity.ParentProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParentRepository extends MongoRepository<ParentProfile, String> {

    Optional<ParentProfile> findByUserId(String userId);

    void deleteByUserId(String userId);
}
