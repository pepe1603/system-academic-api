package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.StudentDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface StudentDocumentRepository extends JpaRepository<StudentDocument, UUID> {

    Page<StudentDocument> findAllByIsDeletedFalse(Pageable pageable);

    Page<StudentDocument> findAllByIsDeletedTrue(Pageable pageable);

    List<StudentDocument> findByStudentIdAndIsDeletedFalse(UUID studentId);

    List<StudentDocument> findByStudentIdAndDocumentTypeAndIsDeletedFalse(UUID studentId, String documentType);
}
