package ru.urfu.riddlebot.domain;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class RiddleBank {
    private final Map<Difficulty, List<Riddle>> riddles;
    private final Random random;
    public RiddleBank(){
        random = new Random();
        riddles = Map.of(
                Difficulty.EASY, List.of(
                        new Riddle("Вопрос 1", "ответ1"),
                        new Riddle("Вопрос 2", "ответ2")
                ),
                Difficulty.MEDIUM, List.of(
                        new Riddle("Вопрос 3", "ответ3"),
                        new Riddle("Вопрос 4", "ответ4")
                ),
                Difficulty.HARD, List.of(
                        new Riddle("Вопрос 5", "ответ5"),
                        new Riddle("Вопрос 6", "ответ6")
                )
        );
    }
    public RiddleBank(Map<Difficulty, List<Riddle>> riddles){
        this.riddles = riddles;
        this.random = new Random();
    }
    public Riddle getRandomRiddle(Difficulty difficulty) {
        List<Riddle> list = riddles.get(difficulty);
        return list.get(random.nextInt(list.size()));
    }
}