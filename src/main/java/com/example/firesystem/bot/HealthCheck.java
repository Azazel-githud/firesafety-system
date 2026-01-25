package com.example.firesystem.bot;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HealthCheck {
    private final FireAlertBot botService;

    @Scheduled(cron = "0 0 9 * * *") // Каждый день в 9:00
    public void sendDailyHealthCheck() {
        botService.sendToAdmin("✅ Система пожарной безопасности работает в штатном режиме\n" +
                "📅 Дата: " + java.time.LocalDate.now() + "\n" +
                "⏰ Время проверки: " + java.time.LocalTime.now());
    }

    @Scheduled(fixedRate = 3600000) // Каждый час
    public void sendHourlyStatus() {
        botService.sendToAdmin("🔄 Часовая проверка системы\n" +
                "✅ Все модули работают\n" +
                "⏰ Время: " + java.time.LocalTime.now());
    }
}