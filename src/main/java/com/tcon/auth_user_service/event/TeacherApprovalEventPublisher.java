package com.tcon.auth_user_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeacherApprovalEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "teacher-approval-events";

    public void publishTeacherApproved(String teacherUserId) {
        TeacherApprovalEvent event = TeacherApprovalEvent.builder()
                .teacherUserId(teacherUserId)
                .status("APPROVED")
                .timestamp(LocalDateTime.now())
                .eventType("TEACHER_APPROVED")
                .build();

        kafkaTemplate.send(TOPIC, teacherUserId, event);
        log.info("Published TeacherApprovedEvent for teacher: {}", teacherUserId);
    }

    public void publishTeacherRejected(String teacherUserId, String reviewerUserId) {
        TeacherApprovalEvent event = TeacherApprovalEvent.builder()
                .teacherUserId(teacherUserId)
                .status("REJECTED")
                .reviewerUserId(reviewerUserId)
                .timestamp(LocalDateTime.now())
                .eventType("TEACHER_REJECTED")
                .build();

        kafkaTemplate.send(TOPIC, teacherUserId, event);
        log.info("Published TeacherRejectedEvent for teacher: {}", teacherUserId);
    }
}

