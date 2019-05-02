package com.settingdust.dustmultirespawn.module.main.entity;

import lombok.Data;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@Data
@ConfigSerializable
public class PermissionEntity {
    @Setting(comment = "Whether to require the \"dust.spawn.spawns.name\" permission for each spawn location, where \"name\" is the name of the spawn")
    private boolean perSpawnPerms = false;
}
