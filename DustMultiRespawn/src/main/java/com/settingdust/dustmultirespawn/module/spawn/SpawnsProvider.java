package com.settingdust.dustmultirespawn.module.spawn;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import com.settingdust.dustcore.api.ConfigProvider;
import com.settingdust.dustmultirespawn.DustMultiRespawn;
import com.settingdust.dustmultirespawn.module.ProviderManager;
import com.settingdust.dustmultirespawn.module.main.MainProvider;
import com.settingdust.dustmultirespawn.module.spawn.handler.RespawnHandler;
import com.settingdust.dustmultirespawn.module.spawn.handler.SpawnSignHandler;
import com.settingdust.dustmultirespawn.module.spawn.handler.SpawnWaystoneHandler;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.service.NucleusWarpService;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class SpawnsProvider extends ConfigProvider<SpawnsEntity> {
    private DustMultiRespawn plugin = DustMultiRespawn.getInstance();
    private ConfigurationNode locale = plugin.getLocale();
    private boolean isSyncWarp;
    private NucleusWarpService warpService;
    private boolean perSpawnPerms;
    private Map<String, SpawnNode> spawns;
    private static SpawnsProvider instance;

    public SpawnsProvider(ProviderManager providerManager) {
        super(new SpawnsConfig(), new SpawnsEntity());

        instance = this;

        spawns = entity.getLocations();
        MainProvider mainProvider = providerManager.getMainProvider();
        this.isSyncWarp = mainProvider.isSyncWarp();
        if (isSyncWarp) {
            warpService = NucleusAPI.getWarpService().get();
        }
        this.perSpawnPerms = mainProvider.getPerms().isPerSpawnPerms();

        new SpawnsCommand(this);

        Sponge.getEventManager().registerListeners(plugin, new SpawnSignHandler(mainProvider, this));
        Sponge.getEventManager().registerListeners(plugin, new SpawnWaystoneHandler(mainProvider, this));
        Sponge.getEventManager().registerListeners(plugin, new RespawnHandler(this));
    }

    @Override
    public void load() {
        this.config = new SpawnsConfig();
        try {
            if (Objects.isNull(config.getRoot().getValue())) {
                this.entity = new SpawnsEntity();
            } else {
                this.entity = config.getRoot().getValue(TypeToken.of(SpawnsEntity.class));
            }
            this.config.save(this.entity);
        } catch (ObjectMappingException | IOException e) {
            e.printStackTrace();
        }
    }

    public SpawnNode getSpawnLocation(Location location, Player player) {
        Location<World> spawnLocation = ((World) location.getExtent()).getSpawnLocation();
        Vector3d spawnRotation = null;
        double distance = -1D;
        for (String key : spawns.keySet()) {
            Location<World> current = spawns.get(key).location;
            double tmpDistance = current.getPosition().distance(location.getPosition());
            if (canPlayerUseSpawn(player, key) && (distance == -1 || tmpDistance < distance)) {
                distance = tmpDistance;
                spawnLocation = current;
                spawnRotation = spawns.get(key).rotation;
            }
        }

        return new SpawnNode(spawnLocation, spawnRotation == null ? player.getRotation() : spawnRotation);
    }

    public void add(String name, Location<World> location, @Nullable Vector3d rotation) {
        spawns.put(name, new SpawnNode(location, rotation));
        if (isSyncWarp) {
            warpService.setWarp(name, location, location.getPosition());
            warpService.setWarpCategory(name, locale
                    .getNode("warp")
                    .getNode("category").getString());
        }
    }

    public boolean remove(String name) {
        if (spawns.containsKey(name)) {
            spawns.remove(name);
            if (isSyncWarp) {
                warpService.removeWarp(name);
            }
            return true;
        } else {
            return false;
        }
    }

    public static boolean canPlayerUseSpawn(Player player, String name) {
        return !instance.perSpawnPerms || player.hasPermission("dust.spawn.spawns." + name);
    }

    public Map<String, SpawnNode> getLocations() {
        return entity.getLocations();
    }
}
