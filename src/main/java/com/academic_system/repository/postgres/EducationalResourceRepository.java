package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.EducationalResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface EducationalResourceRepository extends JpaRepository<EducationalResource, UUID> {

    Page<EducationalResource> findAllByIsDeletedFalse(Pageable pageable);

    Page<EducationalResource> findAllByIsDeletedTrue(Pageable pageable);

    List<EducationalResource> findByCourseIdAndIsDeletedFalse(UUID courseId);
}
