package com.settingdust.forcelocale.module.main;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.message.MessageChannelEvent;

public class MessageHandler {
    private MainProvider mainProvider;

    public MessageHandler(MainProvider mainProvider) {
        this.mainProvider = mainProvider;
    }

    @Listener
    public void onMessage(MessageChannelEvent event) {
        event.getMessage();
    }
}
