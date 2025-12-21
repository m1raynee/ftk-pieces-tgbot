package com.m1raynee.telegram.handlers;

import com.m1raynee.telegram.filters.AdminFilter;
import com.m1raynee.telegram.utils.KeyboardUtil;
import com.m1raynee.telegram.utils.ReflectedUtil;

import io.github.natanimn.telebof.BotClient;
import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.annotations.MessageHandler;
import io.github.natanimn.telebof.enums.ParseMode;
import io.github.natanimn.telebof.types.updates.Message;

public class StartLogicHandler {
    public StartLogicHandler(BotClient bot) {
    }

    @MessageHandler(commands = "start", priority = 2)
    public void startAsTeacher(BotContext context, Message message) {
        Long id = message.chat.id;
        var ref = new ReflectedUtil<BotContext>(context);

        if (ref.clearStateUnless(id))
            context.sendMessage(id, "<i>Последнее состояние сброшено...</i>").parseMode(ParseMode.HTML).exec();

        context.sendMessage(id, """
                Добро пожаловть в Базу деталей ФТК!
                Выберите действие на клавиатуре ⬇️
                """).replyMarkup(KeyboardUtil.baseKeyboard(AdminFilter.isAdmin(id)))
                .exec();
    }

    @MessageHandler(commands = "delete")
    public void deleteCommand(BotContext context, Message message) {
        context.deleteMessage(message.chat.id, message.message_id).exec();
    }
}
