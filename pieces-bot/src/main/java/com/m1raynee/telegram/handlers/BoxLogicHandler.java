package com.m1raynee.telegram.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import com.m1raynee.db.Database;
import com.m1raynee.db.entity.Box;
import com.m1raynee.db.entity.Piece;

import io.github.natanimn.telebof.BotClient;
import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.annotations.MessageHandler;
import io.github.natanimn.telebof.filters.CustomFilter;
import io.github.natanimn.telebof.types.updates.Message;
import io.github.natanimn.telebof.types.updates.Update;

public class BoxLogicHandler {
    static Pattern patternStarBox = Pattern.compile("/start box(\\d+)");
    @SuppressWarnings("unused")
    private BotClient bot;

    public BoxLogicHandler(BotClient bot) {
        this.bot = bot;
    }

    public static class BoxFilter implements CustomFilter {
        @Override
        public boolean check(Update upd) {
            System.out.println(upd.message.text);
            return patternStarBox.matcher(upd.message.text).find();
        }
    }

    @MessageHandler(filter = BoxFilter.class)
    public void startBoxCommand(BotContext ctx, Message msg) {
        var matcher = patternStarBox.matcher(msg.text);
        Integer box_index;
        if (matcher.find()) {
            box_index = Integer.valueOf(matcher.group(1));
        } else {
            ctx.sendMessage(msg.chat.id, "Не найден номер коробки").exec();
            return;
        }
        sendBoxInformation(ctx, msg.chat.id, Database.fetchBoxPreview(box_index));
    }

    public void sendBoxInformation(BotContext ctx, Long chat_id, Box box) {
        if (box == null) {
            ctx.sendMessage(chat_id, "Коробки не существует...").exec();
            return;
        }

        String text = "<b>🧰 %s</b>".formatted(box.toString());
        if (box.getPlaceCode() == null) {
            text += " (конструктор)";
        } else {
            text += "\n<b>🧩 Детали в коробке:</b>\n";
            List<Piece> pieces = new ArrayList<>(box.getPieces());
            for (int i = 0; i < pieces.size(); i++) {
                var piece = pieces.get(i);
                text += piece.getName() + "(x%d)\n".formatted(piece.getCalculatedAmount());
            }
        }

        ctx.sendMessage(chat_id, text).exec();
    }

}
