package com.example.firesystem.service;

import com.example.firesystem.model.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfReportService {

    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Value("${reports.save.path:./reports/}")
    private String reportsSavePath;

    @Value("${reports.auto-save:false}")
    private boolean autoSave;

    public byte[] generateAlertReport(Long alertId) {
        log.info("Генерация PDF отчета для Alert #{}", alertId);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Используем стандартный шрифт без кириллицы
                writeReportContent(contentStream, page, alertId);
            }

            // Сохраняем в байтовый массив
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            byte[] pdfBytes = baos.toByteArray();

            log.info("PDF отчет для Alert #{} сгенерирован ({} байт)", alertId, pdfBytes.length);

            // Автоматическое сохранение если включено
            if (autoSave) {
                saveToFile(pdfBytes, alertId);
            }

            return pdfBytes;

        } catch (IOException e) {
            log.error("Ошибка при генерации PDF отчета для Alert #{}", alertId, e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private void writeReportContent(PDPageContentStream contentStream, PDPage page, Long alertId)
            throws IOException {
        float margin = 50;
        float yPosition = page.getMediaBox().getHeight() - margin;
        float lineHeight = 15;

        // Заголовок
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("ALERT REPORT #" + alertId);
        contentStream.endText();

        yPosition -= 40;

        // Подзаголовок
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("INCIDENT DETAILS:");
        contentStream.endText();

        yPosition -= lineHeight * 1.5f;

        // Данные
        Alert alert = getMockAlert(alertId);
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);

        String[] details = {
                "Alert ID: #" + alert.getId(),
                "Type: " + translateEventTypeToLatin(alert.getType()),
                "Status: " + translateStatusToLatin(alert.getStatus()),
                "Location: " + (alert.getSensor() != null ? "Server Room #3, 2nd floor" : "Not specified"),
                "Date and Time: " + alert.getTimestamp().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "Description: Fire detected in server room",
                "Assigned to: Ivanov I.I."
        };

        for (int i = 0; i < details.length; i++) {
            if (i > 0) {
                contentStream.newLineAtOffset(0, -lineHeight);
            }
            contentStream.showText(details[i]);
        }
        contentStream.endText();

        // Подпись
        yPosition -= lineHeight * 10;
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Responsible: ___________________  Ivanov I.I.");
        contentStream.endText();

        // Дата
        yPosition -= lineHeight * 2;
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Close date: " + alert.getTimestamp().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        contentStream.endText();
    }

    private void saveToFile(byte[] pdfBytes, Long alertId) {
        try {
            // Создаем папку
            Path reportsDir = Paths.get(reportsSavePath);
            if (!Files.exists(reportsDir)) {
                Files.createDirectories(reportsDir);
                log.info("Создана директория для отчетов: {}", reportsDir.toAbsolutePath());
            }

            // Генерируем имя файла
            String fileName = String.format("alert_report_%d_%s.pdf",
                    alertId,
                    LocalDateTime.now().format(FILE_DATE_FORMATTER));

            Path filePath = reportsDir.resolve(fileName);

            // Сохраняем файл
            Files.write(filePath, pdfBytes);

            log.info("PDF отчет сохранен: {}", filePath.toAbsolutePath());

        } catch (IOException e) {
            log.error("Ошибка сохранения PDF файла", e);
        }
    }

    /**
     * Метод с гарантированным сохранением файла
     */
    public byte[] generateAndSaveReport(Long alertId) {
        byte[] pdfBytes = generateAlertReport(alertId);

        // Сохраняем даже если autoSave=false
        saveToFile(pdfBytes, alertId);

        return pdfBytes;
    }

    /**
     * Получение списка сохраненных отчетов
     */
    public java.util.List<String> listSavedReports() {
        try {
            Path reportsDir = Paths.get(reportsSavePath);
            if (!Files.exists(reportsDir)) {
                return java.util.List.of("Reports directory does not exist");
            }

            return Files.list(reportsDir)
                    .filter(path -> path.toString().endsWith(".pdf"))
                    .map(path -> path.getFileName().toString())
                    .toList();

        } catch (IOException e) {
            log.error("Ошибка чтения списка отчетов", e);
            return java.util.List.of("Error: " + e.getMessage());
        }
    }

    /**
     * Получение сохраненного отчета
     */
    public byte[] getSavedReport(String filename) {
        try {
            Path filePath = Paths.get(reportsSavePath, filename);
            if (!Files.exists(filePath)) {
                throw new RuntimeException("File not found: " + filename);
            }

            return Files.readAllBytes(filePath);

        } catch (IOException e) {
            log.error("Ошибка чтения файла отчета", e);
            throw new RuntimeException("Failed to read report file", e);
        }
    }

    /**
     * Заглушка для теста
     */
    private Alert getMockAlert(Long alertId) {
        Alert alert = new Alert();
        alert.setId(alertId);
        alert.setType(com.example.firesystem.enums.EventType.accident);
        alert.setStatus(com.example.firesystem.enums.StatusType.resolved);
        alert.setTimestamp(LocalDateTime.now());
        alert.setDescription("Fire detected in server room");

        com.example.firesystem.model.Sensor sensor = new com.example.firesystem.model.Sensor();
        sensor.setLocation("Server Room #3, 2nd floor");
        alert.setSensor(sensor);

        com.example.firesystem.model.User user = new com.example.firesystem.model.User();
        user.setUsername("Ivanov I.I.");
        alert.setAssignedTo(user);

        return alert;
    }

    private String translateEventTypeToLatin(com.example.firesystem.enums.EventType eventType) {
        if (eventType == null)
            return "UNKNOWN";
        return switch (eventType) {
            case accident -> "FIRE";
            case hard_braking -> "ALARM";
            case button -> "MAINTENANCE";
            default -> eventType.toString().toUpperCase();
        };
    }

    private String translateStatusToLatin(com.example.firesystem.enums.StatusType status) {
        if (status == null)
            return "UNKNOWN";
        return switch (status) {
            case new_status -> "NEW";
            case in_progress -> "IN PROGRESS";
            case resolved -> "RESOLVED";
            default -> status.toString().toUpperCase();
        };
    }

    /**
     * Простая версия без сохранения (только для демонстрации)
     */
    public byte[] generateSimplePdf(Long alertId) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
                contentStream.beginText();
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText("Fire Alert System Report");
                contentStream.endText();

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(100, 650);
                contentStream.showText("Alert ID: #" + alertId);
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Generated: " + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                contentStream.newLineAtOffset(0, -40);
                contentStream.showText("This is a demonstration of PDF generation.");
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("In production, this would include full incident details.");
                contentStream.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);

            log.info("Простой PDF для Alert #{} сгенерирован", alertId);
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Ошибка генерации простого PDF", e);
            throw new RuntimeException("Failed to generate simple PDF", e);
        }
    }
}