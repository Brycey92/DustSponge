package com.settingdust.forcelocale.module;

import com.settingdust.dustcore.api.AbstractProviderManager;
import com.settingdust.forcelocale.module.main.MainProvider;
import lombok.Getter;

public class ProviderManager extends AbstractProviderManager {
    @Getter
    private MainProvider mainProvider;

    @Override
    protected void load() {
        this.mainProvider = new MainProvider();

        this.providers.add(mainProvider);
    }
}
