package com.tcon.auth_user_service.user.migration;
import com.tcon.auth_user_service.curriculum.client.CurriculumServiceClient;
import com.tcon.auth_user_service.curriculum.dto.GradeDto;
import com.tcon.auth_user_service.curriculum.dto.SubjectDto;
import com.tcon.auth_user_service.curriculum.dto.TopicDto;
import com.tcon.auth_user_service.user.entity.TeacherProfile;
import com.tcon.auth_user_service.user.entity.TeachingArea;
import com.tcon.auth_user_service.user.repository.TeacherProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@Profile("teaching-area-migration")
@RequiredArgsConstructor
public class TeachingAreaMigrationRunner implements CommandLineRunner {

    private final TeacherProfileRepository teacherProfileRepository;
    private final CurriculumServiceClient curriculumClient; // define this Feign client

    @Override
    public void run(String... args) {
        log.info("🚀 Starting TeachingArea migration...");

        List<GradeDto> grades = curriculumClient.getGrades();
        Map<String, String> gradeNameToId = grades.stream()
                .collect(Collectors.toMap(GradeDto::getName, GradeDto::getId));

        List<SubjectDto> subjects = curriculumClient.getAllSubjects();
        Map<String, String> subjectKeyToId = subjects.stream()
                .collect(Collectors.toMap(
                        s -> s.getGradeId() + "|" + s.getName(),
                        SubjectDto::getId
                ));

        List<TopicDto> topics = curriculumClient.getAllTopics();
        Map<String, String> topicKeyToId = topics.stream()
                .collect(Collectors.toMap(
                        t -> t.getSubjectId() + "|" + t.getName(),
                        TopicDto::getId
                ));

        List<TeacherProfile> profiles = teacherProfileRepository.findAll();
        int updatedCount = 0;

        for (TeacherProfile profile : profiles) {
            boolean changed = false;

            if (profile.getTeachingAreas() != null) {
                for (TeachingArea area : profile.getTeachingAreas()) {
                    if (area.getGradeId() != null && area.getSubjectId() != null) continue;

                    String gradeId = gradeNameToId.get(area.getGrade());
                    if (gradeId == null) continue;

                    String subjectId = subjectKeyToId.get(gradeId + "|" + area.getSubject());
                    if (subjectId == null) continue;

                    area.setGradeId(gradeId);
                    area.setSubjectId(subjectId);

                    if (area.getTopics() != null && !area.getTopics().isEmpty()) {
                        List<String> topicIds = area.getTopics().stream()
                                .map(name -> topicKeyToId.get(subjectId + "|" + name))
                                .filter(Objects::nonNull)
                                .toList();
                        area.setTopicIds(topicIds);
                    }

                    changed = true;
                }
            }

            if (changed) {
                teacherProfileRepository.save(profile);
                updatedCount++;
            }
        }

        log.info("✅ TeachingArea migration complete. Profiles updated: {}", updatedCount);
    }
}