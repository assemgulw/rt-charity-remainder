package com.example.rtcharityremainderbot.bot;

import com.example.rtcharityremainderbot.config.BotConfig;
import com.example.rtcharityremainderbot.model.MoneyRepository;
import com.example.rtcharityremainderbot.model.User;
import com.example.rtcharityremainderbot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.util.*;

import static java.time.LocalTime.now;

@Slf4j
@Component
public class Bot extends TelegramLongPollingBot {

    @Autowired
    public Bot(UserRepository userRepository, BotConfig botConfig, MoneyRepository moneyRepository) {
        this.userRepository = userRepository;
        this.config = botConfig;
        this.moneyRepository = moneyRepository;
    }

    final BotConfig config;

    private final UserRepository userRepository;

    private final MoneyRepository moneyRepository;



    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (messageText.contains("/setMoney")) {
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
                    sendNewUserMessage(update, chatId);
                    break;
                case "Жинақ туралы мәлімет":
                    sendMoneyInfoMessage(chatId);
                    break;
                case "Құпия кодың":
                    sendPersonalCode(chatId);
                    break;
                case "send notification ✅":
                    if (hasGrants(chatId))
                    sendNotificationToAll();
                    break;
                case "Мен админ, бірақ мен де кодымды білгім келеді \uD83D\uDC40":
                    if (hasGrants(chatId))
                        sendPersonalCode(chatId);
                    break;
                case "send money info":
                    break;
                case "get users \uD83D\uDCDD":
                    if (hasGrants(chatId))
                        sendInfoAboutAllUsers();
                    break;
                default:
                    break;
            }
        }
    }

    private void sendInfoAboutAllUsers() {
        SendMessage authorisedMessage = new SendMessage();
        SendMessage notAuthorisedMessage = new SendMessage();

        List<User> allRegisteredUsers = (List<User>) userRepository.findRegisteredUsers();
        List<User> notRegisteredUsers = (List<User>) userRepository.findNotRegisteredUsers();

        List<Long> admins = userRepository.findAdmins();

        String authorisedText = "Барлық тіркелген адамдар: \n\n";

        for (int i=1;i<=allRegisteredUsers.size();i++){
            String userInfo = i + ". " + "@" + allRegisteredUsers.get(i-1).getUserName() +
                    " " + allRegisteredUsers.get(i-1).getFirstName() +
                    " " + allRegisteredUsers.get(i-1).getLastName() +
                    " " + allRegisteredUsers.get(i-1).getPrivateCode() +
                    " (id - " + allRegisteredUsers.get(i-1).getChatId() + ")" +
                    "\n";
            authorisedText += userInfo;
        }

        String notAuthorisedText = "Барлық тіркелмеген адамдар: \n\n";

        for (int i=1;i<=notRegisteredUsers.size();i++){
            String userInfo = i + ". " + "@" + notRegisteredUsers.get(i-1).getUserName() +
                    " " + notRegisteredUsers.get(i-1).getFirstName() +
                    " " + notRegisteredUsers.get(i-1).getLastName() +
                    " " + notRegisteredUsers.get(i-1).getPrivateCode() +
                    " (id - " + notRegisteredUsers.get(i-1).getChatId() + ")" +
                    "\n";
            notAuthorisedText += userInfo;
        }



        for (Long admin : admins) {

            authorisedMessage.setChatId(String.valueOf(admin));
            authorisedMessage.setText(authorisedText);
            notAuthorisedMessage.setChatId(String.valueOf(admin));
            notAuthorisedMessage.setText(notAuthorisedText);

            try {
                execute(authorisedMessage);
                execute(notAuthorisedMessage);
            } catch (TelegramApiException e) {
                log.error("Error occurred while sending the message: {}", e.getMessage());
            }
        }
    }

    private void sendNotificationToAll() {
        SendMessage message = new SendMessage();
        List<User> allUsers = (List<User>) userRepository.findRegisteredUsers();

        for (User user : allUsers) {
            String personalText = "Сәлем қайырымды жан!\n" +
                    "Жақсылық дәнегін анық та, ақылды жолмен егейік. \n" +
                    "\n" +
                    "Әр айдың 10-ына дейін қорға саларыңызды жіберуді еске саламыз.\n" +
                    "Каспи: +7 707 202 37 57 (Рахиля)\n" +
                    "\n" +
                    "Ниетіңіз қабыл болсын ❤\uFE0F\n" +
                    "Өтініш, садақаны аударым жасағанда кодыңды жазуды ұмытпа";
            message.setChatId(String.valueOf(user.getChatId()));
            message.setText(personalText);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("Error occurred while sending the message: {}", e.getMessage());
            }
        }
    }

    private boolean hasGrants(Long chatId) {
        return userRepository.checkIsAdminById(chatId);
    }

    private void sendPersonalCode(Long chatId) {
        String code = userRepository.findPrivateCodeById(chatId);
        SendMessage message = new SendMessage();

        String text = code!=null ? "Құпиялық кодың: " +  code :
                "Кешірім сұраймын. Қазір жүйені жөндеп жатыр. Жүйе қайта қосылғанда, сұрауыңа лезде қайтып келемін.";

        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred while sending the message: {}", e.getMessage());
        }
    }

    private void sendMoneyInfoMessage(Long chatId) {
        Double lastAmount = moneyRepository.getLastAmount();
        SendMessage message = new SendMessage();

        String text = "Кешірім сұраймын \uD83E\uDEF6 Қазір RT Tech qoldaw жүйені жөндеп жатыр. Жүйе қайта қосылғанда, сұрауыңа лезде қайтып келемін.";

        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred while sending the message: {}", e.getMessage());
        }
    }

    private void sendNewUserMessage(Update update, Long chatId) {
        Optional<User> userOptional = userRepository.findById(chatId);
        User user = userOptional.isPresent() ? userOptional.get() : new User();
        Date currentDate = new Date();

        user.setChatId(chatId);
        user.setUserName(update.getMessage().getChat().getUserName());
        user.setFirstName(update.getMessage().getChat().getFirstName());
        user.setLastName(update.getMessage().getChat().getLastName());
        if (Objects.equals(update.getMessage().getChat().getUserName(), "aikerim_abdulla") ||
                Objects.equals(update.getMessage().getChat().getUserName(), "assemgulw")) {
            user.setAdmin(true);
        } else {
            user.setAdmin(false);
        }
        user.setRegisteredTime(currentDate);

        if (userOptional.isEmpty()) {
            String newCode = generateCodes();
            while (userRepository.findUserByCode(newCode) != null) {
                newCode = generateCodes();
            }
            user.setPrivateCode(newCode);
        }
        userRepository.save(user);
        if (user.getAdmin()) {
            sendAdminMessage(chatId);
        } else {
            sendGreetingMessage(chatId);
            notifyAdmin(chatId);
        }
    }

    private String generateCodes() {
        Random random = new Random();
        String prefix = "3A";
        int randomNumber = random.nextInt(100) + 1;
        String formattedNumber = String.format("%02d", randomNumber);
        return prefix + formattedNumber;
    }

    private void sendAdminMessage(Long chatId) {
        SendMessage adminMessage = new SendMessage();

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardRow keyboardRow2 = new KeyboardRow();
        KeyboardRow keyboardRow3 = new KeyboardRow();

        KeyboardRow keyboardRow4 = new KeyboardRow();

        keyboardRow1.add("send notification ✅");
        keyboardRow2.add("get users \uD83D\uDCDD");
        keyboardRow3.add("Мен админ, бірақ мен де кодымды білгім келеді \uD83D\uDC40");
        keyboardRow4.add("send money info фичаны тез бітір");
        keyboardRows.add(keyboardRow1);
        keyboardRows.add(keyboardRow2);
        keyboardRows.add(keyboardRow3);
        keyboardRows.add(keyboardRow4);
        keyboardMarkup.setKeyboard(keyboardRows);

        String text = "Салем, админ! Қош келдің! \uD83D\uDE09";

        adminMessage.setChatId(String.valueOf(chatId));
        adminMessage.setText(text);
        adminMessage.setReplyMarkup(keyboardMarkup);

        try {
            execute(adminMessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred while sending the message: {}", e.getMessage());
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

    private void sendGreetingMessage(long chatId) {
        SendMessage greetingMessage = new SendMessage();

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardRow keyboardRow2 = new KeyboardRow();
        keyboardRow1.add("Жинақ туралы мәлімет");
        keyboardRow2.add("Құпия кодың");
        keyboardRows.add(keyboardRow1);
        keyboardRows.add(keyboardRow2);
        keyboardMarkup.setKeyboard(keyboardRows);

        String text = "Қош келдің шырайлы жан❤\uFE0F\n" +
                "\n" +
                "Мен РТ Кайырым ботымын. Ай сайын үзіп алмай ізгілік дәнегін отырғызуыңды еске салып, қызмет қыламын\uD83E\uDD32 \n" +
                "\n" +
                "Қор есебінде қолдаушымызды құпия ұстап  кодпен белгілейміз \uD83D\uDE09\n" +
                "Өтініш, садақаны аударым жасағанда кодыңды жазуды ұмытпа\uD83E\uDD13\uD83E\uDD70\n" +
                "\n" +
                "Ал кеттік, қызметімді осы айдың садақасын жіберуіңді сұрап бастайын\uD83E\uDEF6\n" +
                "Каспи: +7 707 202 37 57 (Рахиля)";

        greetingMessage.setChatId(String.valueOf(chatId));
        greetingMessage.setText(text);
        greetingMessage.setReplyMarkup(keyboardMarkup);

        try {
            execute(greetingMessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred while sending the message: {}", e.getMessage());
        }
    }

    private void notifyAdmin(long chatId) {
        SendMessage message = new SendMessage();
        Optional<User> userOptional = userRepository.findById(chatId);
        User user = userOptional.isPresent() ? userOptional.get() : null;

        List<Long> admins = userRepository.findAdmins();

        String text = "";
        if (user != null) {
            text = user.getFirstName() +
                    " " + user.getLastName() +
                    ", " + user.getUserName() + " started communication with bot.\n " +
                    userRepository.findRegisteredCount() + " out of all" +
                    userRepository.findAllUsersCount() + "\n" +
                    "Generated private code for new member: " + user.getPrivateCode();
        }

        for (Long admin : admins) {
            message.setChatId(String.valueOf(admin));
            message.setText(text);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("Error occurred while sending the message: {}", e.getMessage());
            }
        }
    }
}
