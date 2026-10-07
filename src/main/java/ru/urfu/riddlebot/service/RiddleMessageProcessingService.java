package ru.urfu.riddlebot.service;
import ru.urfu.riddlebot.bot.Bot;
import ru.urfu.riddlebot.domain.Riddle;
import ru.urfu.riddlebot.domain.RiddleBank;
import ru.urfu.riddlebot.domain.UserSession;
import ru.urfu.riddlebot.domain.Difficulty;
import java.util.Map;
import java.util.HashMap;

public class RiddleMessageProcessingService implements MessageProcessingService {
    private final Map<Long,UserSession> sessions;
    private final RiddleBank riddleBank;
    public static final String EASY_LABEL = "Легкий";
    public static final String MEDIUM_LABEL = "Средний";
    public static final String HARD_LABEL = "Сложный";

    public RiddleMessageProcessingService(RiddleBank riddleBank){
        this.riddleBank = riddleBank;
        this.sessions = new HashMap<>();
    }
    private void handleDifficultyChoice(Message msg,long chatId,Bot bot,UserSession session){
        Difficulty difficulty = switch(msg.text()){
            case EASY_LABEL -> Difficulty.EASY;
            case MEDIUM_LABEL -> Difficulty.MEDIUM;
            case HARD_LABEL -> Difficulty.HARD;
            default -> null;
        };
        if (difficulty == null){
            bot.sendMessage(new Message("Выберите сложность"), chatId);
            return;
        }
        Riddle riddle = riddleBank.getRandomRiddle(difficulty);
        session.setCurrentRiddle(riddle);
        bot.sendMessage(new Message(riddle.question()), chatId);
    }
    private void handleAnswer(Message msg,long chatId,Bot bot,UserSession session){
        Riddle currentRiddle = session.getCurrentRiddle();
        if(msg.text().toLowerCase().contains(currentRiddle.answer().toLowerCase())){
            bot.sendMessage(new Message("Правильно!"), chatId);
            session.setCurrentRiddle(null);
        }else{
            bot.sendMessage(new Message("Неправильно! Попробуй ещё раз :)"), chatId);
        }
    }
    @Override
    public void processMessage(Message msg, long chatId, Bot bot) {
        UserSession session = sessions.computeIfAbsent(chatId,id -> new UserSession());
        if (session.getCurrentRiddle() == null){
            handleDifficultyChoice(msg, chatId, bot, session);
        } else{
            handleAnswer(msg, chatId, bot, session);
        }
    }
}
