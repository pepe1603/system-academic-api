package com.academic_system.repository.mysql;

import com.academic_system.entity.mysql.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, String> {
    Optional<Institution> findByIsActiveTrue();
}
