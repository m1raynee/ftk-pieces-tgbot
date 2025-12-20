package com.m1raynee.telegram.handlers;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.m1raynee.db.HibernateConfiguration;
import com.m1raynee.db.entity.Box;
import com.m1raynee.telegram.utils.KeyboardUtil;

import io.github.natanimn.telebof.BotClient;
import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.annotations.MessageHandler;
import io.github.natanimn.telebof.filters.CustomFilter;
import io.github.natanimn.telebof.types.updates.Message;
import io.github.natanimn.telebof.types.updates.Update;

public class StartLogicHandler {
    static Pattern patternStarBox = Pattern.compile("/start box(\\d+)");
    static Set<Long> adminIds = new HashSet<>();
    static {
        adminIds.add(1073934849L);
    }

    public StartLogicHandler(BotClient bot) {

    }

    public static class BoxFilter implements CustomFilter {
        @Override
        public boolean check(Update upd) {
            return patternStarBox.matcher(upd.message.text).find();
        }
    }

    @MessageHandler(filter = BoxFilter.class, priority = 0)
    public void startBoxCommand(BotContext ctx, Message msg) {
        var matcher = patternStarBox.matcher(msg.text);
        String box_index;
        if (matcher.find()) {
            box_index = matcher.group(1);
        } else {
            ctx.sendMessage(msg.chat.id, "Не найден номер коробки").exec();
            return;
        }
        var sessionFactory = HibernateConfiguration.getSessionFactory();
        Box box = sessionFactory.fromSession(session -> {
            return session.createSelectionQuery("from Box where index=:index", Box.class)
                    .setParameter("index", Integer.valueOf(
                            box_index))
                    .getSingleResult();
        });

        ctx.sendMessage(msg.chat.id, box.toString()).exec();
    }

    @MessageHandler(commands = "start", priority = 2)
    public void startSimpleCommand(BotContext ctx, Message msg) {
        ctx.sendMessage(msg.chat.id, """
                Добро пожаловть в Базу деталей ФТК!
                Выберите действие на клавиатуре ⬇️
                """).replyMarkup(
                KeyboardUtil.baseKeyboard(adminIds.contains(msg.chat.id)))
                .exec();
    }
}
