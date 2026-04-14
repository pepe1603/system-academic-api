package com.academic_system.repository.mysql;

import com.academic_system.entity.mysql.PortalContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortalContactRepository extends JpaRepository<PortalContact, String> {
    List<PortalContact> findByIsReadFalseOrderByCreatedAtDesc();
    List<PortalContact> findAllByOrderByCreatedAtDesc();
}
