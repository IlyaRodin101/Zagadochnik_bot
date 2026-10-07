import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelegramBot {
    private static final int POLLING_TIMEOUT_SECONDS = 30;

    private final String token;
    private final List<Riddle> riddles;
    private final Map<Long, Game> games;
    private long nextUpdateId;

    public TelegramBot(String token) {
        this.token = token;
        this.riddles = createRiddles();
        this.games = new HashMap<Long, Game>();
    }

    public void start() {
        System.out.println("Бот запущен.");
        while (true) {
            try {
                List<Object> updates = getUpdates();
                for (Object update : updates) {
                    Map<String, Object> updateData = asMap(update);
                    handleUpdate(updateData);
                    Object updateId = updateData.get("update_id");
                    if (!(updateId instanceof Number)) {
                        throw new IllegalArgumentException("В обновлении Telegram нет номера.");
                    }
                    nextUpdateId = ((Number) updateId).longValue() + 1;
                }
            } catch (IOException | IllegalArgumentException exception) {
                System.err.println("Не удалось обработать обновления Telegram: " + exception.getMessage());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private List<Object> getUpdates() throws IOException {
        String address = getApiAddress("getUpdates") + "?timeout=" + POLLING_TIMEOUT_SECONDS
                + "&offset=" + nextUpdateId;
        Map<String, Object> response = asMap(request(address, null));
        if (!Boolean.TRUE.equals(response.get("ok"))) {
            throw new IOException("Telegram не подтвердил получение обновлений.");
        }
        return asList(response.get("result"));
    }

    private void handleUpdate(Map<String, Object> update) throws IOException {
        Object messageObject = update.get("message");
        if (!(messageObject instanceof Map)) {
            return;
        }
        Map<String, Object> message = asMap(messageObject);
        Object chatObject = message.get("chat");
        Object textObject = message.get("text");
        if (!(chatObject instanceof Map) || !(textObject instanceof String)) {
            return;
        }

        Map<String, Object> chat = asMap(chatObject);
        Object chatIdObject = chat.get("id");
        if (!(chatIdObject instanceof Number)) {
            return;
        }
        long chatId = ((Number) chatIdObject).longValue();
        String text = (String) textObject;

        if (text.equals("/start") || text.startsWith("/start@")) {
            Game game = new Game();
            games.put(chatId, game);
            sendRiddleMessage(chatId, formatRiddle(game), game);
            return;
        }
        if (text.equals("/help") || text.startsWith("/help@")) {
            sendMessage(chatId, "Нажми кнопку с ответом на загадку. "
                    + "После первой ошибки будет ещё одна попытка. Чтобы начать заново, отправь /start.");
            return;
        }

        Game game = games.get(chatId);
        if (game == null) {
            sendMessage(chatId, "Чтобы начать игру, отправь /start.");
            return;
        }

        Riddle riddle = riddles.get(game.getRiddleNumber());
        if (riddle.isCorrect(text)) {
            game.nextRiddle(riddles.size());
            sendRiddleMessage(chatId, "Верно!\n\n" + formatRiddle(game), game);
        } else if (game.getWrongAnswers() == 0) {
            game.addWrongAnswer();
            sendRiddleMessage(chatId,
                    "Неверно. Это первая ошибка — попробуй ещё раз:\n\n" + formatRiddle(game), game);
        } else {
            String correctAnswer = riddle.getCorrectAnswer();
            game.nextRiddle(riddles.size());
            sendRiddleMessage(chatId, "Неверно. Правильный ответ: " + correctAnswer
                    + "\n\n" + formatRiddle(game), game);
        }
    }

    private String formatRiddle(Game game) {
        Riddle riddle = riddles.get(game.getRiddleNumber());
        StringBuilder message = new StringBuilder();
        message.append("Загадка ").append(game.getRiddleNumber() + 1)
                .append(" из ").append(riddles.size()).append(":\n")
                .append(riddle.getQuestion())
                .append("\n\nВыбери один из четырёх вариантов кнопкой.");
        return message.toString();
    }

    private void sendRiddleMessage(long chatId, String text, Game game) throws IOException {
        String form = "chat_id=" + chatId + "&text="
                + URLEncoder.encode(text, StandardCharsets.UTF_8.name())
                + "&reply_markup="
                + URLEncoder.encode(createKeyboard(riddles.get(game.getRiddleNumber())),
                        StandardCharsets.UTF_8.name());
        Map<String, Object> response = asMap(request(getApiAddress("sendMessage"), form));
        if (!Boolean.TRUE.equals(response.get("ok"))) {
            throw new IOException("Telegram не подтвердил отправку сообщения.");
        }
    }

    private void sendMessage(long chatId, String text) throws IOException {
        String form = "chat_id=" + chatId + "&text="
                + URLEncoder.encode(text, StandardCharsets.UTF_8.name());
        Map<String, Object> response = asMap(request(getApiAddress("sendMessage"), form));
        if (!Boolean.TRUE.equals(response.get("ok"))) {
            throw new IOException("Telegram не подтвердил отправку сообщения.");
        }
    }

    private String createKeyboard(Riddle riddle) {
        String[] answers = riddle.getAnswers();
        StringBuilder keyboard = new StringBuilder();
        keyboard.append("{\"keyboard\":[");
        for (int i = 0; i < answers.length; i += 2) {
            if (i > 0) {
                keyboard.append(',');
            }
            keyboard.append("[\"").append(escapeJson(answers[i])).append("\",\"")
                    .append(escapeJson(answers[i + 1])).append("\"]");
        }
        keyboard.append("],\"resize_keyboard\":true,\"one_time_keyboard\":true}");
        return keyboard.toString();
    }

    private String escapeJson(String text) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (character == '"' || character == '\\') {
                escaped.append('\\').append(character);
            } else if (character < 0x20) {
                escaped.append(String.format("\\u%04x", (int) character));
            } else {
                escaped.append(character);
            }
        }
        return escaped.toString();
    }

    private Object request(String address, String form) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout((POLLING_TIMEOUT_SECONDS + 10) * 1000);
        try {
            if (form == null) {
                connection.setRequestMethod("GET");
            } else {
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                byte[] data = form.getBytes(StandardCharsets.UTF_8);
                try (java.io.OutputStream output = connection.getOutputStream()) {
                    output.write(data);
                }
            }

            int statusCode = connection.getResponseCode();
            InputStream stream = statusCode >= 200 && statusCode < 300
                    ? connection.getInputStream() : connection.getErrorStream();
            String body = readResponse(stream);
            if (statusCode < 200 || statusCode >= 300) {
                throw new IOException("Telegram API вернул HTTP " + statusCode + ": " + body);
            }
            return JsonParser.parse(body);
        } finally {
            connection.disconnect();
        }
    }

    private String readResponse(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    private String getApiAddress(String method) {
        return "https://api.telegram.org/bot" + token + "/" + method;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (!(value instanceof Map)) {
            throw new IllegalArgumentException("Telegram вернул неожиданный JSON-объект.");
        }
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private List<Object> asList(Object value) {
        if (!(value instanceof List)) {
            throw new IllegalArgumentException("Telegram вернул неожиданный список обновлений.");
        }
        return (List<Object>) value;
    }

    private List<Riddle> createRiddles() {
        List<Riddle> list = new ArrayList<Riddle>();
        list.add(new Riddle("Зимой и летом одним цветом.", new String[]{"Берёза", "Ёлка", "Дуб", "Рябина"}, 1));
        list.add(new Riddle("Без окон, без дверей, полна горница людей.", new String[]{"Арбуз", "Дом", "Огурец", "Тыква"}, 0));
        list.add(new Riddle("Два конца, два кольца, посередине гвоздик.", new String[]{"Молоток", "Ножницы", "Плоскогубцы", "Компас"}, 1));
        list.add(new Riddle("Не лает, не кусает, а в дом не пускает.", new String[]{"Замок", "Собака", "Звонок", "Забор"}, 0));
        list.add(new Riddle("Сто одёжек и все без застёжек.", new String[]{"Капуста", "Лук", "Кукуруза", "Подушка"}, 0));
        list.add(new Riddle("Висит груша — нельзя скушать.", new String[]{"Лампочка", "Груша", "Игрушка", "Сосулька"}, 0));
        list.add(new Riddle("Четыре ноги, а ходить не умеет.", new String[]{"Кошка", "Стол", "Лошадь", "Стул"}, 1));
        list.add(new Riddle("Что можно увидеть с закрытыми глазами?", new String[]{"Темноту", "Сон", "Облако", "Солнце"}, 1));
        list.add(new Riddle("Без рук, без ног, а рисовать умеет.", new String[]{"Карандаш", "Мороз", "Художник", "Ветер"}, 1));
        list.add(new Riddle("По небу плывёт, белой ватой зовётся.", new String[]{"Туман", "Облако", "Снег", "Дым"}, 1));
        list.add(new Riddle("Не огонь, а жжётся.", new String[]{"Перец", "Лёд", "Уголь", "Крапива"}, 3));
        list.add(new Riddle("Утром на четырёх ногах, днём на двух, вечером на трёх.", new String[]{"Собака", "Человек", "Стол", "Птица"}, 1));
        list.add(new Riddle("Всегда во рту, а не проглотишь.", new String[]{"Язык", "Зуб", "Ложка", "Конфета"}, 0));
        list.add(new Riddle("Без языка, а рассказывает.", new String[]{"Книга", "Радио", "Телефон", "Телевизор"}, 0));
        list.add(new Riddle("Чем больше из неё берёшь, тем больше она становится.", new String[]{"Корзина", "Яма", "Бутылка", "Куча"}, 1));
        list.add(new Riddle("В воде родится, а воды боится.", new String[]{"Соль", "Лёд", "Рыба", "Водоросль"}, 0));
        list.add(new Riddle("Кто говорит на всех языках?", new String[]{"Переводчик", "Эхо", "Попугай", "Учитель"}, 1));
        list.add(new Riddle("Что принадлежит тебе, но другие пользуются этим чаще?", new String[]{"Твой телефон", "Твоё имя", "Твоя книга", "Твой рюкзак"}, 1));
        list.add(new Riddle("Без рук, без ног, а ворота открывает.", new String[]{"Ключ", "Замок", "Звонок", "Ветер"}, 0));
        list.add(new Riddle("У кого есть шляпа без головы и нога без сапога?", new String[]{"У гриба", "У человека", "У дерева", "У стола"}, 0));
        return list;
    }
}
