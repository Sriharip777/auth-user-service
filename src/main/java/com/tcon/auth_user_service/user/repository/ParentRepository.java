package com.tcon.auth_user_service.user.repository;

import com.tcon.auth_user_service.user.entity.ParentProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParentRepository extends MongoRepository<ParentProfile, String> {

    Optional<ParentProfile> findByUserId(String userId);

    boolean existsByUserId(String userId);
}

