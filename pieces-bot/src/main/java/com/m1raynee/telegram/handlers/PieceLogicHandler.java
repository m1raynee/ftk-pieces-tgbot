package com.m1raynee.telegram.handlers;

import com.m1raynee.db.Database;
import com.m1raynee.db.entity.Piece;
import com.m1raynee.db.enums.ActionState;
import com.m1raynee.telegram.filters.AdminFilter;
import com.m1raynee.telegram.scenarios.InlineSelectorScenario;
import com.m1raynee.telegram.utils.FilterUtils;
import com.m1raynee.telegram.utils.KeyboardUtil;
import com.m1raynee.telegram.utils.Pageable;

import io.github.natanimn.telebof.BotClient;
import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.annotations.MessageHandler;
import io.github.natanimn.telebof.enums.ParseMode;
import io.github.natanimn.telebof.types.updates.ChosenInlineResult;
import io.github.natanimn.telebof.types.updates.Message;

public class PieceLogicHandler {
    public PieceLogicHandler(BotClient bot) {
        bot.onInline(
                FilterUtils.filterInlineState(state -> state == "showpiece"),
                InlineSelectorScenario::searchPiece);
        bot.onChosenInlineResult(
                FilterUtils.filterInlineResultState(state -> state == "showpiece"),
                PieceLogicHandler::inlinePieceAnswer);
    }

    public static void inlinePieceAnswer(BotContext context, ChosenInlineResult result) {
        var userId = result.from.id;
        var page = new Pageable(0, 10);
        var isTeacher = AdminFilter.isAdmin(userId);

        context.setState(userId, "");
        if (result.result_id.contains("null")) {
            context.sendMessage(userId, "Отмена поиска").exec();
            return;
        }
        Piece piece = Database.fetchPieceActionsPaginated(Integer.valueOf(result.result_id), page);
        context.getStateData(userId).put("lookupPiece", piece);

        if (piece == null) {
            context.sendMessage(userId, "Детали не существует...").exec();
            return;
        }

        var message = context.sendMessage(userId, formPiecePreview(piece, page, isTeacher))
                .parseMode(ParseMode.HTML)
                .replyMarkup(KeyboardUtil.paginationPiceAction(page, piece, isTeacher))
                .exec();

        if (isTeacher)
            context.getStateData(userId).put("lookupMessagePage", page);
        context.getStateData(userId).put("lookupMessage", message);
    }

    public static String formPiecePreview(Piece piece, Pageable page, boolean isTeacher) {
        String text = """
                <b>🧩 %s %s</b>
                <i>(%s; %s)</i>
                <b>Количество:</b> +%d -%d (x%d)

                """.formatted(
                piece.getTagId(), piece.getName(),
                piece.getArticle(), piece.getAltName(),
                piece.getPositiveAmount(), piece.getNegativeAmount(), piece.getCalculatedAmount());

        if (isTeacher)
            return text;
        var pieceActions = piece.getTrActions();
        if (pieceActions != null) {
            for (int i = 0; i < pieceActions.size(); i++) {
                var pa = pieceActions.get(i);
                text += "%d. %s %s %s (%d)".formatted(
                        page.startI() + i + 1, pa.getRequester(),
                        switch (pa.getActionState()) {
                            case ActionState.INVENRATIZATION -> "🆕";
                            case ActionState.STORED -> "➡️";
                            case ActionState.TAKEN -> "⬅️";
                            case ActionState.LOST -> "❌";
                        }, pa.getPerformer(), pa.getAmount());
            }
        }
        return text;
    }

    public static void handlePreview(BotContext context, long userId) {
        var data = context.getStateData(userId);
        if (!data.containsKey("lookupPiece") || !data.containsKey("lookupMessage"))
            return;

        var message = (Message) data.get("lookupMessage");
        var piece = (Piece) data.get("lookupPiece");
        var isTeacher = AdminFilter.isAdmin(userId);
        var page = new Pageable(0, 10);

        var text = formPiecePreview(piece, page, isTeacher);
        var keyboard = KeyboardUtil.paginationPiceAction(page, piece, isTeacher);

        context.editMessageText(text, message.chat.id, message.message_id)
                .parseMode(ParseMode.HTML)
                .replyMarkup(keyboard)
                .exec();

    }

    @MessageHandler(commands = "showpiece")
    public static void showPieceCommand(BotContext context, Message message) {
        context.setState(message.from.id, "showpiece-name");
        context.sendMessage(message.chat.id, "Чтобы выбрать <b>деталь</b>, перейдите в инлайн режим")
                .parseMode(ParseMode.HTML)
                .replyMarkup(KeyboardUtil.moveToInlineSingle())
                .exec();
    }

}
