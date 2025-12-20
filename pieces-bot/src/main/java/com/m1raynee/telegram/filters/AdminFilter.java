package com.m1raynee.telegram.filters;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.natanimn.telebof.filters.CustomFilter;
import io.github.natanimn.telebof.types.updates.Update;

public class AdminFilter implements CustomFilter {

    static Set<Long> adminIds;
    static {
        var adminEnv = System.getenv("PIECES_BOT_ADMINS");
        adminIds = (adminEnv == null || adminEnv.isEmpty())
                ? new HashSet<>()
                : Stream.of(adminEnv.split(";"))
                        .map(s -> (s.isEmpty()) ? null : Long.valueOf(s))
                        .collect(Collectors.toCollection(HashSet::new));
    }

    public static boolean isAdmin(Long id) {
        return adminIds.contains(id);
    }

    public boolean check(Update update) {
        return update.message != null && isAdmin(update.message.sender_chat.id);
    }
}
