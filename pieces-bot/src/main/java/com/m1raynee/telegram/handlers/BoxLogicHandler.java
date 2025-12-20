package com.m1raynee.telegram.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.m1raynee.db.HibernateConfiguration;
import com.m1raynee.db.dto.PieceAmountDTO;
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
        var sessionFactory = HibernateConfiguration.getSessionFactory();
        Box box = sessionFactory.fromSession(session -> {
            List<PieceAmountDTO> amounts = session.createSelectionQuery("""
                    select new com.m1raynee.db.dto.PieceAmountDTO(p.id, SUM(pa.amount))
                    from Piece p
                    left join p.actions pa
                    join p.box b
                    where b.index=:index
                    group by p.id
                    """,
                    PieceAmountDTO.class)
                    .setParameter("index", box_index)
                    .getResultList();

            Map<Long, Long> stockMap = amounts.stream()
                    .collect(Collectors.toMap(PieceAmountDTO::getPieceId,
                            dto -> dto.getCurrentAmount() != null ? dto.getCurrentAmount() : 0L));

            Box resultBox = session
                    .createSelectionQuery("from Box b left join fetch b.pieces where b.index=:index", Box.class)
                    .setParameter("index", box_index).getSingleResultOrNull();

            for (Piece piece : resultBox.getPieces()) {
                piece.setCalculatedAmount(stockMap.get(piece.getId()));
            }
            return resultBox;

        });

        System.out.println("Fetch complete");

        sendBoxInformation(ctx, msg.chat.id, box);
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
