package ru.urfu.riddlebot.service;

import ru.urfu.riddlebot.bot.Bot;

public interface MessageProcessingService {
    void processMessage(Message msg, long chatId, Bot bot);
}
