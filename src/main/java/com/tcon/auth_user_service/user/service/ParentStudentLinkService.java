package com.tcon.auth_user_service.user.service;

import com.tcon.auth_user_service.user.entity.StudentProfile;
import com.tcon.auth_user_service.user.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParentStudentLinkService {

    private final StudentRepository studentRepository;

    public List<String> getChildIdsForParent(String parentId) {
        return studentRepository.findByParentId(parentId)
                .stream()
                .map(StudentProfile::getId)
                .toList();
    }

    public List<String> getParentIdsForStudent(String studentId) {
        return studentRepository.findById(studentId)
                .map(StudentProfile::getParentId)
                .map(List::of)
                .orElseGet(List::of);
    }
}
