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

    /* =====================================================
       TEACHER SUBMITS VERIFICATION
       ===================================================== */
    @Transactional
    public TeacherVerificationDto submitVerification(
            String teacherUserId,
            TeacherVerificationDto dto
    ) {
        log.info("Teacher {} uploading verification documents", teacherUserId);

        TeacherVerification verification = verificationRepository
                .findByTeacherUserId(teacherUserId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Verification record not found. Teacher profile not initialized properly."
                        )
                );

        if (!"PENDING".equals(verification.getStatus())) {
            throw new IllegalStateException("Verification already processed");
        }

        verification.setDocumentUrls(dto.getDocumentUrls());

        TeacherVerification saved = verificationRepository.save(verification);

        log.info("✅ Verification documents updated for teacher: {}", teacherUserId);
        return safeMapToDto(saved);
    }

    /* =====================================================
       ADMIN: GET PENDING VERIFICATIONS (LEGACY SUPPORT)
       ===================================================== */
    @Transactional(readOnly = true)
    public List<TeacherVerificationDto> getPendingVerifications() {
        log.info("Fetching pending teacher verifications");

        List<TeacherVerification> verifications =
                verificationRepository.findByStatus("PENDING");

        return verifications.stream()
                .map(this::safeMapToDto)
                .collect(Collectors.toList());
    }

    /* =====================================================
       ADMIN: GET VERIFICATIONS BY STATUS (NEW)
       ===================================================== */
    @Transactional(readOnly = true)
    public List<TeacherVerificationDto> getVerificationsByStatus(String status) {

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status parameter is required");
        }

        String normalizedStatus = status.toUpperCase();

        if (!List.of("PENDING", "APPROVED", "REJECTED").contains(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid verification status: " + status);
        }

        log.info("Fetching teacher verifications with status: {}", normalizedStatus);

        List<TeacherVerification> verifications =
                verificationRepository.findByStatus(normalizedStatus);

        return verifications.stream()
                .map(this::safeMapToDto)
                .collect(Collectors.toList());
    }

    /* =====================================================
       ADMIN: APPROVE VERIFICATION
       ===================================================== */
    @Transactional
    public TeacherVerificationDto approveVerification(
            String verificationId,
            String reviewerUserId
    ) {
        log.info("Admin {} approving verification: {}", reviewerUserId, verificationId);

        TeacherVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Verification not found: " + verificationId)
                );

        if (!"PENDING".equals(verification.getStatus())) {
            throw new IllegalStateException("Verification already processed");
        }

        verification.setStatus("APPROVED");
        verification.setReviewerUserId(reviewerUserId);
        verification.setReviewedAt(LocalDateTime.now());

        TeacherVerification saved = verificationRepository.save(verification);

        updateTeacherProfileStatus(verification.getTeacherUserId(), "VERIFIED");

        log.info("✅ Verification approved for teacher: {}", verification.getTeacherUserId());
        return safeMapToDto(saved);
    }

    /* =====================================================
       ADMIN: REJECT VERIFICATION
       ===================================================== */
    @Transactional
    public TeacherVerificationDto rejectVerification(
            String verificationId,
            String reviewerUserId,
            String reason
    ) {
        log.info("Admin {} rejecting verification: {}", reviewerUserId, verificationId);

        TeacherVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Verification not found: " + verificationId)
                );

        if (!"PENDING".equals(verification.getStatus())) {
            throw new IllegalStateException("Verification already processed");
        }

        verification.setStatus("REJECTED");
        verification.setReviewerUserId(reviewerUserId);
        verification.setRejectionReason(reason);
        verification.setReviewedAt(LocalDateTime.now());

        TeacherVerification saved = verificationRepository.save(verification);

        updateTeacherProfileStatus(verification.getTeacherUserId(), "REJECTED");

        log.info("❌ Verification rejected for teacher: {}", verification.getTeacherUserId());
        return safeMapToDto(saved);
    }

    /* =====================================================
       UPDATE TEACHER PROFILE STATUS
       ===================================================== */
    private void updateTeacherProfileStatus(String teacherUserId, String status) {
        teacherProfileRepository.findByUserId(teacherUserId)
                .ifPresent(profile -> {
                    profile.setVerificationStatus(status);
                    teacherProfileRepository.save(profile);
                    log.info("Updated teacher profile status to: {}", status);
                });
    }

    /* =====================================================
       SAFE DTO MAPPER (NULL-PROOF)
       ===================================================== */
    private TeacherVerificationDto safeMapToDto(TeacherVerification verification) {
        return TeacherVerificationDto.builder()
                .id(verification.getId())
                .teacherUserId(verification.getTeacherUserId())
                .documentUrls(
                        verification.getDocumentUrls() == null
                                ? List.of()
                                : verification.getDocumentUrls()
                )
                .status(
                        verification.getStatus() == null
                                ? "PENDING"
                                : verification.getStatus()
                )
                .reviewerUserId(verification.getReviewerUserId())
                .rejectionReason(verification.getRejectionReason())
                .reviewedAt(verification.getReviewedAt())
                .createdAt(verification.getCreatedAt())
                .build();
    }
}