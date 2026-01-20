package com.example.firesystem.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.firesystem.dto.UploadResponseDto;
import com.example.firesystem.service.UploadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "File Upload", description = "Methods for uploading .csv files to import data into the system")
public class UploadController {
    private final UploadService uploadService;

    @Operation(summary = "Upload new Sensors", description = "Receives file with new Sensors and adds them to database")
    @PreAuthorize("hasAuthority('SENSOR:CREATE')")
    @PostMapping(value = "/sensors", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponseDto> uploadSensors(@RequestParam MultipartFile file) {
        UploadResponseDto response = uploadService.importSensors(file);
        if (response.failureCount() > 0) {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Upload new Alerts", description = "Receives file with new Alerts and adds them to database")
    @PreAuthorize("hasAuthority('ALERT:CREATE')")
    @PostMapping(value = "/alerts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponseDto> uploadAlerts(@RequestParam MultipartFile file) {
        UploadResponseDto response = uploadService.importAlerts(file);
        if (response.failureCount() > 0) {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
        }
        return ResponseEntity.ok(response);
    }
}