package ru.urfu.riddlebot.service;

import ru.urfu.riddlebot.bot.Bot;
import ru.urfu.riddlebot.domain.Riddle;
import ru.urfu.riddlebot.domain.RiddleBank;
import ru.urfu.riddlebot.domain.UserSession;
import ru.urfu.riddlebot.domain.Difficulty;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class RiddleMessageProcessingService implements MessageProcessingService {
    private final Map<Long, UserSession> sessions;
    private final RiddleBank riddleBank;
    public static final String EASY_LABEL = "Легкий";
    public static final String MEDIUM_LABEL = "Средний";
    public static final String HARD_LABEL = "Сложный";
    public static final List<String> DIFFICULTY_BUTTONS = List.of(EASY_LABEL, MEDIUM_LABEL, HARD_LABEL);
    public static final String CHOOSE_DIFFICULTY_MESSAGE = "Выберите сложность";
    public static final String CORRECT_ANSWER_MESSAGE = "Правильно! Выберите сложность следующей загадки.";
    public static final String WRONG_ANSWER_MESSAGE = "Неправильно! Попробуй ещё раз :)";

    public RiddleMessageProcessingService(RiddleBank riddleBank) {
        this.riddleBank = riddleBank;
        this.sessions = new HashMap<>();
    }

    private void handleDifficultyChoice(Message msg, long chatId, Bot bot, UserSession session) {
        Difficulty difficulty = switch (msg.text()) {
            case EASY_LABEL -> Difficulty.EASY;
            case MEDIUM_LABEL -> Difficulty.MEDIUM;
            case HARD_LABEL -> Difficulty.HARD;
            default -> null;
        };
        if (difficulty == null) {
            bot.sendMessage(new Message(CHOOSE_DIFFICULTY_MESSAGE, DIFFICULTY_BUTTONS), chatId);
            return;
        }
        Riddle riddle = riddleBank.getRandomRiddle(difficulty);
        session.setCurrentRiddle(riddle);
        bot.sendMessage(new Message(riddle.question()), chatId);
    }

    private void handleAnswer(Message msg, long chatId, Bot bot, UserSession session) {
        Riddle currentRiddle = session.getCurrentRiddle();
        if (msg.text().toLowerCase().contains(currentRiddle.answer().toLowerCase())) {
            bot.sendMessage(new Message(CORRECT_ANSWER_MESSAGE, DIFFICULTY_BUTTONS), chatId);
            session.setCurrentRiddle(null);
        } else {
            bot.sendMessage(new Message(WRONG_ANSWER_MESSAGE, DIFFICULTY_BUTTONS), chatId);
        }
    }

    @Override
    public void processMessage(Message msg, long chatId, Bot bot) {
        if (msg.text() == null) {
            return;
        }
        UserSession session = sessions.computeIfAbsent(chatId, id -> new UserSession());
        if (session.getCurrentRiddle() == null) {
            handleDifficultyChoice(msg, chatId, bot, session);
        } else {
            handleAnswer(msg, chatId, bot, session);
        }
    }
}
