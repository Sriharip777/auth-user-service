package com.tcon.auth_user_service.user.service;

import com.tcon.auth_user_service.user.entity.StudentWishlist;
import com.tcon.auth_user_service.user.repository.StudentWishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentWishlistService {

    private final StudentWishlistRepository wishlistRepository;

    // toggle, returns true if now wishlisted, false if removed
    public boolean toggleWishlist(String userId, String courseId) {
        if (wishlistRepository.existsByUserIdAndCourseId(userId, courseId)) {
            wishlistRepository.deleteByUserIdAndCourseId(userId, courseId);
            return false;
        }
        StudentWishlist item = StudentWishlist.builder()
                .userId(userId)
                .courseId(courseId)
                .createdAt(LocalDateTime.now())
                .build();
        wishlistRepository.save(item);
        return true;
    }

    public List<String> getWishlistCourseIds(String userId) {
        return wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(StudentWishlist::getCourseId)
                .toList();
    }
}