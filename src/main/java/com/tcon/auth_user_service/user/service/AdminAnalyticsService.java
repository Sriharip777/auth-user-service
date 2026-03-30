package com.tcon.auth_user_service.user.service;
import com.tcon.auth_user_service.client.FinancialAnalyticsClient;
import com.tcon.auth_user_service.client.LearningAnalyticsClient;
import com.tcon.auth_user_service.client.dto.MonthlyClassStatClientDto;
import com.tcon.auth_user_service.client.dto.MonthlyRevenueStatClientDto;
import com.tcon.auth_user_service.client.dto.StudentBookingAnalyticsClientDto;
import com.tcon.auth_user_service.client.dto.TeacherBookingAnalyticsClientDto;
import com.tcon.auth_user_service.client.dto.TeacherEarningsAnalyticsClientDto;
import com.tcon.auth_user_service.user.dto.*;
import com.tcon.auth_user_service.user.entity.StudentProfile;
import com.tcon.auth_user_service.user.entity.TeacherProfile;
import com.tcon.auth_user_service.user.entity.User;
import com.tcon.auth_user_service.user.entity.UserRole;
import com.tcon.auth_user_service.user.repository.StudentRepository;
import com.tcon.auth_user_service.user.repository.TeacherProfileRepository;
import com.tcon.auth_user_service.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAnalyticsService {

    private final UserRepository userRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final StudentRepository studentRepository;
    private final LearningAnalyticsClient learningAnalyticsClient;
    private final FinancialAnalyticsClient financialAnalyticsClient;

    public AdminOverviewDto getOverview() {
        List<User> users = safeList(userRepository.findAll());
        List<MonthlyClassStatClientDto> classStats = getSafeOverviewClassStats();
        List<MonthlyRevenueStatClientDto> revenueStats = getSafeOverviewRevenueStats();

        Map<String, Integer> monthlyUsers = users.stream()
                .filter(u -> u.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        u -> monthLabel(u.getCreatedAt().getMonthValue()),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        Map<String, Integer> classesMap = classStats.stream()
                .filter(dto -> dto.getLabel() != null)
                .collect(Collectors.toMap(
                        MonthlyClassStatClientDto::getLabel,
                        dto -> dto.getClasses() != null ? dto.getClasses() : 0,
                        (a, b) -> a
                ));

        Map<String, Double> revenueMap = revenueStats.stream()
                .filter(dto -> dto.getLabel() != null)
                .collect(Collectors.toMap(
                        MonthlyRevenueStatClientDto::getLabel,
                        dto -> dto.getRevenue() != null ? dto.getRevenue() : 0.0,
                        (a, b) -> a
                ));

        List<String> labels = buildLastSixMonthLabels();

        List<OverviewPointDto> stats = labels.stream()
                .map(label -> OverviewPointDto.builder()
                        .label(label)
                        .users(monthlyUsers.getOrDefault(label, 0))
                        .classes(classesMap.getOrDefault(label, 0))
                        .revenue(revenueMap.getOrDefault(label, 0.0))
                        .build())
                .toList();

        return AdminOverviewDto.builder().stats(stats).build();
    }

    public List<TeacherAnalyticsDto> getTeachers(String search) {
        Map<String, TeacherProfile> teacherProfileMap = safeList(teacherProfileRepository.findAll()).stream()
                .filter(profile -> profile.getUserId() != null)
                .collect(Collectors.toMap(
                        TeacherProfile::getUserId,
                        Function.identity(),
                        (a, b) -> a
                ));

        Map<String, TeacherBookingAnalyticsClientDto> bookingMap = getSafeTeacherAnalytics().stream()
                .filter(dto -> dto.getTeacherId() != null)
                .collect(Collectors.toMap(
                        TeacherBookingAnalyticsClientDto::getTeacherId,
                        Function.identity(),
                        (a, b) -> a
                ));

        Map<String, TeacherEarningsAnalyticsClientDto> earningsMap = getSafeTeacherEarnings().stream()
                .filter(dto -> dto.getTeacherId() != null)
                .collect(Collectors.toMap(
                        TeacherEarningsAnalyticsClientDto::getTeacherId,
                        Function.identity(),
                        (a, b) -> a
                ));

        return safeList(userRepository.findAll()).stream()
                .filter(user -> user.getRole() == UserRole.TEACHER)
                .map(user -> {
                    TeacherProfile profile = teacherProfileMap.get(user.getId());
                    TeacherBookingAnalyticsClientDto booking = bookingMap.get(user.getId());
                    TeacherEarningsAnalyticsClientDto earnings = earningsMap.get(user.getId());

                    return TeacherAnalyticsDto.builder()
                            .id(user.getId())
                            .name(buildName(user))
                            .subject(profile != null && profile.getSubjects() != null && !profile.getSubjects().isEmpty()
                                    ? String.join(", ", profile.getSubjects())
                                    : "N/A")
                            .rating(profile != null && profile.getAverageRating() != null ? profile.getAverageRating() : 0.0)
                            .students(booking != null && booking.getUniqueStudents() != null ? booking.getUniqueStudents() : 0)
                            .classes(booking != null && booking.getCompletedClasses() != null ? booking.getCompletedClasses() : 0)
                            .earnings(earnings != null && earnings.getEarnings() != null ? earnings.getEarnings() : 0.0)
                            .attendance(100)
                            .issues(buildTeacherIssues(profile, booking))
                            .status(profile != null && "VERIFIED".equalsIgnoreCase(profile.getVerificationStatus()) ? "active" : "pending")
                            .build();
                })
                .filter(t -> matchesSearch(search, t.getName(), t.getSubject(), t.getStatus()))
                .toList();
    }

    public List<StudentAnalyticsDto> getStudents(String search) {
        Map<String, StudentProfile> studentProfileMap = safeList(studentRepository.findAll()).stream()
                .filter(profile -> profile.getUserId() != null)
                .collect(Collectors.toMap(
                        StudentProfile::getUserId,
                        Function.identity(),
                        (a, b) -> a
                ));

        Map<String, StudentBookingAnalyticsClientDto> bookingMap = getSafeStudentAnalytics().stream()
                .filter(dto -> dto.getStudentId() != null)
                .collect(Collectors.toMap(
                        StudentBookingAnalyticsClientDto::getStudentId,
                        Function.identity(),
                        (a, b) -> a
                ));

        return safeList(userRepository.findAll()).stream()
                .filter(user -> user.getRole() == UserRole.STUDENT)
                .map(user -> {
                    StudentProfile profile = studentProfileMap.get(user.getId());
                    StudentBookingAnalyticsClientDto booking = bookingMap.get(user.getId());

                    int completedClasses = booking != null && booking.getCompletedClasses() != null ? booking.getCompletedClasses() : 0;
                    int totalMinutes = booking != null && booking.getTotalMinutesLearned() != null ? booking.getTotalMinutesLearned() : 0;
                    int hoursLearned = totalMinutes / 60;

                    int progress = 0;
                    if (profile != null && profile.getEnrolledCourses() != null && !profile.getEnrolledCourses().isEmpty()) {
                        progress = Math.min(100, completedClasses * 10);
                    }

                    return StudentAnalyticsDto.builder()
                            .id(user.getId())
                            .name(buildName(user))
                            .grade(profile != null && profile.getGradeLevel() != null ? profile.getGradeLevel() : "N/A")
                            .subjects(profile != null && profile.getInterests() != null ? profile.getInterests() : List.of())
                            .hoursLearned(hoursLearned)
                            .attendance(100)
                            .progress(progress)
                            .issues(buildStudentIssues(booking))
                            .status("active")
                            .build();
                })
                .filter(s -> matchesSearch(search, s.getName(), s.getGrade(), String.join(" ", s.getSubjects()), s.getStatus()))
                .toList();
    }

    public List<AnalyticsIssueDto> getIssues(String search) {
        List<AnalyticsIssueDto> issues = new ArrayList<>();

        safeList(teacherProfileRepository.findAll()).stream()
                .filter(tp -> tp.getVerificationStatus() != null && !"VERIFIED".equalsIgnoreCase(tp.getVerificationStatus()))
                .forEach(tp -> {
                    User user = userRepository.findById(tp.getUserId()).orElse(null);
                    issues.add(AnalyticsIssueDto.builder()
                            .id("teacher-verification-" + tp.getUserId())
                            .type("verification")
                            .user(user != null ? buildName(user) : tp.getUserId())
                            .description("Teacher verification pending or rejected")
                            .date(tp.getUpdatedAt() != null ? tp.getUpdatedAt().toLocalDate().toString() : "N/A")
                            .status("open")
                            .build());
                });

        return issues.stream()
                .filter(i -> matchesSearch(search, i.getUser(), i.getDescription(), i.getType(), i.getStatus()))
                .toList();
    }

    private List<MonthlyClassStatClientDto> getSafeOverviewClassStats() {
        try {
            return safeList(learningAnalyticsClient.getOverviewClassStats());
        } catch (Exception ex) {
            log.error("Failed to fetch class stats", ex);
            return List.of();
        }
    }

    private List<MonthlyRevenueStatClientDto> getSafeOverviewRevenueStats() {
        try {
            return safeList(financialAnalyticsClient.getOverviewRevenue());
        } catch (Exception ex) {
            log.error("Failed to fetch revenue stats", ex);
            return List.of();
        }
    }

    private List<TeacherBookingAnalyticsClientDto> getSafeTeacherAnalytics() {
        try {
            return safeList(learningAnalyticsClient.getTeacherAnalytics());
        } catch (Exception ex) {
            log.error("Failed to fetch teacher booking analytics", ex);
            return List.of();
        }
    }

    private List<StudentBookingAnalyticsClientDto> getSafeStudentAnalytics() {
        try {
            return safeList(learningAnalyticsClient.getStudentAnalytics());
        } catch (Exception ex) {
            log.error("Failed to fetch student booking analytics", ex);
            return List.of();
        }
    }

    private List<TeacherEarningsAnalyticsClientDto> getSafeTeacherEarnings() {
        try {
            return safeList(financialAnalyticsClient.getTeacherEarnings());
        } catch (Exception ex) {
            log.error("Failed to fetch teacher earnings analytics", ex);
            return List.of();
        }
    }

    private List<String> buildTeacherIssues(TeacherProfile profile, TeacherBookingAnalyticsClientDto booking) {
        List<String> issues = new ArrayList<>();
        if (profile == null) {
            issues.add("Teacher profile missing");
            return issues;
        }
        if (profile.getVerificationStatus() != null && !"VERIFIED".equalsIgnoreCase(profile.getVerificationStatus())) {
            issues.add("Verification pending");
        }
        if (booking != null && booking.getCancelledClasses() != null && booking.getCancelledClasses() >= 3) {
            issues.add("High booking cancellation rate");
        }
        return issues;
    }

    private List<String> buildStudentIssues(StudentBookingAnalyticsClientDto booking) {
        List<String> issues = new ArrayList<>();
        if (booking != null && booking.getCancelledClasses() != null && booking.getCancelledClasses() >= 3) {
            issues.add("Frequent class cancellations");
        }
        if (booking != null && booking.getCompletedClasses() != null && booking.getCompletedClasses() == 0) {
            issues.add("No completed classes yet");
        }
        return issues;
    }

    private boolean matchesSearch(String search, String... values) {
        if (search == null || search.isBlank()) return true;
        String q = search.toLowerCase();
        return Arrays.stream(values)
                .filter(Objects::nonNull)
                .anyMatch(v -> v.toLowerCase().contains(q));
    }

    private String buildName(User user) {
        String fullName = ((user.getFirstName() != null ? user.getFirstName() : "") + " " +
                (user.getLastName() != null ? user.getLastName() : "")).trim();
        return fullName.isBlank() ? user.getEmail() : fullName;
    }

    private <T> List<T> safeList(List<T> input) {
        return input != null ? input : List.of();
    }

    private String monthLabel(int month) {
        return switch (month) {
            case 1 -> "Jan";
            case 2 -> "Feb";
            case 3 -> "Mar";
            case 4 -> "Apr";
            case 5 -> "May";
            case 6 -> "Jun";
            case 7 -> "Jul";
            case 8 -> "Aug";
            case 9 -> "Sep";
            case 10 -> "Oct";
            case 11 -> "Nov";
            case 12 -> "Dec";
            default -> "N/A";
        };
    }

    private List<String> buildLastSixMonthLabels() {
        List<String> labels = new ArrayList<>();
        java.time.YearMonth now = java.time.YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            labels.add(monthLabel(now.minusMonths(i).getMonthValue()));
        }
        return labels;
    }
}