package ru.urfu.riddlebot;

import ru.urfu.riddlebot.bot.Bot;
import ru.urfu.riddlebot.service.Message;

import java.util.ArrayList;
import java.util.List;

public final class DummyBot implements Bot {
    private final List<Message> outcomingMessageList = new ArrayList<>();

    public List<Message> getOutcomingMessageList() {
        return List.copyOf(outcomingMessageList);
    }

    @Override
    public void sendMessage(Message msg, long id) {
        outcomingMessageList.add(msg);
    }
}