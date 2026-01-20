package com.example.firesystem.dto;

import java.util.List;

public record UploadResponseDto(
        int totalRows,
        int successCount,
        int failureCount,
        List<String> errorList) {

}