package com.m1raynee.telegram.scenarios;

import java.util.function.Predicate;

import com.m1raynee.db.HibernateConfiguration;
import com.m1raynee.db.entity.Student;
import com.m1raynee.telegram.utils.KeyboardUtil;
import com.m1raynee.telegram.utils.ReflectedUtil;

import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.filters.Filter;
import io.github.natanimn.telebof.filters.FilterExecutor;
import io.github.natanimn.telebof.types.inline.InlineQueryResult;
import io.github.natanimn.telebof.types.inline.InlineQueryResultArticle;
import io.github.natanimn.telebof.types.input.InputTextMessageContent;
import io.github.natanimn.telebof.types.updates.InlineQuery;

public class InlineSelectorScenario {

    public static FilterExecutor filterInlineState(Predicate<String> checker) {
        return filter -> {
            var r = new ReflectedUtil<Filter>(filter);
            return checker.test(
                    r.getStorage().getName(r.getUpdate().inline_query.from.id));
        };
    }

    public static FilterExecutor filterInlineResultState(Predicate<String> checker) {
        return filter -> {
            var r = new ReflectedUtil<Filter>(filter);
            return checker.test(
                    r.getStorage().getName(r.getUpdate().chosen_inline_result.from.id));
        };
    }

    public static void searchStudent(BotContext ctx, InlineQuery query) {
        if (query.query.isEmpty()) {
            ctx.answerInlineQuery(query.id, new InlineQueryResult[] {
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
                        return new InlineQueryResultArticle("STU-" + student.getId(), student.getName(),
                                new InputTextMessageContent(student.getName() + " (STU-" + student.getId() + ")"))
                                .description("STU-" + student.getId());
                    }).toArray(InlineQueryResult[]::new);
        });

        if (match.length < 1) {
            match = new InlineQueryResult[] {
                    KeyboardUtil.emptyQueryResult("❌ Ничего не найдено", "Ничего не найдено...")
                            .description("Студент не существует или не создан") };
        }

        ctx.answerInlineQuery(query.id, match).cacheTime(500).exec();
    }
}
