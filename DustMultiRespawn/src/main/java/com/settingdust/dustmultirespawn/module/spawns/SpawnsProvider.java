package com.settingdust.dustmultirespawn.module.spawns;

import com.google.common.reflect.TypeToken;
import com.settingdust.dustcore.api.ConfigProvider;
import com.settingdust.dustmultirespawn.DustMultiRespawn;
import com.settingdust.dustmultirespawn.module.ProviderManager;
import com.settingdust.dustmultirespawn.module.main.MainProvider;
import com.settingdust.dustmultirespawn.module.spawns.handler.SpawnSignHandler;
import com.settingdust.dustmultirespawn.module.spawns.handler.SpawnWaystoneHandler;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.service.NucleusWarpService;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class SpawnsProvider extends ConfigProvider<SpawnsObject> {
    private DustMultiRespawn plugin = DustMultiRespawn.getInstance();
    private ConfigurationNode locale = plugin.getLocale();
    private boolean isNucleusLoaded = plugin.isNucleusLoaded();
    private ProviderManager providerManager;
    private MainProvider mainProvider;
    private boolean isSyncWarp;
    private NucleusWarpService warpService;
    private Map<String, Location> spawns;

    public SpawnsProvider(ProviderManager providerManager) {
        super(new SpawnsConfig(), new SpawnsObject());
        if (isNucleusLoaded && isSyncWarp) {
            warpService = NucleusAPI.getWarpService().get();
        }

        spawns = entity.getLocations();
        this.providerManager = providerManager;
        this.mainProvider = providerManager.getMainProvider();
        this.isSyncWarp = mainProvider.isSyncWarp();

        Sponge.getEventManager().registerListeners(plugin, new SpawnSignHandler(mainProvider, this));
        Sponge.getEventManager().registerListeners(plugin, new SpawnWaystoneHandler(mainProvider, this));
    }

    @Override
    public void load() {
        this.config = new SpawnsConfig();
        try {
            if (Objects.isNull(config.getRoot().getValue())) {
                this.entity = new SpawnsObject();
            } else {
                this.entity = config.getRoot().getValue(TypeToken.of(SpawnsObject.class));
            }
            this.config.save(this.entity);
        } catch (ObjectMappingException | IOException e) {
            e.printStackTrace();
        }
        new SpawnsCommand(this);
    }

    public Location<World> getSpawnLocation(Location location) {
        Location<World> spawnLocation = ((World) location.getExtent()).getSpawnLocation();
        double distance = -1D;
        for (String key : spawns.keySet()) {
            Location current = spawns.get(key);
            double tmpDistance = current.getPosition().distance(location.getPosition());
            if (distance == -1 || tmpDistance < distance) {
                distance = tmpDistance;
                spawnLocation = current;
            }
        }
        return spawnLocation;
    }

    public void add(String name, Location<World> location) {
        spawns.put(name, location);
        if (isNucleusLoaded && isSyncWarp) {
            warpService.setWarp(name, location, location.getPosition());
            warpService.setWarpCategory(name, locale
                    .getNode("warp")
                    .getNode("category").getString());
        }
    }

    public boolean remove(String name) {
        if (spawns.containsKey(name)) {
            spawns.remove(name);
            if (isNucleusLoaded && isSyncWarp) {
                warpService.removeWarp(name);
            }
            return true;
        } else {
            return false;
        }
    }

    public Map<String, Location> getLocations() {
        return entity.getLocations();
    }
}