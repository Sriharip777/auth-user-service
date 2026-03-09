package com.tcon.auth_user_service.user.repository;


import com.tcon.auth_user_service.user.entity.StudentWishlist;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StudentWishlistRepository extends MongoRepository<StudentWishlist, String> {

    boolean existsByUserIdAndCourseId(String userId, String courseId);

    void deleteByUserIdAndCourseId(String userId, String courseId);

    List<StudentWishlist> findByUserIdOrderByCreatedAtDesc(String userId);
}