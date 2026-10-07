public class Main {
    public static void main(String[] args) {
        String token = System.getenv("TELEGRAM_BOT_TOKEN");
        if (token == null || token.trim().isEmpty()) {
            System.err.println("Задай переменную окружения TELEGRAM_BOT_TOKEN.");
            return;
        }

        TelegramBot bot = new TelegramBot(token);
        bot.start();
    }
}
