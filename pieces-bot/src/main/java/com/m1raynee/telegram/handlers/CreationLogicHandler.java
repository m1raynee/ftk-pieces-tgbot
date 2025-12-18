package com.m1raynee.telegram.handlers;

import io.github.natanimn.telebof.BotClient;
import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.annotations.MessageHandler;
import io.github.natanimn.telebof.types.updates.Message;

public class CreationLogicHandler {
    BotClient bot;

    public CreationLogicHandler(BotClient bot) {
        this.bot = bot;
    }

    @MessageHandler(commands = "create")
    void createCommand(BotContext ctx, Message msg) {
        ctx.setState(msg.chat.id, "selection_create");
        ctx.sendMessage(msg.chat.id, "Выберите сущность для создания:")
                .replyMarkup(null);
    }

}
