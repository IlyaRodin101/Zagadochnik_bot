package ru.urfu.riddlebot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.urfu.riddlebot.domain.Difficulty;
import ru.urfu.riddlebot.domain.Riddle;
import ru.urfu.riddlebot.domain.RiddleBank;
import ru.urfu.riddlebot.service.Message;
import ru.urfu.riddlebot.service.RiddleMessageProcessingService;

import java.util.List;
import java.util.Map;


public final class RiddleMessageProcessingServiceTest {
    private final Riddle easyRiddle = new Riddle("Не лает, не кусает, а в дом не пускает?", "замок");
    private final Riddle mediumRiddle = new Riddle("Что можно увидеть с закрытыми глазами?", "сон");
    private final Riddle hardRiddle = new Riddle("Чем больше из неё берёшь, тем больше она становится. Что это?", "яма");

    private DummyBot dummyBot;
    private RiddleMessageProcessingService service;


    @BeforeEach
    public void setupTest() {
        dummyBot = new DummyBot();
        RiddleBank bank = new RiddleBank(Map.of(
                Difficulty.EASY, List.of(easyRiddle),
                Difficulty.MEDIUM, List.of(mediumRiddle),
                Difficulty.HARD, List.of(hardRiddle)
        ));
        service = new RiddleMessageProcessingService(bank);
    }


    private void send(String text, long chatId) {
        service.processMessage(new Message(text), chatId, dummyBot);
    }


    private String lastReply() {
        List<Message> messages = dummyBot.getOutcomingMessageList();
        return messages.get(messages.size() - 1).text();
    }


    @Test
    @DisplayName("Выбор лёгкой сложности: сервис присылает вопрос лёгкой загадки")
    void easyChoiceTest() {
        send(RiddleMessageProcessingService.EASY_LABEL, 0L);

        Assertions.assertEquals(1, dummyBot.getOutcomingMessageList().size());
        Assertions.assertEquals(easyRiddle.question(), lastReply());
    }

    @Test
    @DisplayName("Выбор средней сложности: сервис присылает вопрос средней загадки")
    void mediumChoiceTest() {
        send(RiddleMessageProcessingService.MEDIUM_LABEL, 0L);

        Assertions.assertEquals(1, dummyBot.getOutcomingMessageList().size());
        Assertions.assertEquals(mediumRiddle.question(), lastReply());
    }

    @Test
    @DisplayName("Выбор сложной сложности: сервис присылает вопрос сложной загадки")
    void hardChoiceTest() {
        send(RiddleMessageProcessingService.HARD_LABEL, 0L);

        Assertions.assertEquals(1, dummyBot.getOutcomingMessageList().size());
        Assertions.assertEquals(hardRiddle.question(), lastReply());
    }


    @Test
    @DisplayName("Сервис должен предложить выбрать сложность в ответ на незнакомый текст")
    void unknownTextTest() {
        send("привет", 0L);

        Assertions.assertEquals(1, dummyBot.getOutcomingMessageList().size());
        Assertions.assertEquals(RiddleMessageProcessingService.CHOOSE_DIFFICULTY_MESSAGE, lastReply());
    }


    @Test
    @DisplayName("Сервис должен подтвердить правильный ответ")
    void correctAnswerTest() {
        send(RiddleMessageProcessingService.EASY_LABEL, 0L);
        send(easyRiddle.answer(), 0L);

        Assertions.assertEquals(2, dummyBot.getOutcomingMessageList().size());
        Assertions.assertEquals(RiddleMessageProcessingService.CORRECT_ANSWER_MESSAGE, lastReply());
    }


    @Test
    @DisplayName("Сервис должен принимать ответ без учёта регистра и внутри фразы")
    void answerInsidePhraseTest() {
        send(RiddleMessageProcessingService.EASY_LABEL, 0L);
        send("Это " + easyRiddle.answer().toUpperCase() + "!", 0L);

        Assertions.assertEquals(RiddleMessageProcessingService.CORRECT_ANSWER_MESSAGE, lastReply());
    }


    @Test
    @DisplayName("Сервис должен сообщить о неправильном ответе")
    void wrongAnswerTest() {
        send(RiddleMessageProcessingService.EASY_LABEL, 0L);
        send("ключ", 0L);

        Assertions.assertEquals(2, dummyBot.getOutcomingMessageList().size());
        Assertions.assertEquals(RiddleMessageProcessingService.WRONG_ANSWER_MESSAGE, lastReply());
    }


    @Test
    @DisplayName("После неправильного ответа загадка остаётся активной")
    void wrongThenCorrectTest() {
        send(RiddleMessageProcessingService.EASY_LABEL, 0L);
        send("ключ", 0L);
        send(easyRiddle.answer(), 0L);

        Assertions.assertEquals(3, dummyBot.getOutcomingMessageList().size());
        Assertions.assertEquals(RiddleMessageProcessingService.WRONG_ANSWER_MESSAGE,
                dummyBot.getOutcomingMessageList().get(1).text());
        Assertions.assertEquals(RiddleMessageProcessingService.CORRECT_ANSWER_MESSAGE, lastReply());
    }


    @Test
    @DisplayName("После правильного ответа сессия сбрасывается")
    void sessionResetAfterCorrectAnswerTest() {
        send(RiddleMessageProcessingService.EASY_LABEL, 0L);
        send(easyRiddle.answer(), 0L);
        send("привет", 0L);

        Assertions.assertEquals(3, dummyBot.getOutcomingMessageList().size());
        Assertions.assertEquals(RiddleMessageProcessingService.CHOOSE_DIFFICULTY_MESSAGE, lastReply());
    }


    @Test
    @DisplayName("Состояния разных чатов не влияют друг на друга")
    void chatsAreIndependentTest() {
        send(RiddleMessageProcessingService.EASY_LABEL, 1L);
        send("привет", 2L);

        Assertions.assertEquals(2, dummyBot.getOutcomingMessageList().size());
        Assertions.assertEquals(RiddleMessageProcessingService.CHOOSE_DIFFICULTY_MESSAGE, lastReply());
    }

    @Test
    @DisplayName("Сервис не должен отвечать на сообщение без текста")
    void textIsNullTest() {
        send(null, 0L);

        Assertions.assertTrue(dummyBot.getOutcomingMessageList().isEmpty());
    }
    @Test
    @DisplayName("Просьба выбрать сложность содержит кнопки сложностей")
    void difficultyButtonsTest() {
        send("привет", 0L);

        Assertions.assertEquals(RiddleMessageProcessingService.DIFFICULTY_BUTTONS,
                dummyBot.getOutcomingMessageList().get(0).buttons());
    }

    @Test
    @DisplayName("Вопрос загадки отправляется без кнопок")
    void riddleWithoutButtonsTest() {
        send(RiddleMessageProcessingService.EASY_LABEL, 0L);

        Assertions.assertTrue(dummyBot.getOutcomingMessageList().get(0).buttons().isEmpty());
    }
}