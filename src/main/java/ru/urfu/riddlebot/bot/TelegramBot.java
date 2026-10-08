package ru.urfu.riddlebot.bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.DefaultLongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.urfu.riddlebot.service.Message;
import ru.urfu.riddlebot.service.MessageProcessingService;

public final class TelegramBot extends DefaultLongPollingUpdateConsumer implements Bot {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBot.class);
    private final TelegramClient telegramClient;
    private final MessageProcessingService logic;
    private final String token;
    private final TelegramBotsLongPollingApplication application;

    public TelegramBot(String token, MessageProcessingService logic) {
        telegramClient = new OkHttpTelegramClient(token);
        this.logic = logic;
        this.token = token;
        application = new TelegramBotsLongPollingApplication();
    }
    public void start(){
        try{
            application.registerBot(token,this);
            LOGGER.info("Бот запущен :)");
            Thread.currentThread().join();
        } catch (Exception e) {
            LOGGER.error("сообщение", e);
        }
    }
    private Message convertFromUpdateToMessage(Update update){
        return new Message(update.getMessage().getText());
    }
    @Override
    public void sendMessage(Message msg,long id){
        try{
            telegramClient.execute(SendMessage.builder().text(msg.text()).chatId(id).build());
        } catch (TelegramApiException e){
            LOGGER.error("сообщение",e);
        }

    }
    @Override
    public void consume(Update update){
        if (!update.hasMessage()) {
            return;
        }
        long chatId = update.getMessage().getChatId();
        Message msg = convertFromUpdateToMessage(update);
        logic.processMessage(msg, chatId, this);
    }
}
