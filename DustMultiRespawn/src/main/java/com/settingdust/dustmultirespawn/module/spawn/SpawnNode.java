package com.settingdust.dustmultirespawn.module.spawn;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class SpawnNode {
    public Location<World> location;
    public Vector3d rotation;

    public SpawnNode(Location<World> location, Vector3d rotation) {
        this.location = location;
        this.rotation = rotation;
    }
}
