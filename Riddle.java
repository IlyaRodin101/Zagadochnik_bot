public class Riddle {
    private final String question;
    private final String[] answers;
    private final int correctAnswer;

    public Riddle(String question, String[] answers, int correctAnswer) {
        this.question = question;
        this.answers = answers;
        this.correctAnswer = correctAnswer;
    }

    public String getQuestion() {
        return question;
    }

    public String[] getAnswers() {
        return answers;
    }

    public String getCorrectAnswer() {
        return answers[correctAnswer];
    }

    public boolean isCorrect(String answer) {
        String normalizedAnswer = answer.trim();
        try {
            int choice = Integer.parseInt(normalizedAnswer);
            return choice == correctAnswer + 1;
        } catch (NumberFormatException exception) {
            return answers[correctAnswer].equalsIgnoreCase(normalizedAnswer);
        }
    }
}
