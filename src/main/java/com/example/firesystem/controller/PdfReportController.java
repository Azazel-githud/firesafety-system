package com.example.firesystem.controller;

import com.example.firesystem.service.PdfReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "API для генерации отчетов")
public class PdfReportController {

    private final PdfReportService pdfReportService;

    @Operation(summary = "Сгенерировать PDF отчет (латиница)")
    @GetMapping("/alert/{alertId}/pdf")
    public ResponseEntity<byte[]> generateAlertPdfReport(@PathVariable Long alertId) {
        byte[] pdfBytes = pdfReportService.generateAlertReport(alertId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename",
                "alert_report_" + alertId + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @Operation(summary = "Простой тестовый PDF")
    @GetMapping("/alert/{alertId}/simple")
    public ResponseEntity<byte[]> generateSimplePdf(@PathVariable Long alertId) {
        byte[] pdfBytes = pdfReportService.generateSimplePdf(alertId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename",
                "simple_report_" + alertId + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @Operation(summary = "Сохранить PDF отчет")
    @PostMapping("/alert/{alertId}/save")
    public ResponseEntity<String> savePdfReport(@PathVariable Long alertId) {
        byte[] pdfBytes = pdfReportService.generateAndSaveReport(alertId);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body("PDF report for Alert #" + alertId + " saved. " +
                        "Size: " + pdfBytes.length + " bytes");
    }

    @Operation(summary = "Список сохраненных отчетов")
    @GetMapping("/saved")
    public ResponseEntity<List<String>> listSavedReports() {
        List<String> reports = pdfReportService.listSavedReports();
        return ResponseEntity.ok(reports);
    }
}