package com.settingdust.dustmultirespawn.module.spawn.handler;

import com.google.common.collect.Maps;
import com.settingdust.dustmultirespawn.module.main.MainProvider;
import com.settingdust.dustmultirespawn.module.spawn.SpawnsProvider;
import com.settingdust.dustmultirespawn.module.spawn.handler.AbstractSpawnSignHandler;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpawnWaystoneHandler extends AbstractSpawnSignHandler {
    private Map<UUID, BlockSnapshot> waitingForName = Maps.newHashMap();
    private MainProvider mainProvider;

    public SpawnWaystoneHandler(MainProvider mainProvider, SpawnsProvider spawnsProvider) {
        super("waystones:waystone", spawnsProvider);
        this.mainProvider = mainProvider;
    }

    @Listener
    public void onMove(MoveEntityEvent event) {
        if (event.getSource() instanceof Player) {
            Player player = (Player) event.getSource();
            UUID uuid = player.getUniqueId();
            if (waitingForName.containsKey(uuid)) {
                BlockSnapshot blockSnapshot = waitingForName.get(uuid);
                BlockState blockState = blockSnapshot.getState();
                boolean isBase = (Boolean) blockState.getTraitValue(blockState.getTrait("base").get()).get();
                TileEntity tileEntity = blockSnapshot.getLocation().get().getTileEntity().get();
                if (tileEntity.isValid()) {
                    String unsafeData = tileEntity.toContainer().getString(DataQuery.of("UnsafeData")).get();
                    Pattern pattern = Pattern.compile("WaystoneName=(.*?),");
                    Matcher matcher = pattern.matcher(unsafeData);
                    if (matcher.find()) {
                        String wayStoneName = matcher.group(1);
                        if (!wayStoneName.equalsIgnoreCase("")) {
                            spawnsProvider.add(wayStoneName, blockSnapshot.getLocation().get(), null);
                            waitingForName.remove(uuid);
                            player.sendMessage(Text.builder()
                                    .color(TextColors.GREEN)
                                    .append(Text.of(locale
                                            .getNode("operation")
                                            .getNode("add")
                                            .getNode("success").getString())
                                    ).build()
                            );
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void placeHandler(BlockSnapshot blockSnapshot, Player player) {
        if (mainProvider.get().getSync().isWaystone()) {
            BlockState blockState = blockSnapshot.getState();
            boolean isBase = (Boolean) blockState.getTraitValue(blockState.getTrait("base").get()).get();
            if (isBase) {
                waitingForName.put(player.getUniqueId(), blockSnapshot);
            }
        }
    }

    @Override
    protected void breakHandler(BlockSnapshot blockSnapshot, Player player) {
        BlockState blockState = blockSnapshot.getState();
        boolean isBase = (Boolean) blockState.getTraitValue(blockState.getTrait("base").get()).get();
        if (isBase) {
            super.breakHandler(blockSnapshot, player);
        }
    }
}
