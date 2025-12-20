package com.m1raynee.telegram.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartNameReducerV7 {

    private static final int MAX_LENGTH = 60;

    // --- КОНСТАНТЫ МАППИНГА И ИСКЛЮЧЕНИЙ ---

    // 1. Фиксированный маппинг словосочетаний и их сокращений
    private static final Map<String, String> ABBREVIATION_MAP = new HashMap<>();
    static {
        ABBREVIATION_MAP.put("соединительный", "соед.");
        ABBREVIATION_MAP.put("соединительная", "соед.");
        ABBREVIATION_MAP.put("модульная", "мод.");
        ABBREVIATION_MAP.put("полумодульной", "/2мод.");
        ABBREVIATION_MAP.put("крестовидными", "крест.");
        ABBREVIATION_MAP.put("рифлёной", "риф.");
        ABBREVIATION_MAP.put("угловая", "угл.");
        ABBREVIATION_MAP.put("балка", "бал.");
        ABBREVIATION_MAP.put("деталь", "дет.");
        // Важно: слова из маппинга должны быть удалены из STOP_WORDS,
        // так как мы хотим их сокращать.
    }

    // 2. Список слов, которые остаются полными (если короткие) или сохраняются до
    // обрезки
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "колесо", "балка", "деталь", "штифт"));

    // Паттерн для поиска гласных
    private static final Pattern VOWEL_PATTERN = Pattern.compile("[аеёиоуыэюяaeiouy]", Pattern.CASE_INSENSITIVE);

    // --- ГРАММАТИЧЕСКАЯ ЛОГИКА (Fallback) ---

    private static String getRussianAbbreviation(String word) {
        String lowerWord = word.toLowerCase();

        // 1. Приоритет: Проверка фиксированного маппинга
        if (ABBREVIATION_MAP.containsKey(lowerWord)) {
            return ABBREVIATION_MAP.get(lowerWord);
        }

        // 2. Исключение: Не сокращать, если это стоп-слово, короткое слово или
        // составное слово
        if (word.length() <= 4 || word.contains("-") || STOP_WORDS.contains(lowerWord)) {
            return word;
        }

        // 3. Общее грамматическое сокращение (для слов, не вошедших в маппинг)
        Matcher matcher = VOWEL_PATTERN.matcher(word);

        if (matcher.find()) {
            int firstVowelIndex = matcher.start();

            if (firstVowelIndex > 1) {
                // Сокращаем до согласной перед первой гласной.
                return word.substring(0, firstVowelIndex) + ".";
            } else {
                // Fallback: Угловая -> Угл. (3-4 символа)
                int fallbackLength = Math.min(word.length(), 4);
                return word.substring(0, fallbackLength) + ".";
            }
        }

        return word;
    }

    // --- ОСНОВНАЯ ФУНКЦИЯ ---

    public static String reducePartName(String originalName) {
        if (originalName == null || originalName.isEmpty()) {
            return "";
        }

        // --- ЭТАП 1: Очистка (Только удаление скобок) ---
        String cleanedName = originalName.replaceAll("\\s*\\([^)]*\\)", "").trim();

        if (cleanedName.length() <= MAX_LENGTH) {
            return cleanedName;
        }

        // --- ЭТАП 2: Сокращение с использованием маппинга ---

        // Паттерн для поиска чисел/числительных и обычных слов
        Pattern acronymPattern = Pattern.compile("([0-9]+[хx-]?[0-9]*)|([А-Яа-яЁёA-Za-z-]+)");
        Matcher matcher = acronymPattern.matcher(cleanedName);

        StringBuilder finalBuilder = new StringBuilder();

        while (matcher.find()) {
            String match = matcher.group();
            if (match.isEmpty())
                continue;

            // Если match содержит только пробел, пропускаем
            if (match.trim().isEmpty())
                continue;

            // Группа 1 ловит числа/числительные
            if (matcher.group(1) != null) {
                // Числа и числительные остаются полностью
                finalBuilder.append(match);

            } else {
                // Применяем маппинг или общее сокращение
                finalBuilder.append(getRussianAbbreviation(match));
            }

            finalBuilder.append(" ");

            // Ранний выход, если строка стала слишком длинной
            if (finalBuilder.length() >= MAX_LENGTH + 5) {
                break;
            }
        }

        String finalReducedName = finalBuilder.toString().trim();

        // 3. Обрезание до MAX_LENGTH
        if (finalReducedName.length() > MAX_LENGTH) {
            return finalReducedName.substring(0, MAX_LENGTH);
        }

        return finalReducedName;
    }

    public static void main(String[] args) {
        String[] examples = {
                "Белая соединительная деталь (как трубка, только одна половина, с круглым отверстием)",
                "Светло-серая 5-модульная балка полумодульной толщины",
                "Жёлтая угловая балка 3х3 с закруглённым соединением, полумодульной толщины",
                "Чёрная 4-модульная балка с внешними крестовидными отверстиями, полумодульной толщины",
                "Салатовая 4-модульная балка с внешними крестовидными отверстиями и утолщением на одном конце, полумодульной толщины",
                "Чёрное колесо 18х14 мм с рифлёной шиной и крестовидным отверстием"
        };

        System.out.println("--- Фиксированный маппинг: " + ABBREVIATION_MAP + " ---");
        System.out.println(
                "-------------------------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println(String.format("%-10s | %-20s | %s", "Длина", "Сокращенное название", "Исходное название"));
        System.out.println(
                "-------------------------------------------------------------------------------------------------------------------------------------------------------------");

        for (String name : examples) {
            String reduced = reducePartName(name);
            System.out.println(String.format("%-10d | %-20s | %s", reduced.length(), reduced, name));
        }
    }
}