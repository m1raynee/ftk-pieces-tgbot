package com.m1raynee.telegram.handlers;

import com.m1raynee.telegram.scenarios.InlineSelectorScenario;
import com.m1raynee.telegram.utils.KeyboardUtil;

import io.github.natanimn.telebof.BotClient;
import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.annotations.MessageHandler;
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
                InlineSelectorScenario.filterInlineState(this::stateSetActive),
                InlineSelectorScenario::searchStudent);
        bot.onChosenInlineResult(
                InlineSelectorScenario.filterInlineResultState(this::stateSetActive),
                this::inlineStudentAnswer);
    }

    @MessageHandler(commands = "setactive")
    void setActiveStudent(BotContext ctx, Message msg) {
        ctx.setState(msg.chat.id, "setactive-student.name");
        ctx.sendMessage(msg.chat.id, "Чтобы выбрать студента из базы, перейдите в инлайн режим")
                .replyMarkup(KeyboardUtil.moveToInlineSingle())
                .exec();
    }

    void inlineStudentAnswer(BotContext ctx, ChosenInlineResult result) {
        ctx.setState(result.from.id, "");
        ctx.getStateData(result.from.id).put("active", result.result_id);
        ctx.sendMessage(result.from.id, result.toString()).exec();
        ctx.sendMessage(result.from.id, ctx.getStateData(result.from.id).toString()).exec();
    }

}
