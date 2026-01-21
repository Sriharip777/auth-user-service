package com.tcon.auth_user_service.user.repository;


import com.tcon.auth_user_service.user.entity.TeacherProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends MongoRepository<TeacherProfile, String> {

    Optional<TeacherProfile> findByUserId(String userId);

    List<TeacherProfile> findBySubjectsContainingIgnoreCase(String subject);

    List<TeacherProfile> findByVerificationStatus(String status);

    List<TeacherProfile> findByIsAvailableTrue();

    List<TeacherProfile> findByAverageRatingGreaterThanEqual(Double rating);
}

