package com.tcon.auth_user_service.user.service;

import com.tcon.auth_user_service.user.dto.TeacherVerificationDto;
import com.tcon.auth_user_service.user.entity.TeacherVerification;
import com.tcon.auth_user_service.user.repository.TeacherProfileRepository;
import com.tcon.auth_user_service.user.repository.TeacherVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherVerificationService {

    private final TeacherVerificationRepository verificationRepository;
    private final TeacherProfileRepository teacherProfileRepository;

    /**
     * Teacher submits verification documents
     */
    @Transactional
    public TeacherVerificationDto submitVerification(String teacherUserId, TeacherVerificationDto dto) {
        log.info("Teacher {} submitting verification documents", teacherUserId);

        // Check if verification already exists
        if (verificationRepository.existsByTeacherUserIdAndStatus(teacherUserId, "PENDING")) {
            throw new IllegalArgumentException("Verification request already pending");
        }

        TeacherVerification verification = TeacherVerification.builder()
                .teacherUserId(teacherUserId)
                .documentUrls(dto.getDocumentUrls())
                .status("PENDING")
                .build();

        TeacherVerification saved = verificationRepository.save(verification);
        log.info("✅ Verification submitted with ID: {}", saved.getId());

        return mapToDto(saved);
    }

    /**
     * Get all pending verifications (Admin only)
     */
    @Transactional(readOnly = true)
    public List<TeacherVerificationDto> getPendingVerifications() {
        log.info("Fetching pending teacher verifications");

        List<TeacherVerification> verifications = verificationRepository.findByStatus("PENDING");
        log.info("Found {} pending verifications", verifications.size());

        return verifications.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Approve teacher verification (Admin only)
     */
    @Transactional
    public TeacherVerificationDto approveVerification(String verificationId, String reviewerUserId) {
        log.info("Admin {} approving verification: {}", reviewerUserId, verificationId);

        TeacherVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("Verification not found: " + verificationId));

        if (!"PENDING".equals(verification.getStatus())) {
            throw new IllegalStateException("Verification already processed");
        }

        verification.setStatus("APPROVED");
        verification.setReviewerUserId(reviewerUserId);
        verification.setReviewedAt(LocalDateTime.now());

        TeacherVerification saved = verificationRepository.save(verification);

        // Update teacher profile status
        updateTeacherProfileStatus(verification.getTeacherUserId(), "VERIFIED");

        log.info("✅ Verification approved for teacher: {}", verification.getTeacherUserId());

        return mapToDto(saved);
    }

    /**
     * Reject teacher verification (Admin only)
     */
    @Transactional
    public TeacherVerificationDto rejectVerification(String verificationId, String reviewerUserId, String reason) {
        log.info("Admin {} rejecting verification: {}", reviewerUserId, verificationId);

        TeacherVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("Verification not found: " + verificationId));

        if (!"PENDING".equals(verification.getStatus())) {
            throw new IllegalStateException("Verification already processed");
        }

        verification.setStatus("REJECTED");
        verification.setReviewerUserId(reviewerUserId);
        verification.setRejectionReason(reason);
        verification.setReviewedAt(LocalDateTime.now());

        TeacherVerification saved = verificationRepository.save(verification);

        // Update teacher profile status
        updateTeacherProfileStatus(verification.getTeacherUserId(), "REJECTED");

        log.info("❌ Verification rejected for teacher: {} - Reason: {}",
                verification.getTeacherUserId(), reason);

        return mapToDto(saved);
    }

    private void updateTeacherProfileStatus(String teacherUserId, String status) {
        teacherProfileRepository.findByUserId(teacherUserId)
                .ifPresent(profile -> {
                    profile.setVerificationStatus(status);
                    teacherProfileRepository.save(profile);
                    log.info("Updated teacher profile status to: {}", status);
                });
    }

    private TeacherVerificationDto mapToDto(TeacherVerification verification) {
        return TeacherVerificationDto.builder()
                .id(verification.getId())
                .teacherUserId(verification.getTeacherUserId())
                .documentUrls(verification.getDocumentUrls())
                .status(verification.getStatus())
                .reviewerUserId(verification.getReviewerUserId())
                .rejectionReason(verification.getRejectionReason())
                .reviewedAt(verification.getReviewedAt())
                .createdAt(verification.getCreatedAt())
                .build();
    }
}
