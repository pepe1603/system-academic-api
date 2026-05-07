package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface CourseRepository extends JpaRepository<Course, UUID> {

    boolean existsByCourseCodeAndIsDeletedFalse(String courseCode);

    boolean existsByCourseCodeAndIsDeletedFalseAndIdNot(String courseCode, UUID id);

    Optional<Course> findByCourseCodeAndIsDeletedFalse(String courseCode);

    Page<Course> findAllByIsDeletedFalse(Pageable pageable);

    Page<Course> findAllByIsDeletedTrue(Pageable pageable);
}
