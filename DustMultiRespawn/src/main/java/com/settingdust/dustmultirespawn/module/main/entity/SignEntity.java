package com.settingdust.dustmultirespawn.module.main.entity;

import lombok.Data;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@Data
@ConfigSerializable
public class SignEntity {
    @Setting(comment = "Whether to sync spawn when the block below is placed/broken")
    private boolean enable = false;
    @Setting(comment = "The block to sync when placed/broken")
    private String type = "minecraft:torch";
}