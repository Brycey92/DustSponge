package com.settingdust.dustcore.locale;

import com.google.common.collect.Maps;
import com.settingdust.dustcore.DustCore;
import com.settingdust.dustcore.api.DLocale;
import com.settingdust.dustcore.utils.PlaceholderUtils;
import com.settingdust.dustcore.utils.PluginUtils;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Locale;
import java.util.Map;

public class LocaleManager {
    private Map<String, DLocale> locales;
    @Getter
    private static DLocale coreLocale;

    public LocaleManager(Locale locale) {
        this.load(locale);
    }

    public void load(Locale locale) {
        coreLocale = new DLocale(locale, DustCore.getInstance().getPluginContainer());

        locales = Maps.newHashMap();
        locales.put(DustCore.ID, coreLocale);

        for (PluginContainer plugin : Sponge.getPluginManager().getPlugins()) {
            if (PluginUtils.isDepend(plugin, DustCore.ID)) {
                DustCore.getInstance().getLogger().info(
                        PlaceholderUtils.forPluginInfo(coreLocale
                                .getRoot()
                                .getNode("plugin")
                                .getNode("currentPlugin")
                                .getString(), plugin)
                );
                locales.put(plugin.getId(), new DLocale(locale, plugin));
            }
        }

        DustCore.getInstance().getLogger().info(
                PlaceholderUtils.forLanguage(coreLocale
                        .getRoot()
                        .getNode("plugin")
                        .getNode("currentLanguage")
                        .getString(), locale)
        );
    }

    public DLocale getLocale(PluginContainer pluginContainer) {
        return getLocale(pluginContainer.getId());
    }

    public DLocale getLocale(String id) {
        return locales.get(id);
    }

    @Listener(order = Order.LATE)
    public void onReload(GameReloadEvent event) {
        this.load(DustCore.getInstance().getProviderManager().getMainProvider().getLocale());
    }
}
