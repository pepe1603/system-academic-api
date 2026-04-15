package com.academic_system.service.portal;

import com.academic_system.dto.portal.*;
import com.academic_system.entity.mysql.*;
import com.academic_system.repository.mysql.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortalService {

    private final InstitutionRepository institutionRepository;
    private final NewsRepository newsRepository;
    private final EventRepository eventRepository;
    private final PortalAdvertisementRepository advertisementRepository;
    private final PortalContactRepository contactRepository;

    // ==================== INSTITUTION ====================

    public InstitutionDTO getInstitution() {
        return institutionRepository.findByIsActiveTrue()
                .map(this::toInstitutionDTO)
                .orElse(null);
    }

    @Transactional("mysqlTransactionManager")
    public InstitutionDTO updateInstitution(InstitutionDTO dto) {
        Institution institution = institutionRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Institución no encontrada"));

        if (dto.getName() != null) institution.setName(dto.getName());
        if (dto.getAddress() != null) institution.setAddress(dto.getAddress());
        if (dto.getPhone() != null) institution.setPhone(dto.getPhone());
        if (dto.getEmail() != null) institution.setEmail(dto.getEmail());
        if (dto.getWebsite() != null) institution.setWebsite(dto.getWebsite());
        if (dto.getMission() != null) institution.setMission(dto.getMission());
        if (dto.getVision() != null) institution.setVision(dto.getVision());
        if (dto.getHistory() != null) institution.setHistory(dto.getHistory());
        if (dto.getValues() != null) institution.setValues(dto.getValues());
        if (dto.getLogoUrl() != null) institution.setLogoUrl(dto.getLogoUrl());
        
        institution = institutionRepository.save(institution);
        return toInstitutionDTO(institution);
    }

    // ==================== NEWS ====================

    public List<NewsDTO> getPublishedNews() {
        return newsRepository.findByIsPublishedTrueAndIsDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::toNewsDTO)
                .collect(Collectors.toList());
    }

    public Page<NewsDTO> getPublishedNewsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return newsRepository.findByIsPublishedTrueAndIsDeletedFalse(pageable)
                .map(this::toNewsDTO);
    }

    public NewsDTO getNewsById(String id) {
        return newsRepository.findById(id)
                .map(this::toNewsDTO)
                .orElse(null);
    }

    @Transactional("mysqlTransactionManager")
    public NewsDTO createNews(NewsDTO dto) {
        News news = News.builder()
                .id(UUID.randomUUID().toString())
                .title(dto.getTitle())
                .content(dto.getContent())
                .imageUrl(dto.getImageUrl())
                .isPublished(dto.getIsPublished() != null ? dto.getIsPublished() : true)
                .isDeleted(false)
                .build();
        
        news = newsRepository.save(news);
        return toNewsDTO(news);
    }

    @Transactional("mysqlTransactionManager")
    public NewsDTO updateNews(String id, NewsDTO dto) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Noticia no encontrada"));

        if (dto.getTitle() != null) news.setTitle(dto.getTitle());
        if (dto.getContent() != null) news.setContent(dto.getContent());
        if (dto.getImageUrl() != null) news.setImageUrl(dto.getImageUrl());
        if (dto.getIsPublished() != null) news.setIsPublished(dto.getIsPublished());
        
        news = newsRepository.save(news);
        return toNewsDTO(news);
    }

    @Transactional("mysqlTransactionManager")
    public void deleteNews(String id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Noticia no encontrada"));
        news.setIsDeleted(true);
        newsRepository.save(news);
    }

    // ==================== EVENTS ====================

    public List<EventDTO> getPublishedEvents() {
        return eventRepository.findByIsPublishedTrueAndIsDeletedFalseOrderByEventDateDesc()
                .stream()
                .map(this::toEventDTO)
                .collect(Collectors.toList());
    }

    public Page<EventDTO> getPublishedEventsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate").descending());
        return eventRepository.findByIsPublishedTrueAndIsDeletedFalse(pageable)
                .map(this::toEventDTO);
    }

    public EventDTO getEventById(String id) {
        return eventRepository.findById(id)
                .map(this::toEventDTO)
                .orElse(null);
    }

    @Transactional("mysqlTransactionManager")
    public EventDTO createEvent(EventDTO dto) {
        Event event = Event.builder()
                .id(UUID.randomUUID().toString())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .location(dto.getLocation())
                .isPublished(dto.getIsPublished() != null ? dto.getIsPublished() : true)
                .isDeleted(false)
                .build();
        
        event = eventRepository.save(event);
        return toEventDTO(event);
    }

    @Transactional("mysqlTransactionManager")
    public EventDTO updateEvent(String id, EventDTO dto) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getEventDate() != null) event.setEventDate(dto.getEventDate());
        if (dto.getLocation() != null) event.setLocation(dto.getLocation());
        if (dto.getIsPublished() != null) event.setIsPublished(dto.getIsPublished());
        
        event = eventRepository.save(event);
        return toEventDTO(event);
    }

    @Transactional("mysqlTransactionManager")
    public void deleteEvent(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        event.setIsDeleted(true);
        eventRepository.save(event);
    }

    // ==================== ADVERTISEMENTS ====================

    public List<AdvertisementDTO> getPublishedAds() {
        return advertisementRepository.findByIsPublishedTrueAndIsDeletedFalseOrderByDisplayOrderAsc()
                .stream()
                .map(this::toAdvertisementDTO)
                .collect(Collectors.toList());
    }

    public List<AdvertisementDTO> getPublishedAdsByPosition(String position) {
        return advertisementRepository.findByPositionAndIsPublishedTrueAndIsDeletedFalseOrderByDisplayOrderAsc(position)
                .stream()
                .map(this::toAdvertisementDTO)
                .collect(Collectors.toList());
    }

    @Transactional("mysqlTransactionManager")
    public AdvertisementDTO createAdvertisement(AdvertisementDTO dto) {
        PortalAdvertisement ad = PortalAdvertisement.builder()
                .id(UUID.randomUUID().toString())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .linkUrl(dto.getLinkUrl())
                .position(dto.getPosition() != null ? dto.getPosition() : "BANNER")
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0)
                .isPublished(dto.getIsPublished() != null ? dto.getIsPublished() : true)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .isDeleted(false)
                .build();
        
        ad = advertisementRepository.save(ad);
        return toAdvertisementDTO(ad);
    }

    @Transactional("mysqlTransactionManager")
    public AdvertisementDTO updateAdvertisement(String id, AdvertisementDTO dto) {
        PortalAdvertisement ad = advertisementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Anuncio no encontrado"));

        if (dto.getTitle() != null) ad.setTitle(dto.getTitle());
        if (dto.getDescription() != null) ad.setDescription(dto.getDescription());
        if (dto.getImageUrl() != null) ad.setImageUrl(dto.getImageUrl());
        if (dto.getLinkUrl() != null) ad.setLinkUrl(dto.getLinkUrl());
        if (dto.getPosition() != null) ad.setPosition(dto.getPosition());
        if (dto.getDisplayOrder() != null) ad.setDisplayOrder(dto.getDisplayOrder());
        if (dto.getIsPublished() != null) ad.setIsPublished(dto.getIsPublished());
        if (dto.getStartDate() != null) ad.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) ad.setEndDate(dto.getEndDate());
        
        ad = advertisementRepository.save(ad);
        return toAdvertisementDTO(ad);
    }

    @Transactional("mysqlTransactionManager")
    public void deleteAdvertisement(String id) {
        PortalAdvertisement ad = advertisementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Anuncio no encontrado"));
        ad.setIsDeleted(true);
        advertisementRepository.save(ad);
    }

    // ==================== CONTACT ====================

    @Transactional("mysqlTransactionManager")
    public ContactDTO submitContact(ContactDTO dto) {
        PortalContact contact = PortalContact.builder()
                .id(UUID.randomUUID().toString())
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .subject(dto.getSubject())
                .message(dto.getMessage())
                .isRead(false)
                .isResponded(false)
                .build();
        
        contact = contactRepository.save(contact);
        return toContactDTO(contact);
    }

    public List<ContactDTO> getUnreadMessages() {
        return contactRepository.findByIsReadFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::toContactDTO)
                .collect(Collectors.toList());
    }

    public List<ContactDTO> getAllMessages() {
        return contactRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toContactDTO)
                .collect(Collectors.toList());
    }

    @Transactional("mysqlTransactionManager")
    public ContactDTO markAsRead(String id) {
        PortalContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mensaje no encontrado"));
        contact.setIsRead(true);
        contact = contactRepository.save(contact);
        return toContactDTO(contact);
    }

    @Transactional("mysqlTransactionManager")
    public ContactDTO respondToContact(String id, String response) {
        PortalContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mensaje no encontrado"));
        
        contact.setResponse(response);
        contact.setIsResponded(true);
        contact.setIsRead(true);
        contact.setResponseDate(LocalDateTime.now());
        
        contact = contactRepository.save(contact);
        return toContactDTO(contact);
    }

    // ==================== MAPPERS ====================

    private InstitutionDTO toInstitutionDTO(Institution entity) {
        return InstitutionDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(entity.getAddress())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .website(entity.getWebsite())
                .mission(entity.getMission())
                .vision(entity.getVision())
                .history(entity.getHistory())
                .values(entity.getValues())
                .logoUrl(entity.getLogoUrl())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private NewsDTO toNewsDTO(News entity) {
        return NewsDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .imageUrl(entity.getImageUrl())
                .isPublished(entity.getIsPublished())
                .isDeleted(entity.getIsDeleted())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private EventDTO toEventDTO(Event entity) {
        return EventDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .eventDate(entity.getEventDate())
                .location(entity.getLocation())
                .isPublished(entity.getIsPublished())
                .isDeleted(entity.getIsDeleted())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private AdvertisementDTO toAdvertisementDTO(PortalAdvertisement entity) {
        return AdvertisementDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .linkUrl(entity.getLinkUrl())
                .position(entity.getPosition())
                .displayOrder(entity.getDisplayOrder())
                .isPublished(entity.getIsPublished())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .isDeleted(entity.getIsDeleted())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private ContactDTO toContactDTO(PortalContact entity) {
        return ContactDTO.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .subject(entity.getSubject())
                .message(entity.getMessage())
                .isRead(entity.getIsRead())
                .isResponded(entity.getIsResponded())
                .response(entity.getResponse())
                .responseDate(entity.getResponseDate())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}