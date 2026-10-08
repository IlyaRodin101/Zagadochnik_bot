package ru.urfu.riddlebot.service;

import java.util.List;

public record Message(String text, List<String> buttons) {
    public Message(String text) {
        this(text, List.of());
    }
}
