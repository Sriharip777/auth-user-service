package com.tcon.auth_user_service.user.service;

import com.tcon.auth_user_service.event.TeacherApprovalEventPublisher;
import com.tcon.auth_user_service.user.dto.TeacherVerificationDto;
import com.tcon.auth_user_service.user.entity.TeacherProfile;
import com.tcon.auth_user_service.user.entity.TeacherVerification;
import com.tcon.auth_user_service.user.repository.TeacherRepository;
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
    private final TeacherRepository teacherRepository;
    private final TeacherApprovalEventPublisher approvalEventPublisher;

    @Transactional
    public TeacherVerificationDto submitVerification(String teacherUserId, List<String> documentUrls) {
        TeacherVerification verification = TeacherVerification.builder()
                .teacherUserId(teacherUserId)
                .documentUrls(documentUrls)
                .status("PENDING")
                .build();

        TeacherVerification saved = verificationRepository.save(verification);
        log.info("Verification submitted for teacher: {}", teacherUserId);
        return toDto(saved);
    }

    @Transactional
    public TeacherVerificationDto approveVerification(String verificationId, String reviewerUserId) {
        TeacherVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("Verification not found: " + verificationId));

        verification.setStatus("APPROVED");
        verification.setReviewerUserId(reviewerUserId);
        verification.setReviewedAt(LocalDateTime.now());

        TeacherVerification saved = verificationRepository.save(verification);

        // Update teacher profile
        TeacherProfile teacherProfile = teacherRepository.findByUserId(verification.getTeacherUserId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher profile not found"));

        teacherProfile.setVerificationStatus("VERIFIED");
        teacherRepository.save(teacherProfile);

        log.info("Verification approved for teacher: {}", verification.getTeacherUserId());

        // Publish event
        approvalEventPublisher.publishTeacherApproved(verification.getTeacherUserId());

        return toDto(saved);
    }

    @Transactional
    public TeacherVerificationDto rejectVerification(String verificationId, String reviewerUserId, String reason) {
        TeacherVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("Verification not found: " + verificationId));

        verification.setStatus("REJECTED");
        verification.setReviewerUserId(reviewerUserId);
        verification.setRejectionReason(reason);
        verification.setReviewedAt(LocalDateTime.now());

        TeacherVerification saved = verificationRepository.save(verification);

        // Update teacher profile
        TeacherProfile teacherProfile = teacherRepository.findByUserId(verification.getTeacherUserId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher profile not found"));

        teacherProfile.setVerificationStatus("REJECTED");
        teacherRepository.save(teacherProfile);

        log.info("Verification rejected for teacher: {} - Reason: {}", verification.getTeacherUserId(), reason);

        return toDto(saved);
    }

    public List<TeacherVerificationDto> getPendingVerifications() {
        return verificationRepository.findByStatus("PENDING").stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public TeacherVerificationDto getVerificationStatus(String teacherUserId) {
        TeacherVerification verification = verificationRepository.findByTeacherUserId(teacherUserId)
                .orElseThrow(() -> new IllegalArgumentException("Verification not found for teacher: " + teacherUserId));
        return toDto(verification);
    }

    private TeacherVerificationDto toDto(TeacherVerification verification) {
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

