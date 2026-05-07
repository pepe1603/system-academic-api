package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.AcademicSemester;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface AcademicSemesterRepository extends JpaRepository<AcademicSemester, UUID> {

    boolean existsByNameAndIsDeletedFalse(String name);

    boolean existsByNameAndIsDeletedFalseAndIdNot(String name, UUID id);

    Optional<AcademicSemester> findByNameAndYearAndPeriod(String name, Integer year, Integer period);

    Page<AcademicSemester> findAllByIsDeletedFalse(Pageable pageable);

    Page<AcademicSemester> findAllByIsDeletedTrue(Pageable pageable);
}
