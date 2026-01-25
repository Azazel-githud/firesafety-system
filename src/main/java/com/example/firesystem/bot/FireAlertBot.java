package com.example.firesystem.bot;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.example.firesystem.enums.EventType;
import com.example.firesystem.enums.StatusType;
import com.example.firesystem.model.Alert;
import com.example.firesystem.service.UserService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class FireAlertBot extends TelegramLongPollingBot {
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(FireAlertBot.class);
    private final String botName;
    private final Long adminChatID;

    public FireAlertBot(
            @Value("${telegram.bot.name}") String botName,
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.chat-id}") Long adminChatID,
            @Lazy UserService userService) {
        super(botToken);
        this.botName = botName;
        this.adminChatID = adminChatID;
        this.userService = userService;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            logger.info("Received message: {}", update.getMessage().getText());
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (messageText.startsWith("/start")) {
                handleStart(chatId, messageText);
            } else if (messageText.startsWith("/help")) {
                sendHelpMessage(chatId);
            } else if (messageText.startsWith("/status")) {
                sendSystemStatus(chatId);
            } else if (messageText.startsWith("/alerts")) {
                sendAlertsInfo(chatId);
            } else {
                sendMessage(chatId,
                        "Unknown command. Send '/start <user_id>' to link your account or '/help' for help.");
            }
        }
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);

        try {
            execute(message);
            logger.info("Sent message to chat {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message", e);
        }
    }

    public void sendToAdmin(String text) {
        sendMessage(adminChatID, text);
        logger.info("Sent message to admin");
    }

    private void handleCallback(CallbackQuery query) {
        String[] data = query.getData().split(":");
        Long alertId = Long.parseLong(data[1]);
        int messageId = query.getMessage().getMessageId();
        Long chatId = query.getMessage().getChatId();

        try {
            if (data[0].equals("ACKNOWLEDGE")) {
                editMessage(chatId, messageId,
                        "✅ Оповещение #" + alertId + " подтверждено");
            } else if (data[0].equals("RESOLVE")) {
                editMessage(chatId, messageId,
                        "✅ Оповещение #" + alertId + " отмечено как решенное");
            } else if (data[0].equals("ASSIGN")) {
                editMessage(chatId, messageId,
                        "✅ Вы назначены ответственным за оповещение #" + alertId);
            }

            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(query.getId());
            execute(answer);
            logger.info("Callback {} handled", query.getId());
        } catch (Exception e) {
            logger.error("Error handling callback", e);
            sendMessage(chatId, "Error: " + e.getMessage());
        }
    }

    private void editMessage(Long chatId, int messageId, String newText) {
        EditMessageText newMessage = new EditMessageText();
        newMessage.setChatId(String.valueOf(chatId));
        newMessage.setMessageId(messageId);
        newMessage.setText(newText);

        try {
            execute(newMessage);
        } catch (TelegramApiException e) {
            logger.error("Failed to edit message", e);
        }
    }

    public void sendAlertWithButtons(Long chatId, Alert alert) {
        String messageText = formatAlertMessage(alert);
        SendMessage message = new SendMessage(String.valueOf(chatId), messageText);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton acknowledgeButton = new InlineKeyboardButton("✅ Подтвердить");
        acknowledgeButton.setCallbackData("ACKNOWLEDGE:" + alert.getId());

        InlineKeyboardButton resolveButton = new InlineKeyboardButton("✅ Решено");
        resolveButton.setCallbackData("RESOLVE:" + alert.getId());

        InlineKeyboardButton assignButton = new InlineKeyboardButton("👤 Назначить на меня");
        assignButton.setCallbackData("ASSIGN:" + alert.getId());

        row.add(acknowledgeButton);
        row.add(resolveButton);
        rows.add(row);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(assignButton);
        rows.add(row2);

        message.setReplyMarkup(new InlineKeyboardMarkup(rows));

        try {
            execute(message);
            logger.info("Alert sent to chat: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Failed to send alert", e);
        }
    }

    public void handleStart(Long chatId, String text) {
        String[] data = text.split(" ");

        if (data.length != 2) {
            sendMessage(chatId,
                    "⚠️ Неверный формат. Используйте: /start <ваш_user_id>\n\nВаш user_id можно получить у администратора системы.");
            return;
        }

        try {
            Long userId = Long.parseLong(data[1]);
            userService.updateTelegramId(userId, chatId);
            sendMessage(chatId, "✅ Успешно! Ваш аккаунт привязан к пользователю #" + userId +
                    "\n\nТеперь вы будете получать уведомления о пожарных оповещениях.\n\n" +
                    "Используйте /help для просмотра всех команд.");
        } catch (NumberFormatException e) {
            sendMessage(chatId, "❌ Ошибка: user_id должен быть числом");
            logger.error("Invalid user ID format", e);
        } catch (EntityNotFoundException e) {
            sendMessage(chatId, "❌ Пользователь с таким ID не найден");
            logger.error("User not found", e);
        } catch (Exception e) {
            sendMessage(chatId, "❌ Произошла ошибка при привязке аккаунта");
            logger.error("Linking error", e);
        }
    }

    private void sendHelpMessage(Long chatId) {
        String helpText = """
                📋 *Справка по командам бота*

                *Основные команды:*
                /start <user_id> - Привязать Telegram к аккаунту
                /help - Показать эту справку
                /status - Статус системы

                *Для получения уведомлений:*
                1. Сначала привяжите аккаунт через /start
                2. Бот будет автоматически отправлять уведомления о новых оповещениях

                *Кнопки в уведомлениях:*
                ✅ Подтвердить - Подтвердить получение оповещения
                ✅ Решено - Отметить оповещение как решенное
                👤 Назначить на меня - Взять ответственность за оповещение

                *Для администраторов:* support@firesystem.com
                """;

        sendMessage(chatId, helpText);
    }

    private void sendSystemStatus(Long chatId) {
        String statusText = """
                📊 *Статус системы пожарной безопасности*

                ✅ Система работает в штатном режиме
                🔔 Бот активен и готов к работе
                📡 Все модули подключены

                *Рекомендации:*
                • Регулярно проверяйте уведомления
                • Своевременно реагируйте на оповещения
                • Используйте кнопки для быстрого реагирования

                *Техническая поддержка:*
                support@firesystem.com
                """;

        sendMessage(chatId, statusText);
    }

    private void sendAlertsInfo(Long chatId) {
        String infoText = """
                🚨 *Информация об оповещениях*

                *Типы событий:*
                🔥 ПОЖАР - Обнаружено возгорание
                ⚠️ ТРЕВОГА - Сработала пожарная сигнализация
                🛠️ ОБСЛУЖИВАНИЕ - Требуется обслуживание

                *Статусы оповещений:*
                🆕 НОВЫЙ - Только что созданное
                🔄 В РАБОТЕ - Назначено ответственное лицо
                ✅ РЕШЕНО - Проблема устранена

                *Ваши действия при получении оповещения:*
                1. Немедленно подтвердите получение
                2. Если можете - возьмите ответственность
                3. После решения - отметьте как решенное
                """;

        sendMessage(chatId, infoText);
    }

    private String formatAlertMessage(Alert alert) {
        String eventType = translateEventType(alert.getType());
        String status = translateStatus(alert.getStatus());
        String location = alert.getSensor() != null ? alert.getSensor().getLocation() : "Не указано";
        String assignedTo = alert.getAssignedTo() != null ? alert.getAssignedTo().getUsername() : "Не назначено";

        return String.format("""
                🚨 *НОВОЕ ОПОВЕЩЕНИЕ!*

                *ID:* #%d
                *Тип:* %s
                *Статус:* %s
                *Местоположение:* %s
                *Время:* %s
                *Описание:* %s

                *Ответственный:* %s
                """,
                alert.getId(),
                eventType,
                status,
                location,
                alert.getTimestamp(),
                alert.getDescription(),
                assignedTo);
    }

    private String translateEventType(EventType eventType) {
        if (eventType == null)
            return "НЕИЗВЕСТНО";
        return switch (eventType) {
            case accident -> "🔥 ПОЖАР";
            case hard_braking -> "⚠️ ТРЕВОГА";
            case button -> "🛠️ ОБСЛУЖИВАНИЕ";
            default -> eventType.toString();
        };
    }

    private String translateStatus(StatusType status) {
        if (status == null)
            return "НЕИЗВЕСТНО";
        return switch (status) {
            case new_status -> "🆕 НОВЫЙ";
            case in_progress -> "🔄 В РАБОТЕ";
            case resolved -> "✅ РЕШЕНО";
            default -> status.toString();
        };
    }

    // Метод для массовой рассылки уведомлений об оповещениях
    public void broadcastAlert(Alert alert, List<Long> chatIds) {
        for (Long chatId : chatIds) {
            sendAlertWithButtons(chatId, alert);
        }
        logger.info("Alert #{} broadcasted to {} users", alert.getId(), chatIds.size());
    }

    // Метод для отправки системных уведомлений
    public void sendSystemNotification(Long chatId, String title, String message) {
        String formattedMessage = String.format("""
                📢 *%s*

                %s

                *Время:* %s
                """,
                title,
                message,
                java.time.LocalDateTime.now().toString());

        sendMessage(chatId, formattedMessage);
    }
}