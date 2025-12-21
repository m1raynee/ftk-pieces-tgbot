package com.m1raynee.telegram.handlers;

import com.m1raynee.telegram.scenarios.InlineSelectorScenario;
import com.m1raynee.telegram.utils.FilterUtils;
import com.m1raynee.telegram.utils.KeyboardUtil;

import io.github.natanimn.telebof.BotClient;
import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.annotations.MessageHandler;
import io.github.natanimn.telebof.enums.ParseMode;
import io.github.natanimn.telebof.types.updates.ChosenInlineResult;
import io.github.natanimn.telebof.types.updates.Message;

public class StudentLogicHandler {
    // private final BotClient bot;

    boolean stateSetActive(String state) {
        return state == "setactive-student.name";
    }

    public StudentLogicHandler(BotClient bot) {
        // this.bot = bot;
        bot.onInline(
                FilterUtils.filterInlineState(this::stateSetActive),
                InlineSelectorScenario::searchStudent);
        bot.onChosenInlineResult(
                FilterUtils.filterInlineResultState(this::stateSetActive),
                this::inlineStudentAnswer);
    }

    @MessageHandler(commands = "setactive")
    void setActiveStudent(BotContext context, Message message) {
        context.setState(message.chat.id, "setactive-student.name");
        context.sendMessage(message.chat.id, "Чтобы выбрать студента из базы, перейдите в инлайн режим")
                .parseMode(ParseMode.HTML)
                .replyMarkup(KeyboardUtil.moveToInlineSingle())
                .exec();
    }

    void inlineStudentAnswer(BotContext context, ChosenInlineResult result) {
        context.setState(result.from.id, "");
        context.getStateData(result.from.id).put("activeStudent", result.result_id);
        context.sendMessage(result.from.id, "Установлен исполнитель (STU-%s)".formatted(result.result_id)).exec();
    }

}
