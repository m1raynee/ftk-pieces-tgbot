package com.m1raynee.telegram.handlers;

import java.util.Map;
import com.m1raynee.telegram.filters.AdminFilter;
import com.m1raynee.telegram.utils.KeyboardUtil;
import com.m1raynee.telegram.utils.ReflectedUtil;

import io.github.natanimn.telebof.BotClient;
import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.annotations.MessageHandler;
import io.github.natanimn.telebof.types.updates.Message;

public class StartLogicHandler {
    public StartLogicHandler(BotClient bot) {
    }

    @MessageHandler(commands = "start", priority = 2)
    public void startAsTeacher(BotContext ctx, Message msg) {
        Long id = msg.chat.id;
        var ref = new ReflectedUtil<BotContext>(ctx);
        if (ref.getStateName(id) != null && !ref.getStateName(id).isEmpty()) {
            Map<String, Object> oldData = ctx.getStateData(id);
            ctx.clearState(id);
            ctx.getStateData(id).putAll(oldData);
            ctx.sendMessage(msg.chat.id, "Предыдущее действие отменено");
        }

        ctx.sendMessage(msg.chat.id, """
                Добро пожаловть в Базу деталей ФТК!
                Выберите действие на клавиатуре ⬇️
                """).replyMarkup(KeyboardUtil.baseKeyboard(AdminFilter.isAdmin(id)))
                .exec();
    }
}
