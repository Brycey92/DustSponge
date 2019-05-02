package com.settingdust.dustmultirespawn.module.spawn;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.world.Location;

public class SpawnNode {
    public Location location;
    public Vector3d rotation;

    public SpawnNode(Location location, Vector3d rotation) {
        this.location = location;
        this.rotation = rotation;
    }
}
