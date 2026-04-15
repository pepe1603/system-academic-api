package com.academic_system.controller.portal;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.portal.*;
import com.academic_system.service.portal.PortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class PortalController {

    private final PortalService portalService;

    // ==================== INSTITUTION ====================

    @GetMapping("/institution")
    public ResponseEntity<ApiResponse<InstitutionDTO>> getInstitution() {
        InstitutionDTO institution = portalService.getInstitution();
        if (institution == null) {
            return ResponseEntity.ok(ApiResponse.success("No hay información institucional", null));
        }
        return ResponseEntity.ok(ApiResponse.success(institution));
    }

    @PutMapping("/institution")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InstitutionDTO>> updateInstitution(@RequestBody InstitutionDTO dto) {
        InstitutionDTO updated = portalService.updateInstitution(dto);
        return ResponseEntity.ok(ApiResponse.success("Institución actualizada", updated));
    }

    // ==================== NEWS ====================

    @GetMapping("/news")
    public ResponseEntity<ApiResponse<List<NewsDTO>>> getNews() {
        List<NewsDTO> news = portalService.getPublishedNews();
        return ResponseEntity.ok(ApiResponse.success(news));
    }

    @GetMapping("/news/paged")
    public ResponseEntity<ApiResponse<Page<NewsDTO>>> getNewsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<NewsDTO> news = portalService.getPublishedNewsPaged(page, size);
        return ResponseEntity.ok(ApiResponse.success(news));
    }

    @GetMapping("/news/{id}")
    public ResponseEntity<ApiResponse<NewsDTO>> getNewsById(@PathVariable String id) {
        NewsDTO news = portalService.getNewsById(id);
        if (news == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(news));
    }

    @PostMapping("/news")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NewsDTO>> createNews(@RequestBody NewsDTO dto) {
        NewsDTO created = portalService.createNews(dto);
        return ResponseEntity.ok(ApiResponse.success("Noticia creada", created));
    }

    @PutMapping("/news/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NewsDTO>> updateNews(@PathVariable String id, @RequestBody NewsDTO dto) {
        NewsDTO updated = portalService.updateNews(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Noticia actualizada", updated));
    }

    @DeleteMapping("/news/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteNews(@PathVariable String id) {
        portalService.deleteNews(id);
        return ResponseEntity.ok(ApiResponse.success("Noticia eliminada", null));
    }

    // ==================== EVENTS ====================

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<EventDTO>>> getEvents() {
        List<EventDTO> events = portalService.getPublishedEvents();
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/events/paged")
    public ResponseEntity<ApiResponse<Page<EventDTO>>> getEventsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<EventDTO> events = portalService.getPublishedEventsPaged(page, size);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<ApiResponse<EventDTO>> getEventById(@PathVariable String id) {
        EventDTO event = portalService.getEventById(id);
        if (event == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    @PostMapping("/events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventDTO>> createEvent(@RequestBody EventDTO dto) {
        EventDTO created = portalService.createEvent(dto);
        return ResponseEntity.ok(ApiResponse.success("Evento creado", created));
    }

    @PutMapping("/events/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventDTO>> updateEvent(@PathVariable String id, @RequestBody EventDTO dto) {
        EventDTO updated = portalService.updateEvent(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Evento actualizado", updated));
    }

    @DeleteMapping("/events/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable String id) {
        portalService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Evento eliminado", null));
    }

    // ==================== ADVERTISEMENTS ====================

    @GetMapping("/ads")
    public ResponseEntity<ApiResponse<List<AdvertisementDTO>>> getAdvertisements() {
        List<AdvertisementDTO> ads = portalService.getPublishedAds();
        return ResponseEntity.ok(ApiResponse.success(ads));
    }

    @GetMapping("/ads/{position}")
    public ResponseEntity<ApiResponse<List<AdvertisementDTO>>> getAdvertisementsByPosition(@PathVariable String position) {
        List<AdvertisementDTO> ads = portalService.getPublishedAdsByPosition(position);
        return ResponseEntity.ok(ApiResponse.success(ads));
    }

    @PostMapping("/ads")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdvertisementDTO>> createAdvertisement(@RequestBody AdvertisementDTO dto) {
        AdvertisementDTO created = portalService.createAdvertisement(dto);
        return ResponseEntity.ok(ApiResponse.success("Anuncio creado", created));
    }

    @PutMapping("/ads/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdvertisementDTO>> updateAdvertisement(@PathVariable String id, @RequestBody AdvertisementDTO dto) {
        AdvertisementDTO updated = portalService.updateAdvertisement(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Anuncio actualizado", updated));
    }

    @DeleteMapping("/ads/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAdvertisement(@PathVariable String id) {
        portalService.deleteAdvertisement(id);
        return ResponseEntity.ok(ApiResponse.success("Anuncio eliminado", null));
    }

    // ==================== CONTACT ====================

    @PostMapping("/contact")
    public ResponseEntity<ApiResponse<ContactDTO>> submitContact(@RequestBody ContactDTO dto) {
        ContactDTO created = portalService.submitContact(dto);
        return ResponseEntity.ok(ApiResponse.success("Mensaje enviado correctamente", created));
    }

    @GetMapping("/contact")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ContactDTO>>> getAllMessages() {
        List<ContactDTO> messages = portalService.getAllMessages();
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @GetMapping("/contact/unread")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ContactDTO>>> getUnreadMessages() {
        List<ContactDTO> messages = portalService.getUnreadMessages();
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PutMapping("/contact/{id}/read")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ContactDTO>> markAsRead(@PathVariable String id) {
        ContactDTO updated = portalService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Mensaje marcado como leído", updated));
    }

    @PostMapping("/contact/{id}/respond")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ContactDTO>> respondToContact(@PathVariable String id, @RequestBody String response) {
        ContactDTO updated = portalService.respondToContact(id, response);
        return ResponseEntity.ok(ApiResponse.success("Mensaje respondido", updated));
    }
}