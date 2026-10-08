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
                        new Riddle("Без окон без дверей — полна горница людей.", "огурец"),
                        new Riddle("Сто одёжек — все без застёжек.", "капуста"),
                        new Riddle("Сидит дед, во сто шуб одет.","лук")
                ),
                Difficulty.MEDIUM, List.of(
                        new Riddle("Какая река является единственной, вытекающей из озера Байкал?", "ангара"),
                        new Riddle("Какое прозвище носил английский король Ричард I?", "львиное сердце"),
                        new Riddle("Как звали верного коня Дон Кихота в романе Мигеля де Сервантеса?", "росинант")
                ),
                Difficulty.HARD, List.of(
                        new Riddle("Какая планета Солнечной системы вращается «лежа на боку», так как её ось наклонена почти на 98 градусов?", "уран"),
                        new Riddle("Какой музыкальный инструмент держит в руках памятник музыканту на станциях метро «Маяковская» и «Кузнецкий Мост»?", "гармонь"),
                        new Riddle("Что изначально располагалось в здании в Париже, которое сейчас известно как Пантеон?", "церковь"),
                        new Riddle("Вилкой в глаз или в жопу раз?", "в тюрьме вилок нет, а в жопу не надо")
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