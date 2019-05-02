package com.settingdust.dustmultirespawn.module.spawn;

import com.google.common.collect.Maps;
import lombok.Data;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.world.Location;

import java.util.Map;

@ConfigSerializable
@Data
public class SpawnsEntity {
    @Setting
    private Map<String, SpawnNode> locations = Maps.newHashMap();
}
