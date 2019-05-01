package com.settingdust.forcelocale.module.main;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.Setter;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Map;

@Data
@ConfigSerializable
public class MainEntity {
    @Setter
    private Map<String, String> locales = Maps.newHashMap();
}
