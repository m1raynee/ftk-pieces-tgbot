package com.m1raynee.telegram.utils;

import io.github.natanimn.telebof.enums.ParseMode;
import io.github.natanimn.telebof.types.inline.InlineQueryResultArticle;
import io.github.natanimn.telebof.types.input.InputTextMessageContent;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardButton;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardMarkup;

public final class KeyboardUtil {
    public static InlineQueryResultArticle emptyQueryResult(String title, String text) {
        return new InlineQueryResultArticle("null", title,
                new InputTextMessageContent("<i>" + text + "</i>").parseMode(ParseMode.HTML));
    }

    public static InlineKeyboardButton moveToInline() {
        return moveToInline("Перейти в инлайн режим", "");
    }

    public static InlineKeyboardButton moveToInline(String text, String query) {
        return new InlineKeyboardButton(text)
                .switchInlineQueryCurrentChat(query);
    }

    public static InlineKeyboardMarkup moveToInlineSingle() {
        return new InlineKeyboardMarkup(new InlineKeyboardButton[] { KeyboardUtil.moveToInline() });
    }

    public static InlineKeyboardMarkup moveToInlineSingle(String text, String query) {
        return new InlineKeyboardMarkup(new InlineKeyboardButton[] { KeyboardUtil.moveToInline(text, query) });
    }

}
