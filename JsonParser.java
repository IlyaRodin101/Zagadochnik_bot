import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonParser {
    private final String json;
    private int position;

    private JsonParser(String json) {
        this.json = json;
    }

    public static Object parse(String json) {
        JsonParser parser = new JsonParser(json);
        Object value = parser.parseValue();
        parser.skipWhitespace();
        if (parser.position != json.length()) {
            throw new IllegalArgumentException("Лишние данные после JSON.");
        }
        return value;
    }

    private Object parseValue() {
        skipWhitespace();
        if (position >= json.length()) {
            throw new IllegalArgumentException("Неожиданный конец JSON.");
        }

        char character = json.charAt(position);
        if (character == '{') {
            return parseObject();
        }
        if (character == '[') {
            return parseArray();
        }
        if (character == '"') {
            return parseString();
        }
        if (character == 't') {
            return parseLiteral("true", Boolean.TRUE);
        }
        if (character == 'f') {
            return parseLiteral("false", Boolean.FALSE);
        }
        if (character == 'n') {
            return parseLiteral("null", null);
        }
        if (character == '-' || Character.isDigit(character)) {
            return parseNumber();
        }
        throw new IllegalArgumentException("Некорректное значение JSON.");
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        position++;
        skipWhitespace();
        if (take('}')) {
            return result;
        }

        while (true) {
            skipWhitespace();
            if (position >= json.length() || json.charAt(position) != '"') {
                throw new IllegalArgumentException("Ожидалось имя поля JSON.");
            }
            String name = parseString();
            skipWhitespace();
            expect(':');
            result.put(name, parseValue());
            skipWhitespace();
            if (take('}')) {
                return result;
            }
            expect(',');
        }
    }

    private List<Object> parseArray() {
        List<Object> result = new ArrayList<Object>();
        position++;
        skipWhitespace();
        if (take(']')) {
            return result;
        }

        while (true) {
            result.add(parseValue());
            skipWhitespace();
            if (take(']')) {
                return result;
            }
            expect(',');
        }
    }

    private String parseString() {
        expect('"');
        StringBuilder result = new StringBuilder();
        while (position < json.length()) {
            char character = json.charAt(position++);
            if (character == '"') {
                return result.toString();
            }
            if (character == '\\') {
                if (position >= json.length()) {
                    throw new IllegalArgumentException("Некорректная escape-последовательность JSON.");
                }
                char escaped = json.charAt(position++);
                switch (escaped) {
                    case '"':
                    case '\\':
                    case '/':
                        result.append(escaped);
                        break;
                    case 'b':
                        result.append('\b');
                        break;
                    case 'f':
                        result.append('\f');
                        break;
                    case 'n':
                        result.append('\n');
                        break;
                    case 'r':
                        result.append('\r');
                        break;
                    case 't':
                        result.append('\t');
                        break;
                    case 'u':
                        result.append(parseUnicodeCharacter());
                        break;
                    default:
                        throw new IllegalArgumentException("Некорректная escape-последовательность JSON.");
                }
            } else {
                if (character < 0x20) {
                    throw new IllegalArgumentException("Недопустимый символ в строке JSON.");
                }
                result.append(character);
            }
        }
        throw new IllegalArgumentException("Незакрытая строка JSON.");
    }

    private char parseUnicodeCharacter() {
        if (position + 4 > json.length()) {
            throw new IllegalArgumentException("Некорректный Unicode-символ JSON.");
        }
        try {
            char result = (char) Integer.parseInt(json.substring(position, position + 4), 16);
            position += 4;
            return result;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Некорректный Unicode-символ JSON.");
        }
    }

    private Object parseNumber() {
        int start = position;
        if (take('-') && position >= json.length()) {
            throw new IllegalArgumentException("Некорректное число JSON.");
        }
        while (position < json.length() && Character.isDigit(json.charAt(position))) {
            position++;
        }
        if (take('.')) {
            readDigits();
        }
        if (take('e') || take('E')) {
            if (!take('+')) {
                take('-');
            }
            readDigits();
        }
        try {
            return Double.valueOf(json.substring(start, position));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Некорректное число JSON.");
        }
    }

    private void readDigits() {
        int start = position;
        while (position < json.length() && Character.isDigit(json.charAt(position))) {
            position++;
        }
        if (start == position) {
            throw new IllegalArgumentException("Некорректное число JSON.");
        }
    }

    private Object parseLiteral(String literal, Object value) {
        if (!json.startsWith(literal, position)) {
            throw new IllegalArgumentException("Некорректное значение JSON.");
        }
        position += literal.length();
        return value;
    }

    private void skipWhitespace() {
        while (position < json.length() && Character.isWhitespace(json.charAt(position))) {
            position++;
        }
    }

    private boolean take(char expected) {
        if (position < json.length() && json.charAt(position) == expected) {
            position++;
            return true;
        }
        return false;
    }

    private void expect(char expected) {
        if (!take(expected)) {
            throw new IllegalArgumentException("Некорректная структура JSON.");
        }
    }
}
