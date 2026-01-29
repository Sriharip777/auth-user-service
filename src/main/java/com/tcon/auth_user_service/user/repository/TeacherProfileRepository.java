package com.tcon.auth_user_service.user.repository;

import com.tcon.auth_user_service.user.entity.TeacherProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherProfileRepository extends MongoRepository<TeacherProfile, String> {

    Optional<TeacherProfile> findByUserId(String userId);

    void deleteByUserId(String userId);

    List<TeacherProfile> findByVerificationStatus(String verificationStatus);

    List<TeacherProfile> findByIsAvailable(Boolean isAvailable);

    @Query("{ 'subjects': { $in: [?0] } }")
    List<TeacherProfile> findBySubject(String subject);

    @Query("{ 'averageRating': { $gte: ?0 } }")
    List<TeacherProfile> findByMinRating(Double minRating);

    List<TeacherProfile> findTop10ByOrderByAverageRatingDesc();
}
