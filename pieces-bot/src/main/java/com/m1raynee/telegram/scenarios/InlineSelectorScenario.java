package com.m1raynee.telegram.scenarios;

import com.m1raynee.db.HibernateConfiguration;
import com.m1raynee.db.entity.Box;
import com.m1raynee.db.entity.Student;
import com.m1raynee.telegram.utils.KeyboardUtil;
import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.types.inline.InlineQueryResult;
import io.github.natanimn.telebof.types.inline.InlineQueryResultArticle;
import io.github.natanimn.telebof.types.input.InputTextMessageContent;
import io.github.natanimn.telebof.types.updates.InlineQuery;

public class InlineSelectorScenario {

    public static void searchStudent(BotContext context, InlineQuery query) {
        if (query.query.isEmpty()) {
            context.answerInlineQuery(query.id, new InlineQueryResult[] {
                    KeyboardUtil.emptyQueryResult("🔎 Начните вводить имя", "Ничего не введено...")
                            .description("А затем выберите его в этом списке") })
                    .cacheTime(10)
                    .exec();
            return;
        }

        var sessionFactory = HibernateConfiguration.getSessionFactory();

        InlineQueryResult[] match = sessionFactory.fromSession(session -> {
            return session
                    .createSelectionQuery("from Student s where s.name like :name", Student.class)
                    .setParameter("name", "%" + query.query + "%")
                    .setMaxResults(5)
                    .getResultStream()
                    .map(student -> {
                        return new InlineQueryResultArticle(student.getTagId(), student.getName(),
                                new InputTextMessageContent(student.getName() + " " + student.getTagId()))
                                .description(student.getTagId());
                    }).toArray(InlineQueryResult[]::new);
        });

        if (match.length < 1) {
            match = new InlineQueryResult[] {
                    KeyboardUtil.emptyQueryResult("❌ Ничего не найдено", "Ничего не найдено...")
                            .description("Студент не существует или не создан") };
        }

        context.answerInlineQuery(query.id, match).cacheTime(500).exec();
    }

    public static void searchBox(BotContext context, InlineQuery query) {
        if (query.query.isEmpty()) {
            context.answerInlineQuery(query.id, new InlineQueryResult[] {
                    KeyboardUtil.emptyQueryResult("🔎 Начните вводить название", "Ничего не введено...")
                            .description("А затем выберите его в этом списке") })
                    .cacheTime(10)
                    .exec();
            return;
        }

        var sessionFactory = HibernateConfiguration.getSessionFactory();

        InlineQueryResult[] match = sessionFactory.fromSession(session -> {
            return session
                    .createSelectionQuery("from Box b where b.name like :name", Box.class)
                    .setParameter("name", "%" + query.query + "%")
                    .setMaxResults(20)
                    .getResultStream()
                    .map(box -> {
                        return new InlineQueryResultArticle(box.getIndex().toString(), box.getName(),
                                new InputTextMessageContent(box.getName() + " " + box.getTagId()))
                                .description(box.getTagId());
                    }).toArray(InlineQueryResult[]::new);
        });

        if (match.length < 1) {
            match = new InlineQueryResult[] {
                    KeyboardUtil.emptyQueryResult("❌ Ничего не найдено", "Ничего не найдено...")
                            .description("Коробка с таким именем не существует") };
        }

        context.answerInlineQuery(query.id, match).cacheTime(500).exec();
    }

}
