package com.tcon.auth_user_service.user.repository;


import com.tcon.auth_user_service.user.entity.TeacherVerification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherVerificationRepository extends MongoRepository<TeacherVerification, String> {

    Optional<TeacherVerification> findByTeacherUserId(String teacherUserId);

    List<TeacherVerification> findByStatus(String status);

    List<TeacherVerification> findByReviewerUserId(String reviewerUserId);
}
