package ru.urfu.riddlebot.domain;

public final class UserSession {
    private Riddle currentRiddle;

    public Riddle getCurrentRiddle() {
        return currentRiddle;
    }

    public void setCurrentRiddle(Riddle riddle) {
        currentRiddle = riddle;
    }
}
