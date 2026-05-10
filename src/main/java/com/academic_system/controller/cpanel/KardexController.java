package com.academic_system.controller.cpanel;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.cpanel.CreateKardexRequest;
import com.academic_system.dto.cpanel.KardexDTO;
import com.academic_system.dto.cpanel.UpdateKardexRequest;
import com.academic_system.service.KardexService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/kardex")
@RequiredArgsConstructor
public class KardexController {

    private final KardexService kardexService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<KardexDTO>>> getAllKardexRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = kardexService.getAllKardexRecords(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KardexDTO>> getKardex(@PathVariable String id) {
        Optional<KardexDTO> kardex = kardexService.getKardexById(id);
        return kardex.map(k -> ResponseEntity.ok(ApiResponse.success(k)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Registro kardex no encontrado")));
    }

    @GetMapping("/by-student/{studentId}")
    public ResponseEntity<ApiResponse<List<KardexDTO>>> getKardexByStudent(
            @PathVariable String studentId) {
        var result = kardexService.getKardexByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<KardexDTO>> createKardex(
            @Valid @RequestBody CreateKardexRequest request) {
        KardexDTO kardex = kardexService.createKardex(request);
        return ResponseEntity.ok(ApiResponse.success("Registro kardex creado", kardex));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<KardexDTO>> updateKardex(
            @PathVariable String id,
            @Valid @RequestBody UpdateKardexRequest request) {
        KardexDTO kardex = kardexService.updateKardex(id, request);
        return ResponseEntity.ok(ApiResponse.success("Registro kardex actualizado", kardex));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteKardex(@PathVariable String id) {
        kardexService.deleteKardex(id);
        return ResponseEntity.ok(ApiResponse.success("Registro kardex eliminado", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<KardexDTO>>> getDeletedKardexRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = kardexService.getDeletedKardexRecords(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
