package com.example.firesystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.example.firesystem.bot.FireAlertBot;

@Configuration
public class BotConfig {
    @Bean
    public TelegramBotsApi telegramBotsApi(FireAlertBot fireAlertBot) throws Exception {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(fireAlertBot);
        return api;
    }
}