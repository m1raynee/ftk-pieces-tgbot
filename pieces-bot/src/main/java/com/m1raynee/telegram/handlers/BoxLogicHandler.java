package com.m1raynee.telegram.handlers;

import java.util.List;
import java.util.regex.Pattern;

import com.m1raynee.db.Database;
import com.m1raynee.db.entity.Box;
import com.m1raynee.db.entity.Piece;
import com.m1raynee.telegram.filters.AdminFilter;
import com.m1raynee.telegram.scenarios.InlineSelectorScenario;
import com.m1raynee.telegram.utils.FilterUtils;
import com.m1raynee.telegram.utils.KeyboardUtil;
import com.m1raynee.telegram.utils.ReflectedUtil;

import io.github.natanimn.telebof.BotClient;
import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.annotations.CallbackHandler;
import io.github.natanimn.telebof.annotations.MessageHandler;
import io.github.natanimn.telebof.enums.ParseMode;
import io.github.natanimn.telebof.filters.CustomFilter;
import io.github.natanimn.telebof.filters.Filter;
import io.github.natanimn.telebof.requests.send.SendMessage;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardButton;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardMarkup;
import io.github.natanimn.telebof.types.updates.CallbackQuery;
import io.github.natanimn.telebof.types.updates.ChosenInlineResult;
import io.github.natanimn.telebof.types.updates.Message;
import io.github.natanimn.telebof.types.updates.Update;

public class BoxLogicHandler {
    static Pattern patternStarBox = Pattern.compile("/start box(\\d+)");
    static Pattern patternPieceI = Pattern.compile("lookupBox-(\\w+)-i");

    public boolean stateShowbox(String state) {
        return state == "showbox-selection";
    }

    public BoxLogicHandler(BotClient bot) {
        bot.onInline(
                FilterUtils.filterInlineState(this::stateShowbox),
                InlineSelectorScenario::searchBox);
        bot.onChosenInlineResult(
                FilterUtils.filterInlineResultState(this::stateShowbox),
                BoxLogicHandler::inlineBoxAnswer);

        bot.onMessage(filter -> {
            var ref = new ReflectedUtil<Filter>(filter);
            return ref.getStateName(ref.getUpdate().message.from.id) != null
                    && patternPieceI.matcher(ref.getStateName(ref.getUpdate().message.from.id)).find();
        }, BoxLogicHandler::lookupBoxPieceI);
    }

    public static class BoxFilter implements CustomFilter {
        @Override
        public boolean check(Update update) {
            return patternStarBox.matcher(update.message.text).find();
        }
    }

    public static String formBoxPreview(Box box) {
        String text = "<b>🧰 %s</b>".formatted(box.toString());
        if (box.getPlaceCode() == null) {
            text += " (конструктор)\n";
        } else {
            text += "\n<b>🧩 Детали в коробке:</b>\n";
            List<Piece> pieces = box.getPieces();
            for (int i = 0; i < pieces.size(); i++) {
                var piece = pieces.get(i);
                text += "%d. %s (x%d)\n".formatted(i + 1,
                        piece.getName(), piece.getCalculatedAmount());
            }
            if (pieces.size() == 0) {
                text += "<i>В коробке нет деталей</i>";
            }
        }
        return text;
    }

    public static SendMessage formDefalutBoxSendMessage(BotContext context, long dest, Box box) {
        return context.sendMessage(dest, formBoxPreview(box))
                .parseMode(ParseMode.HTML)
                .replyMarkup(KeyboardUtil.boxPreviewKeyboard(AdminFilter.isAdmin(dest)));
    }

    @MessageHandler(filter = BoxFilter.class)
    public static void startBoxCommand(BotContext context, Message message) {
        var matcher = patternStarBox.matcher(message.text);
        Integer box_index;
        if (matcher.find()) {
            box_index = Integer.valueOf(matcher.group(1));
        } else {
            context.sendMessage(message.chat.id, "Не найден номер коробки").exec();
            return;
        }

        var newMessage = formDefalutBoxSendMessage(context, message.chat.id, Database.fetchBoxPreview(box_index))
                .exec();
        context.getStateData(message.from.id).put("lookupMessage", newMessage);
    }

    @MessageHandler(commands = "showbox")
    public static void showBoxCommand(BotContext context, Message message) {
        context.setState(message.chat.id, "showbox-selection");
        context.sendMessage(message.chat.id, "Чтобы выбрать <b>коробку</b> из базы, перейдите в инлайн режим")
                .parseMode(ParseMode.HTML)
                .replyMarkup(KeyboardUtil.moveToInlineSingle())
                .exec();
    }

    public static void inlineBoxAnswer(BotContext context, ChosenInlineResult result) {
        context.setState(result.from.id, "");
        if (result.result_id.contains("null")) {
            context.sendMessage(result.from.id, "Отмена поиска").exec();
            return;
        }
        Box box = Database.fetchBoxPreview(Integer.valueOf(result.result_id));
        context.getStateData(result.from.id).put("lookupBox", box);

        if (box == null) {
            context.sendMessage(result.from.id, "Коробки не существует...").exec();
            return;
        }
        Message message = formDefalutBoxSendMessage(context, result.from.id, box).exec();
        context.getStateData(result.from.id).put("lookupMessage", message);

    }

    private static void updateKeyboardWithError(BotContext context, Message botMessage, String errorMessage,
            String inputText) {
        String shortenedText = inputText.length() > 2 ? inputText.substring(0, 2) + "…" : inputText;

        var keyboard = KeyboardUtil.boxPreviewKeyboard(AdminFilter.isAdmin(botMessage.chat.id));

        keyboard.inline_keyboard.get(0).get(0).text = "🧩 Информация о детали (%s – %s)".formatted(shortenedText,
                errorMessage);

        context.editMessageReplyMarkup(
                botMessage.chat.id,
                botMessage.message_id)
                .replyMarkup(keyboard)
                .exec();
    }

    public static void lookupBoxPieceI(BotContext context, Message message) {
        context.deleteMessage(message.chat.id, message.message_id).exec();

        var data = context.getStateData(message.chat.id);
        var botMessage = (Message) data.get("lookupMessage");
        var inputText = message.text;

        if (!Pattern.compile("\\d+").matcher(inputText).matches()) {
            updateKeyboardWithError(context, botMessage, "не число", inputText);
            return;
        }

        int i;
        try {
            i = Integer.parseInt(inputText) - 1;
        } catch (NumberFormatException e) {
            // На случай, если Pattern пропустил слишком большое число для Integer
            updateKeyboardWithError(context, botMessage, "слишком большое число", inputText);
            return;
        }

        var box = (Box) data.get("lookupBox");

        if (i < 0 || i >= box.getPieces().size()) {
            updateKeyboardWithError(context, botMessage, "неверный индекс", inputText);
            return;
        }

        var piece = box.getPieces().get(i);
        data.put("lookupPiece", piece);

        var ref = new ReflectedUtil<BotContext>(context);
        var matcher = patternPieceI.matcher(ref.getStateName(message.from.id));
        matcher.find();
        var operation = matcher.group(1);

        switch (operation) {
            case "showPiece":
                context.setState(message.from.id, "");
                PieceLogicHandler.handlePreview(context, message.from.id);
                break;
            case "createAction":
                context.editMessageReplyMarkup(botMessage.chat.id, botMessage.message_id)
                        .replyMarkup(KeyboardUtil.boxPreviewKeyboard(AdminFilter.isAdmin(message.from.id)))
                        .exec();
                context.setState(message.from.id, "performer");
                context.sendMessage(botMessage.chat.id,
                        "Чтобы выбрать, кому выдать деталь, передйтите в инлайн режим\n" +
                                "<b>Деталь:</b> %s".formatted(piece.getName()))
                        .parseMode(ParseMode.HTML)
                        .replyMarkup(KeyboardUtil.moveToInlineSingle())
                        .exec();
                break;
            case "finalizeAction":
                // TODO

                break;

            default:
                break;
        }

    }

    @CallbackHandler(regex = "lookupBox-")
    public static void lookupBoxButton(BotContext context, CallbackQuery callback) {
        var data = context.getStateData(callback.from.id);
        if (data == null
                || !data.containsKey("lookupBox")
                || !data.containsKey("lookupMessage")) {
            context.answerCallbackQuery(callback.id,
                    "Состояние устарело, начните заново")
                    .showAlert(true)
                    .exec();
            return;
        }
        var botMessage = (Message) data.get("lookupMessage");

        if (!botMessage.message_id.equals(callback.message.message_id)) {
            context.answerCallbackQuery(callback.id,
                    "Эта клавиатура уже неактивна")
                    .showAlert(true)
                    .exec();
            return;
        }

        context.answerCallbackQuery(callback.id).exec();

        var actions = callback.data.split("-");
        if (actions[1].equals("back")) {
            context.setState(callback.from.id, "");
            context.editMessageReplyMarkup(callback.message.chat.id, callback.message.message_id)
                    .replyMarkup(KeyboardUtil.boxPreviewKeyboard(AdminFilter.isAdmin(callback.from.id)))
                    .exec();
            return;
        }
        if (actions[1].equals("showPiece") || actions[1].equals("createAction")
                || actions[1].equals("finalizeAction")) {
            if (!actions[1].equals("showPiece")) {
                if (!data.containsKey("activeStudent")) {
                    context.sendMessage(callback.from.id, "Сначала выберите исполнителя: /setactive").exec();
                    return;
                }
            }
            context.setState(callback.from.id, "lookupBox-%s-i".formatted(actions[1]));
            context.editMessageReplyMarkup(callback.message.chat.id, callback.message.message_id)
                    .replyMarkup(new InlineKeyboardMarkup(
                            new InlineKeyboardButton[] { new InlineKeyboardButton(
                                    "↩️ Введите число или вернитесь назад", "lookupBox-back") }))
                    .exec();
            return;
        }

    }

}
