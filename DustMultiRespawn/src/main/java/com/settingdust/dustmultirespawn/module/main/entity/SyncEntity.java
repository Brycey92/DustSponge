package com.settingdust.dustmultirespawn.module.main.entity;

import lombok.Data;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@Data
@ConfigSerializable
public class SyncEntity {
    @Setting(comment = "Whether sync add/remove operation with Nucleus warp")
    private boolean warp = true;
    @Setting(comment = "Sign")
    private SignEntity sign = new SignEntity();
    @Setting(comment = "Sync with waystone(https://minecraft.curseforge.com/projects/waystones)")
    private boolean waystone = true;
}