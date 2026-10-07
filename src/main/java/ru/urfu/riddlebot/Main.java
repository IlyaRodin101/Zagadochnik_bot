package ru.urfu.riddlebot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.urfu.riddlebot.bot.TelegramBot;
import ru.urfu.riddlebot.domain.RiddleBank;
import ru.urfu.riddlebot.service.MessageProcessingService;
import ru.urfu.riddlebot.service.RiddleMessageProcessingService;

public final class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String BOT_TOKEN = "BOT_TOKEN";

    private Main() {
    }

    static void main() {
        RiddleBank riddleBank = new RiddleBank();
        RiddleMessageProcessingService riddleMessageProcessingService = new RiddleMessageProcessingService(riddleBank);
        String token = System.getenv(BOT_TOKEN);
        if(token == null){
            LOGGER.error("Отсутствует токен!");
            return;
        }
        try{
            TelegramBot bot = new TelegramBot(token,riddleMessageProcessingService);
            bot.start();
        } catch (Exception e) {
            LOGGER.error("сообщение",e);
        }
    }
}