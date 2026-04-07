package com.academic_system.repository.mysql;

import com.academic_system.entity.mysql.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    List<Event> findByIsPublishedTrueAndIsDeletedFalseOrderByEventDateDesc();
}
