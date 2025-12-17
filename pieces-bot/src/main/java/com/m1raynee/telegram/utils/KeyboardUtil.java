package com.m1raynee.telegram.utils;

import io.github.natanimn.telebof.enums.ParseMode;
import io.github.natanimn.telebof.types.inline.InlineQueryResultArticle;
import io.github.natanimn.telebof.types.input.InputTextMessageContent;

public final class KeyboardUtil {
    public static InlineQueryResultArticle emptyQueryResult(String title, String text) {
        return new InlineQueryResultArticle("null", title,
                new InputTextMessageContent("<i>" + text + "</i>").parseMode(ParseMode.HTML));
    }
}
