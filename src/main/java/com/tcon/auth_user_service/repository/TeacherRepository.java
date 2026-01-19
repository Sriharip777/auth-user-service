package com.tcon.auth_user_service.repository;

import com.tcon.auth_user_service.entity.TeacherProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends MongoRepository<TeacherProfile, String> {

    Optional<TeacherProfile> findByUserId(String userId);

    void deleteByUserId(String userId);

    List<TeacherProfile> findByVerificationStatus(TeacherProfile.VerificationStatus status);

    @Query("{ $or: [ { 'expertise': { $regex: ?0, $options: 'i' } }, { 'user.firstName': { $regex: ?0, $options: 'i' } } ] }")
    List<TeacherProfile> searchTeachers(String keyword);
}
