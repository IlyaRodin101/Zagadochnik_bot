public class Game {
    private int riddleNumber;
    private int wrongAnswers;

    public int getRiddleNumber() {
        return riddleNumber;
    }

    public int getWrongAnswers() {
        return wrongAnswers;
    }

    public void addWrongAnswer() {
        wrongAnswers++;
    }

    public void nextRiddle(int riddlesCount) {
        riddleNumber = (riddleNumber + 1) % riddlesCount;
        wrongAnswers = 0;
    }

    public void restart() {
        riddleNumber = 0;
        wrongAnswers = 0;
    }
}
