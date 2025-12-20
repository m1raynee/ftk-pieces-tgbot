package com.m1raynee.telegram;

import com.m1raynee.db.HibernateConfiguration;
import com.m1raynee.telegram.handlers.BoxLogicHandler;
import com.m1raynee.telegram.handlers.CreationLogicHandler;
import com.m1raynee.telegram.handlers.StartLogicHandler;
import com.m1raynee.telegram.handlers.StudentLogicHandler;

import io.github.natanimn.telebof.BotClient;

public class Main {
    static final String botToken = System.getenv("PIECES_BOT_TOKEN");

    public static void main(String[] args) {
        var bot = new BotClient(botToken);

        bot.context.setState(0, "operating");

        HibernateConfiguration.getSessionFactory();

        bot.addHandler(new Main());
        bot.addHandler(new StudentLogicHandler(bot));
        bot.addHandler(new CreationLogicHandler(bot));
        bot.addHandler(new BoxLogicHandler(bot));
        bot.addHandler(new StartLogicHandler(bot));

        System.out.println("Starting polling...");

        bot.startPolling();
    }
}
