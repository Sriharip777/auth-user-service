package com.tcon.auth_user_service.user.repository;

import com.tcon.auth_user_service.user.entity.StudentProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends MongoRepository<StudentProfile, String> {

    Optional<StudentProfile> findByUserId(String userId);

    List<StudentProfile> findByGradeLevel(String gradeLevel);

    List<StudentProfile> findByInterestsContaining(String interest);

    List<StudentProfile> findBySchoolName(String schoolName);

    // one parent per student
    List<StudentProfile> findByParentId(String parentId);
}
