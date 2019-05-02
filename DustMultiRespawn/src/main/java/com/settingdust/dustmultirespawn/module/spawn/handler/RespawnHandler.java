package com.settingdust.dustmultirespawn.module.spawn.handler;

import com.settingdust.dustmultirespawn.module.spawn.SpawnNode;
import com.settingdust.dustmultirespawn.module.spawn.SpawnsProvider;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.world.World;

public class RespawnHandler {
    private SpawnsProvider spawnsProvider;

    public RespawnHandler(SpawnsProvider spawnsProvider) {
        this.spawnsProvider = spawnsProvider;
    }

    @Listener
    public void onRespawn(RespawnPlayerEvent event) {
        SpawnNode spawnNode = spawnsProvider.getSpawnLocation(event.getFromTransform().getLocation(), event.getOriginalPlayer());
        event.setToTransform(new Transform<>((World) spawnNode.location.getExtent(), spawnNode.location.getPosition(), spawnNode.rotation));
    }
}
