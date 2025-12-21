package com.m1raynee.telegram.scenarios;

import com.m1raynee.db.HibernateConfiguration;
import com.m1raynee.db.entity.Box;
import com.m1raynee.db.entity.Piece;
import com.m1raynee.db.entity.Student;
import com.m1raynee.telegram.utils.KeyboardUtil;
import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.types.inline.InlineQueryResult;
import io.github.natanimn.telebof.types.inline.InlineQueryResultArticle;
import io.github.natanimn.telebof.types.input.InputTextMessageContent;
import io.github.natanimn.telebof.types.updates.InlineQuery;

public class InlineSelectorScenario {

    // --- 1. Вспомогательный интерфейс для маппинга ---
    @FunctionalInterface
    private interface ResultMapper<T> {
        InlineQueryResultArticle map(T entity);
    }

    // --- 2. Универсальный Метод для Поиска ---
    private static <T> void performInlineSearch(
            BotContext context,
            InlineQuery query,
            Class<T> entityClass,
            String jpqlQuery,
            String emptyPromptTitle,
            String emptyPromptDescription,
            String notFoundDescription,
            ResultMapper<T> mapper,
            int maxResults,
            int cacheTime) {

        // A. Обработка пустого запроса
        if (query.query.isEmpty()) {
            context.answerInlineQuery(query.id, new InlineQueryResult[] {
                    KeyboardUtil.emptyQueryResult(emptyPromptTitle, "Ничего не введено...")
                            .description(emptyPromptDescription)
            })
                    .cacheTime(10)
                    .exec();
            return;
        }

        var sessionFactory = HibernateConfiguration.getSessionFactory();

        InlineQueryResult[] match = sessionFactory.fromSession(session -> {
            return session
                    .createSelectionQuery(jpqlQuery, entityClass)
                    .setParameter("name", "%" + query.query + "%")
                    .setMaxResults(maxResults)
                    .getResultStream()
                    .map(mapper::map)
                    .toArray(InlineQueryResult[]::new);
        });

        if (match.length < 1) {
            match = new InlineQueryResult[] {
                    KeyboardUtil.emptyQueryResult("❌ Ничего не найдено", "Ничего не найдено...")
                            .description(notFoundDescription)
            };
        }

        context.answerInlineQuery(query.id, match).cacheTime(cacheTime).exec();
    }

    public static void searchStudent(BotContext context, InlineQuery query) {
        performInlineSearch(
                context,
                query,
                Student.class,
                "from Student s where s.name like :name",
                "🔎 Начните вводить имя ученика",
                "А затем выберите его в этом списке",
                "Студент не существует или не создан", // Описание ошибки
                (Student student) -> new InlineQueryResultArticle(
                        String.valueOf(student.getId()),
                        student.getName(),
                        new InputTextMessageContent(
                                student.getName() + " " + student.getTagId()))
                        .description(student.getTagId()),
                5,
                500);
    }

    public static void searchBox(BotContext context, InlineQuery query) {
        performInlineSearch(
                context,
                query,
                Box.class,
                "from Box b where b.name like :name",
                "🔎 Начните вводить название коробки",
                "А затем выберите его в этом списке",
                "Коробка с таким именем не существует",
                (Box box) -> new InlineQueryResultArticle(
                        box.getIndex().toString(),
                        box.getName(),
                        new InputTextMessageContent(box.getName() + " " + box.getTagId()))
                        .description(box.getTagId()),
                20,
                500);
    }

    public static void searchPiece(BotContext context, InlineQuery query) {
        performInlineSearch(
                context,
                query,
                Piece.class,
                "from Piece p where p.name like :name",
                "🔎 Начните вводить имя детали",
                "А затем выберите его в этом списке",
                "Деталь с таким именем не существует",
                (Piece piece) -> new InlineQueryResultArticle(
                        String.valueOf(piece.getId()),
                        piece.getName(),
                        new InputTextMessageContent(piece.getName() + " " + piece.getTagId()))
                        .description(piece.getTagId()),
                20,
                500);
    }
}