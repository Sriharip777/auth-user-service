package com.tcon.auth_user_service.user.repository;

import com.tcon.auth_user_service.user.entity.User;
import com.tcon.auth_user_service.user.entity.UserRole;
import com.tcon.auth_user_service.user.entity.UserStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    // Add this missing method
    List<User> findByRole(UserRole role);

    List<User> findByStatus(UserStatus status);

    List<User> findByCreatedAtAfter(LocalDateTime date);

    List<User> findByLockedUntilBefore(LocalDateTime now);

    // Advanced search queries
    @Query("{'firstName': {$regex: ?0, $options: 'i'}}")
    List<User> searchByFirstName(String firstName);

    @Query("{'lastName': {$regex: ?0, $options: 'i'}}")
    List<User> searchByLastName(String lastName);

    @Query("{$or: [{'firstName': {$regex: ?0, $options: 'i'}}, {'lastName': {$regex: ?0, $options: 'i'}}, {'email': {$regex: ?0, $options: 'i'}}]}")
    List<User> searchByKeyword(String keyword);
}
