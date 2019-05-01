package com.settingdust.forcelocale;

import com.google.inject.Inject;
import com.settingdust.dustcore.DustCore;
import com.settingdust.dustcore.api.DLocale;
import com.settingdust.forcelocale.module.ProviderManager;
import lombok.Getter;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import java.nio.file.Path;

@Plugin(
        id = ForceLocale.ID,
        name = "ForceLocale",
        version = "1.0",
        description = "Modify the message of server",
        authors = {
                "SettingDust"
        },
        dependencies = {
                @Dependency(id = "dustcore")
        }
)
public class ForceLocale {
    public static final String ID = "forcelocale";

    @Getter
    @Inject
    private Logger logger;

    @Inject
    @Getter
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    @Getter
    private static ForceLocale instance;

    private DLocale locale;

    @Getter
    private ProviderManager providerManager;

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        instance = this;
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        locale = DustCore.getInstance().getLocaleManager().getLocale(ID);
    }

    @Listener
    public void onStarting(GameStartingServerEvent event) {
        providerManager = new ProviderManager();

        Sponge.getEventManager().registerListeners(this, providerManager);
    }
}
