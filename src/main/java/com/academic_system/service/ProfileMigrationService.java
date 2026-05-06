package com.academic_system.service;

import com.academic_system.entity.postgres.Student;
import com.academic_system.entity.postgres.Teacher;
import com.academic_system.entity.postgres.User;
import com.academic_system.entity.postgres.UserProfile;
import com.academic_system.repository.postgres.StudentRepository;
import com.academic_system.repository.postgres.TeacherRepository;
import com.academic_system.repository.postgres.UserProfileRepository;
import com.academic_system.repository.postgres.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileMigrationService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    /**
     * Migrate existing student and teacher data to UserProfile based on user_id.
     * This should be run once during deployment.
     * Returns a map with migration results for API response.
     */
    @Transactional
    public Map<String, Object> migrateExistingProfiles() {
        log.info("Starting profile migration...");
        
        int studentMigrated = migrateStudents();
        int teacherMigrated = migrateTeachers();
        
        log.info("Profile migration completed. Students migrated: {}, Teachers migrated: {}", 
                studentMigrated, teacherMigrated);
        
        Map<String, Object> result = new HashMap<>();
        result.put("studentsMigrated", studentMigrated);
        result.put("teachersMigrated", teacherMigrated);
        result.put("total", studentMigrated + teacherMigrated);
        result.put("message", "Migración completada. Se migraron " + 
                (studentMigrated + teacherMigrated) + " perfiles en total.");
        
        return result;
    }

    private int migrateStudents() {
        List<Student> students = studentRepository.findAll();
        int count = 0;
        
        for (Student student : students) {
            if (student.getUserId() == null) {
                continue;
            }
            
            User user = userRepository.findById(student.getUserId()).orElse(null);
            if (user == null) {
                continue;
            }
            
            if (userProfileRepository.findByUserId(user.getId()).isPresent()) {
                continue;
            }
            
            UserProfile profile = UserProfile.builder()
                    .user(user)
                    .firstName(student.getFirstName())
                    .lastName(student.getLastName())
                    .curp(student.getCurp())
                    .phone(student.getPhone())
                    .institutionalEmail(student.getInstitutionalEmail())
                    .birthDate(student.getBirthDate())
                    .gender(student.getGender())
                    .enrollmentNumber(student.getEnrollmentNumber())
                    .build();
            
            try {
                userProfileRepository.save(profile);
                count++;
                log.info("Migrated student profile for user_id: {}", user.getId());
            } catch (Exception e) {
                log.error("Failed to migrate student profile for user_id: {}", user.getId(), e);
            }
        }
        
        return count;
    }

    private int migrateTeachers() {
        List<Teacher> teachers = teacherRepository.findAll();
        int count = 0;
        
        for (Teacher teacher : teachers) {
            if (teacher.getUserId() == null) {
                continue;
            }
            
            User user = userRepository.findById(teacher.getUserId()).orElse(null);
            if (user == null) {
                continue;
            }
            
            if (userProfileRepository.findByUserId(user.getId()).isPresent()) {
                continue;
            }
            
            UserProfile profile = UserProfile.builder()
                    .user(user)
                    .firstName(teacher.getFirstName())
                    .lastName(teacher.getLastName())
                    .curp(teacher.getCurp())
                    .rfc(teacher.getRfc())
                    .phone(teacher.getPhone())
                    .institutionalEmail(teacher.getInstitutionalEmail())
                    .employeeNumber(teacher.getEmployeeNumber())
                    .build();
            
            try {
                userProfileRepository.save(profile);
                count++;
                log.info("Migrated teacher profile for user_id: {}", user.getId());
            } catch (Exception e) {
                log.error("Failed to migrate teacher profile for user_id: {}", user.getId(), e);
            }
        }
        
        return count;
    }
}
