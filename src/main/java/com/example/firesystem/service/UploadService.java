package com.example.firesystem.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.firesystem.dto.AlertRequestDto;
import com.example.firesystem.dto.SensorRequestDto;
import com.example.firesystem.dto.UploadResponseDto;
import com.example.firesystem.enums.EventType;
import com.example.firesystem.enums.StatusType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadService {
    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

    private final SensorService sensorService;
    private final AlertService alertService;

    @Value("${spring.servlet.multipart.location}")
    private String uploadLocation;

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("The system supports only CSV files");
        }
        logger.debug("File validation passed: {}", filename);
    }

    private Path saveFile(MultipartFile file) throws IOException {
        String timestamp = LocalDateTime.now().toString().replaceAll(":", "-");
        String filename = timestamp + "_" + file.getOriginalFilename();
        Path targetLocation = Paths.get(uploadLocation).toAbsolutePath().normalize().resolve(filename);

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        logger.info("File saved to: {}", targetLocation);
        return targetLocation;
    }

    public UploadResponseDto importSensors(MultipartFile file) {
        int successCount = 0;
        int failureCount = 0;
        List<String> errorList = new ArrayList<>();

        try {
            validateFile(file);
        } catch (IllegalArgumentException e) {
            errorList.add(file.getOriginalFilename() + " : " + e.getMessage());
            throw new IllegalArgumentException("File validation failed");
        }

        try {
            Path savedFile = saveFile(file);
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build();

            try (BufferedReader reader = Files.newBufferedReader(savedFile, StandardCharsets.UTF_8);
                    CSVParser csvParser = format.parse(reader)) {
                int rowNumber = 1;
                for (CSVRecord record : csvParser) {
                    try {
                        SensorRequestDto request = new SensorRequestDto(
                                record.get("model"),
                                record.get("location"),
                                record.isSet("userId") && !record.get("userId").isEmpty()
                                        ? Long.parseLong(record.get("userId"))
                                        : null);
                        sensorService.createSensor(request);
                        successCount++;
                    } catch (Exception e) {
                        failureCount++;
                        errorList.add(file.getOriginalFilename() + " [row " + rowNumber + "] : " + e.getMessage());
                        logger.warn("Failed to import row {}: {}", rowNumber, e);
                    }
                    rowNumber++;
                }
            }

            logger.info("Sensor import completed. Success: {}, Failures: {}", successCount, failureCount);
            return new UploadResponseDto(
                    successCount + failureCount,
                    successCount,
                    failureCount,
                    errorList);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process CSV file: " + e.getMessage());
        }
    }

    public UploadResponseDto importAlerts(MultipartFile file) {
        int successCount = 0;
        int failureCount = 0;
        List<String> errorList = new ArrayList<>();

        try {
            validateFile(file);
        } catch (IllegalArgumentException e) {
            errorList.add(file.getOriginalFilename() + " : " + e.getMessage());
            throw new IllegalArgumentException("File validation failed");
        }

        try {
            Path savedFile = saveFile(file);
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build();

            try (BufferedReader reader = Files.newBufferedReader(savedFile, StandardCharsets.UTF_8);
                    CSVParser csvParser = format.parse(reader)) {
                int rowNumber = 1;
                for (CSVRecord record : csvParser) {
                    try {
                        // Внимание: sensorId - обязательное поле для Alert
                        Long sensorId = Long.parseLong(record.get("sensorId"));

                        AlertRequestDto request = new AlertRequestDto(
                                sensorId,
                                EventType.valueOf(record.get("type").toUpperCase()),
                                record.get("description"),
                                record.isSet("status") && !record.get("status").isEmpty()
                                        ? StatusType.valueOf(record.get("status").toUpperCase())
                                        : StatusType.new_status,
                                record.isSet("photoUrl")
                                        ? List.of(record.get("photoUrl").split(";"))
                                        : new ArrayList<>(),
                                record.isSet("userId") && !record.get("userId").isEmpty()
                                        ? Long.parseLong(record.get("userId"))
                                        : null);
                        alertService.createAlert(request);
                        successCount++;
                    } catch (Exception e) {
                        failureCount++;
                        errorList.add(file.getOriginalFilename() + " [row " + rowNumber + "] : " + e.getMessage());
                        logger.warn("Failed to import row {}: {}", rowNumber, e);
                    }
                    rowNumber++;
                }
            }

            logger.info("Alert import completed. Success: {}, Failures: {}", successCount, failureCount);
            return new UploadResponseDto(
                    successCount + failureCount,
                    successCount,
                    failureCount,
                    errorList);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process CSV file: " + e.getMessage());
        }
    }
}