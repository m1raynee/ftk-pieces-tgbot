package com.m1raynee.telegram.handlers;

import java.util.regex.Pattern;

import com.m1raynee.db.HibernateConfiguration;
import com.m1raynee.db.entity.Piece;
import com.m1raynee.db.entity.PieceAction;
import com.m1raynee.db.entity.Student;
import com.m1raynee.db.enums.ActionState;
import com.m1raynee.telegram.scenarios.InlineSelectorScenario;
import com.m1raynee.telegram.utils.FilterUtils;
import com.m1raynee.telegram.utils.ReflectedUtil;

import io.github.natanimn.telebof.BotClient;
import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.filters.Filter;
import io.github.natanimn.telebof.types.updates.ChosenInlineResult;
import io.github.natanimn.telebof.types.updates.Message;

public class PieceActionLogicHandler {
    boolean statePerformer(String state) {
        return state == "performer";
    }

    static Pattern patternDigits = Pattern.compile("\\d+");

    public PieceActionLogicHandler(BotClient bot) {
        bot.onInline(
                FilterUtils.filterInlineState(this::statePerformer),
                InlineSelectorScenario::searchStudent);
        bot.onChosenInlineResult(
                FilterUtils.filterInlineResultState(this::statePerformer),
                PieceActionLogicHandler::inlinePerformerAnswer);

        bot.onMessage(filter -> {
            var ref = new ReflectedUtil<Filter>(filter);
            return ref.getStateName(ref.getUpdate().message.from.id) != null
                    && ref.getStateName(ref.getUpdate().message.from.id).equals("PieceAction-amount");
        }, PieceActionLogicHandler::createAction);
    }

    public static void createAction(BotContext context, Message message) {
        if (!message.text.matches("\\d+")) {
            context.sendMessage(message.chat.id, "Это не число");
            return;
        }

        var userId = message.from.id;
        var data = context.getStateData(userId);
        var performerId = Long.valueOf((String) data.get("activeStudent"));
        var requesterId = Long.valueOf((String) data.get("requesterId"));
        var piece = (Piece) data.get("lookupPiece");

        var requester = new Student();
        requester.setId(requesterId);
        var performer = new Student();
        performer.setId(performerId);

        var action = new PieceAction(piece, requester, performer, Integer.valueOf(message.text), ActionState.TAKEN);

        HibernateConfiguration.getSessionFactory().inTransaction(session -> {
            session.persist(action);
        });

        context.setState(userId, "");

        context.sendMessage(message.chat.id, "Новое действие создано").exec();
    }

    public static void inlinePerformerAnswer(BotContext context, ChosenInlineResult result) {
        var userId = result.from.id;
        if (result.result_id == "null") {
            context.sendMessage(userId, "Отмена создания");
            context.setState(userId, "");
            return;
        }
        context.getStateData(userId).put("requesterId", result.result_id);
        context.setState(userId, "PieceAction-amount");
        context.sendMessage(userId, "Какое количество деталей выдать?").exec();
    }
}
