package com.m1raynee.telegram.handlers;

import com.m1raynee.db.HibernateConfiguration;
import com.m1raynee.db.entity.Student;
import com.m1raynee.telegram.utils.KeyboardUtil;
import com.m1raynee.telegram.utils.ReflectedUtil;

import io.github.natanimn.telebof.BotClient;
import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.annotations.MessageHandler;
import io.github.natanimn.telebof.filters.Filter;
import io.github.natanimn.telebof.types.inline.InlineQueryResult;
import io.github.natanimn.telebof.types.inline.InlineQueryResultArticle;
import io.github.natanimn.telebof.types.input.InputTextMessageContent;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardButton;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardMarkup;
import io.github.natanimn.telebof.types.updates.ChosenInlineResult;
import io.github.natanimn.telebof.types.updates.InlineQuery;
import io.github.natanimn.telebof.types.updates.Message;

public class StudentLogicHandler {
    // private final BotClient bot;

    boolean filterState(Filter filter) {
        var refl = new ReflectedUtil<Filter>(filter);
        return refl.getStorage().getName(refl.getUpdate().inline_query.from.id) == "setactive-name";
    }

    boolean filterStateAnswered(Filter filter) {
        var refl = new ReflectedUtil<Filter>(filter);
        System.out.println(refl.getUpdate().toString());
        return refl.getStorage().getName(refl.getUpdate().chosen_inline_result.from.id) == "setactive-name";
    }

    public StudentLogicHandler(BotClient bot) {
        // this.bot = bot;
        bot.onInline(this::filterState, this::inlineStudentSearch);
        bot.onChosenInlineResult(this::filterStateAnswered, this::inlineStudentAnswer);
    }

    @MessageHandler(commands = "setactive")
    public void setActiveStudent(BotContext ctx, Message msg) {
        ctx.setState(msg.chat.id, "setactive-name");
        var keyboard = new InlineKeyboardMarkup(new InlineKeyboardButton[] {
                new InlineKeyboardButton("Перейти в инлайн режим")
                        .switchInlineQueryCurrentChat("")
        });
        ctx.sendMessage(msg.chat.id, "Чтобы выбрать студента из базы, перейдите в инлайн режим")
                .replyMarkup(keyboard)
                .exec();
    }

    public void inlineStudentSearch(BotContext ctx, InlineQuery query) {
        if (query.query.isEmpty()) {
            ctx.answerInlineQuery(query.id, new InlineQueryResult[] {
                    KeyboardUtil.emptyQueryResult("Начните вводить имя", "Ничего не введено...")
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
                                new InputTextMessageContent(student.getName() + " (STU-" + student.getId() + ")"));
                    }).toArray(InlineQueryResult[]::new);
        });

        if (match.length < 1) {
            match = new InlineQueryResult[] {
                    KeyboardUtil.emptyQueryResult("Ничего не найдено", "Ничего не найдено...")
                            .description("Студент не существует или не создан") };
        }

        ctx.answerInlineQuery(query.id, match).cacheTime(500).exec();
    }

    public void inlineStudentAnswer(BotContext ctx, ChosenInlineResult result) {
        ctx.setState(result.from.id, "");
        ctx.getStateData(result.from.id).put("active", result.result_id);
        ctx.sendMessage(result.from.id, result.toString()).exec();
        ctx.sendMessage(result.from.id, ctx.getStateData(result.from.id).toString()).exec();
    }

}
