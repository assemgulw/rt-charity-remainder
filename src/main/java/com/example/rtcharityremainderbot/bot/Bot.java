package com.example.rtcharityremainderbot.bot;

import com.example.rtcharityremainderbot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class Bot extends TelegramLongPollingBot {

    final BotConfig config;

    public Bot(BotConfig config) {
        this.config = config;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String userName = update.getMessage().getChat().getUserName();
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            switch (messageText) {
                case "/start":
                    sendGreetingMessage(chatId, userName);
                    break;
                default:
                    sendMessage(chatId, "Sorry, text was not recognised");
            }
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    public String getBotToken() {
        return config.getBotToken();
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendGreetingMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        String text = String.format("Heello, %s! I'm sending you message for testing. Thank  you!", textToSend);
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
