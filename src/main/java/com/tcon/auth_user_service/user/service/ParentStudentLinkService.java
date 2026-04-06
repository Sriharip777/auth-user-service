package com.tcon.auth_user_service.user.service;

import com.tcon.auth_user_service.user.entity.ParentProfile;
import com.tcon.auth_user_service.user.entity.StudentProfile;
import com.tcon.auth_user_service.user.repository.ParentRepository;
import com.tcon.auth_user_service.user.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParentStudentLinkService {

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;

    public List<String> getChildIdsForParent(String parentId) {
        Set<String> childIds = new LinkedHashSet<>();

        List<StudentProfile> students = studentRepository.findByParentId(parentId);
        if (students != null) {
            students.stream()
                    .map(StudentProfile::getUserId)
                    .filter(id -> id != null && !id.isBlank())
                    .forEach(childIds::add);
        }

        parentRepository.findByUserId(parentId)
                .map(ParentProfile::getChildUserIds)
                .ifPresent(ids -> ids.stream()
                        .filter(id -> id != null && !id.isBlank())
                        .forEach(childIds::add));

        log.info("Resolved child ids for parentId={} -> {}", parentId, childIds);
        return List.copyOf(childIds);
    }

    public List<String> getParentIdsForStudent(String studentId) {
        return studentRepository.findById(studentId)
                .map(StudentProfile::getParentId)
                .map(List::of)
                .orElseGet(List::of);
    }
}