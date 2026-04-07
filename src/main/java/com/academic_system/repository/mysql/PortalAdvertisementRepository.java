package com.academic_system.repository.mysql;

import com.academic_system.entity.mysql.PortalAdvertisement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortalAdvertisementRepository extends JpaRepository<PortalAdvertisement, String> {
    List<PortalAdvertisement> findByIsPublishedTrueAndIsDeletedFalseOrderByDisplayOrderAsc();
    List<PortalAdvertisement> findByPositionAndIsPublishedTrueAndIsDeletedFalseOrderByDisplayOrderAsc(String position);
}
