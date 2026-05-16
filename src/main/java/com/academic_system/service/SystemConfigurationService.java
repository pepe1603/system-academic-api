package com.academic_system.service;

import com.academic_system.dto.cpanel.*;
import com.academic_system.entity.postgres.SystemConfiguration;
import com.academic_system.repository.postgres.SystemConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigurationService {

    private final SystemConfigurationRepository systemConfigurationRepository;

    private static final List<String> VALID_DATA_TYPES = List.of("STRING", "NUMBER", "BOOLEAN", "JSON");

    @Transactional(readOnly = true)
    public Page<SystemConfigurationDTO> getAllConfigurations(Pageable pageable) {
        return systemConfigurationRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<SystemConfigurationDTO> getConfigurationById(String id) {
        return systemConfigurationRepository.findById(UUID.fromString(id))
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<SystemConfigurationDTO> getConfigurationByKey(String key) {
        return systemConfigurationRepository.findByConfigKeyAndIsDeletedFalse(key)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<SystemConfigurationDTO> getDeletedConfigurations(Pageable pageable) {
        return systemConfigurationRepository.findAllByIsDeletedTrue(pageable)
                .map(this::toDTO)
                .getContent();
    }

    @Transactional
    public SystemConfigurationDTO createConfiguration(CreateSystemConfigurationRequest request) {
        String key = request.getConfigKey().toUpperCase().replace(" ", "_");
        if (systemConfigurationRepository.existsByConfigKeyAndIsDeletedFalse(key)) {
            throw new IllegalArgumentException("La clave de configuración ya existe: " + key);
        }

        String dataType = request.getDataType() != null ? request.getDataType().toUpperCase() : "STRING";
        if (!VALID_DATA_TYPES.contains(dataType)) {
            throw new IllegalArgumentException("Tipo de dato inválido. Valores: STRING, NUMBER, BOOLEAN, JSON");
        }

        SystemConfiguration config = SystemConfiguration.builder()
                .configKey(key)
                .configValue(request.getConfigValue())
                .description(request.getDescription())
                .dataType(dataType)
                .module(request.getModule())
                .build();

        config = systemConfigurationRepository.save(config);
        log.info("Created configuration: {} = {}", key, config.getId());
        return toDTO(config);
    }

    @Transactional
    public SystemConfigurationDTO updateConfiguration(String id, UpdateSystemConfigurationRequest request) {
        SystemConfiguration config = systemConfigurationRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Configuración no encontrada"));

        if (request.getConfigValue() != null) config.setConfigValue(request.getConfigValue());
        if (request.getDescription() != null) config.setDescription(request.getDescription());
        if (request.getDataType() != null) {
            String newType = request.getDataType().toUpperCase();
            if (!VALID_DATA_TYPES.contains(newType)) throw new IllegalArgumentException("Tipo de dato inválido");
            config.setDataType(newType);
        }
        if (request.getModule() != null) config.setModule(request.getModule());
        if (request.getIsActive() != null) config.setIsActive(request.getIsActive());

        config = systemConfigurationRepository.save(config);
        log.info("Updated configuration: {}", config.getId());
        return toDTO(config);
    }

    @Transactional
    public void deleteConfiguration(String id) {
        SystemConfiguration config = systemConfigurationRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Configuración no encontrada"));
        config.setIsDeleted(true);
        systemConfigurationRepository.save(config);
        log.info("Deleted configuration: {}", id);
    }

    private SystemConfigurationDTO toDTO(SystemConfiguration config) {
        return SystemConfigurationDTO.builder()
                .id(config.getId())
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .description(config.getDescription())
                .dataType(config.getDataType())
                .module(config.getModule())
                .isActive(config.getIsActive())
                .isDeleted(config.getIsDeleted())
                .build();
    }
}
