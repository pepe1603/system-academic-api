package com.academic_system.service;

import com.academic_system.dto.cpanel.AccessAuditDTO;
import com.academic_system.entity.postgres.AccessAudit;
import com.academic_system.repository.postgres.AccessAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessAuditService {

    private final AccessAuditRepository accessAuditRepository;

    @Transactional(readOnly = true)
    public Page<AccessAuditDTO> getAllAuditLogs(Pageable pageable) {
        return accessAuditRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<AccessAuditDTO> getAuditLogById(String id) {
        return accessAuditRepository.findById(UUID.fromString(id))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AccessAuditDTO> getAuditLogsByUserId(String userId, Pageable pageable) {
        return accessAuditRepository.findByUserIdOrderByCreatedAtDesc(UUID.fromString(userId), pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AccessAuditDTO> getAuditLogsByModule(String module, Pageable pageable) {
        return accessAuditRepository.findByModuleOrderByCreatedAtDesc(module, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AccessAuditDTO> getAuditLogsByAction(String action, Pageable pageable) {
        return accessAuditRepository.findByActionOrderByCreatedAtDesc(action, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AccessAuditDTO> getAuditLogsBySuccess(Boolean success, Pageable pageable) {
        return accessAuditRepository.findBySuccessOrderByCreatedAtDesc(success, pageable)
                .map(this::toDTO);
    }

    @Transactional
    public AccessAuditDTO createAuditLog(AccessAuditDTO dto) {
        AccessAudit audit = AccessAudit.builder()
                .action(dto.getAction())
                .module(dto.getModule())
                .ipAddress(dto.getIpAddress())
                .success(dto.getSuccess())
                .metadata(dto.getMetadata())
                .build();

        if (dto.getUserId() != null) {
            audit.setUser(com.academic_system.entity.postgres.User.builder()
                    .id(dto.getUserId())
                    .build());
        }

        audit = accessAuditRepository.save(audit);
        log.info("Audit log created: {} {} {}", audit.getAction(), audit.getModule(), audit.getId());
        return toDTO(audit);
    }

    @Transactional
    public void deleteAuditLog(String id) {
        AccessAudit audit = accessAuditRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Registro de auditoría no encontrado"));
        accessAuditRepository.delete(audit);
        log.info("Deleted audit log: {}", id);
    }

    private AccessAuditDTO toDTO(AccessAudit audit) {
        return AccessAuditDTO.builder()
                .id(audit.getId())
                .userId(audit.getUser() != null ? audit.getUser().getId() : null)
                .userEmail(audit.getUser() != null ? audit.getUser().getEmail() : null)
                .action(audit.getAction())
                .module(audit.getModule())
                .ipAddress(audit.getIpAddress())
                .success(audit.getSuccess())
                .metadata(audit.getMetadata())
                .createdAt(audit.getCreatedAt())
                .build();
    }
}
