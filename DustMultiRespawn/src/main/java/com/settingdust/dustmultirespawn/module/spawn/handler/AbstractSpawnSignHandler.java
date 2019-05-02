package com.settingdust.dustmultirespawn.module.spawn.handler;

import com.settingdust.dustmultirespawn.DustMultiRespawn;
import com.settingdust.dustmultirespawn.module.spawn.SpawnNode;
import com.settingdust.dustmultirespawn.module.spawn.SpawnsProvider;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;

import java.util.List;
import java.util.Map;

public abstract class AbstractSpawnSignHandler {
    private DustMultiRespawn plugin = DustMultiRespawn.getInstance();
    ConfigurationNode locale = plugin.getLocale();
    SpawnsProvider spawnsProvider;
    private String type;

    AbstractSpawnSignHandler(String type, SpawnsProvider spawnsProvider) {
        this.type = type;
        this.spawnsProvider = spawnsProvider;
    }

    @Listener
    public void onPlace(ChangeBlockEvent.Place event) {
        if (event.getSource() instanceof Player) {
            Player player = (Player) event.getSource();
            if (player.hasPermission("dust.spawn.add")) {
                List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
                for (Transaction<BlockSnapshot> transaction : transactions) {
                    BlockSnapshot blockSnapshot = transaction.getFinal();
                    BlockState blockState = blockSnapshot.getState();
                    BlockType blockType = blockState.getType();
                    if (blockType.getName().equalsIgnoreCase(type)) {
                        this.placeHandler(blockSnapshot, player);
                    }
                }
            }
        }
    }

    @Listener
    public void onBreak(ChangeBlockEvent.Break event) {
        if (event.getSource() instanceof Player) {
            Player player = (Player) event.getSource();
            List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
            for (Transaction<BlockSnapshot> transaction : transactions) {
                BlockSnapshot blockSnapshot = transaction.getOriginal();
                BlockState blockState = blockSnapshot.getState();
                BlockType blockType = blockState.getType();
                if (blockType.getName().equalsIgnoreCase(type)) {
                    this.breakHandler(blockSnapshot, player);
                }
            }
        }
    }

    protected void breakHandler(BlockSnapshot blockSnapshot, Player player) {
        Map<String, SpawnNode> locations = spawnsProvider.getLocations();
        for (String key : locations.keySet()) {
            Location location = locations.get(key).location;
            if (location.getPosition().distance(blockSnapshot.getLocation().get().getPosition()) < 1) {
                locations.remove(key);
                if (player.hasPermission("dust.spawn.remove")) {
                    player.sendMessage(Text.builder()
                            .color(TextColors.GREEN)
                            .append(Text.of(locale
                                    .getNode("operation")
                                    .getNode("remove")
                                    .getNode("success").getString().replaceAll("%name%", key))
                            ).build()
                    );
                } else {
                    Sponge.getServer().getBroadcastChannel().send(Text.builder()
                            .color(TextColors.RED)
                            .append(Text.of(locale
                                    .getNode("operation")
                                    .getNode("remove")
                                    .getNode("success").getString().replaceAll("%name%", key))
                            ).build());
                }
                break;
            }
        }
    }

    protected abstract void placeHandler(BlockSnapshot blockSnapshot, Player player);
}
