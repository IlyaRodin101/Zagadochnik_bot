package ru.urfu.riddlebot.bot;
import ru.urfu.riddlebot.service.Message;

public interface Bot {
    void sendMessage(Message msg, long id);
}
