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
    private DummyBot dummyBot;
    private RiddleMessageProcessingService service;

    @BeforeEach
    public void setupTest() {
        dummyBot = new DummyBot();
        RiddleBank bank = new RiddleBank(Map.of(
                Difficulty.EASY, List.of(new Riddle("Не лает, не кусает, а в дом не пускает?", "замок")),
                Difficulty.MEDIUM, List.of(new Riddle("В каком городе зародилась джинсовая ткань?", "Генуя")),
                Difficulty.HARD, List.of(new Riddle(" Как часто подрывают (подводят) часы на Спасской башне?", "2 раза в сутки"))
        ));
        service = new RiddleMessageProcessingService(bank);
    }

    /// Given: пользователь ещё не выбирал сложность.
    ///
    /// When: он выбирает лёгкий уровень.
    ///
    /// Then: сервис присылает вопрос лёгкой загадки.
    @Test
    @DisplayName("Сервис должен прислать вопрос загадки в ответ на выбор сложности")
    void difficultyChoiceTest() {
        final Message request = new Message(RiddleMessageProcessingService.EASY_LABEL);

        service.processMessage(request, 0L, dummyBot);

        Assertions.assertEquals(1, dummyBot.getOutcomingMessageList().size());
        Assertions.assertEquals("Не лает, не кусает, а в дом не пускает?",
                dummyBot.getOutcomingMessageList().get(0).text());
    }
}