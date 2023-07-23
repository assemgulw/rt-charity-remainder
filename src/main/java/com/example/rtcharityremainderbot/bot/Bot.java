package com.example.rtcharityremainderbot.bot;

import com.example.rtcharityremainderbot.config.BotConfig;
import com.example.rtcharityremainderbot.model.User;
import com.example.rtcharityremainderbot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.time.LocalTime.now;

@Slf4j
@Component
public class Bot extends TelegramLongPollingBot {

    @Autowired
    public Bot(UserRepository userRepository, BotConfig botConfig) {
        this.userRepository = userRepository;
        this.config = botConfig;
    }

    final BotConfig config;

    private final UserRepository userRepository;



    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String userName = update.getMessage().getChat().getUserName();
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (messageText.contains("/send")) {
                String textToSend = messageText.substring(messageText.indexOf(" "));
                if (!textToSend.isEmpty()) {
                    List<User> users = (List<User>) userRepository.findAll();
                    for (User user : users) {
                        sendMessage(user.getChatId(), textToSend);
                    }
                }
            }

            switch (messageText) {
                case "/start":
                    Optional<User> userOptional = userRepository.findById(chatId);
                    Date currentDate = new Date();
                    userOptional.ifPresent(user -> {
                        user.setRegisteredTime(currentDate);
                        user.setChatId(chatId);
                        userRepository.save(user);
                    });
                    sendGreetingMessage(chatId, userName);
                    break;
                default:
                    break;
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
            log.error("Error occurred while sending the message: {}", e.getMessage());
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
            log.error("Error occurred while sending the message: {}", e.getMessage());
        }
    }
}
