package com.settingdust.dustmultirespawn.module.spawn.handler;

import com.google.common.collect.Maps;
import com.settingdust.dustmultirespawn.module.main.MainProvider;
import com.settingdust.dustmultirespawn.module.spawn.SpawnsProvider;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Map;
import java.util.UUID;

public class SpawnSignHandler extends AbstractSpawnSignHandler {
    private Map<UUID, Location<World>> waitingForName = Maps.newHashMap();
    private MainProvider mainProvider;

    public SpawnSignHandler(MainProvider mainProvider, SpawnsProvider spawnsProvider) {
        super(mainProvider
                        .getSync()
                        .getSign()
                        .getType()
                , spawnsProvider);
        this.mainProvider = mainProvider;
    }

    @Listener
    public void onChat(MessageChannelEvent.Chat event) {
        if (event.getSource() instanceof Player) {
            Player player = (Player) event.getSource();
            UUID uuid = player.getUniqueId();
            if (waitingForName.containsKey(uuid)) {
                spawnsProvider.add(event.getRawMessage().toPlain(), waitingForName.get(uuid), null);
                player.sendMessage(Text.builder()
                        .color(TextColors.GREEN)
                        .append(Text.of(locale
                                .getNode("operation")
                                .getNode("add")
                                .getNode("success").getString())
                        ).build()
                );
                event.setCancelled(true);
            }
        }
    }

    @Override
    protected void placeHandler(BlockSnapshot blockSnapshot, Player player) {
        if (mainProvider.get().getSync().getSign().isEnable()) {
            waitingForName.put(player.getUniqueId(), blockSnapshot.getLocation().get());
            player.sendMessage(Text.builder()
                    .color(TextColors.GREEN)
                    .append(Text.of(locale
                            .getNode("operation")
                            .getNode("add")
                            .getNode("enterName").getString())
                    ).build()
            );
        }
    }

    @Override
    protected void breakHandler(BlockSnapshot blockSnapshot, Player player) {
        if (mainProvider.get().getSync().getSign().isEnable()) {
            super.breakHandler(blockSnapshot, player);
        }
    }
}
