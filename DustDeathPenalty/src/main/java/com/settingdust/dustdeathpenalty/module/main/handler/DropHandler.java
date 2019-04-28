package com.settingdust.dustdeathpenalty.module.main.handler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.settingdust.dustdeathpenalty.module.main.MainProvider;
import com.settingdust.dustdeathpenalty.module.main.entity.ExpEntity;
import com.settingdust.dustdeathpenalty.module.main.entity.ItemEntity;
import com.settingdust.dustdeathpenalty.module.main.entity.MainEntity;
import com.settingdust.dustdeathpenalty.module.main.entity.WorldEntity;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.*;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.stream.Collectors;

public class DropHandler {

    private final MainEntity mainEntity;

    public DropHandler(MainProvider mainProvider) {
        this.mainEntity = mainProvider.get();
    }

    @Listener
    public void dropItemAndExp(DestructEntityEvent.Death event, @Getter("getTargetEntity") Living living) {
        if (living.getType().equals(EntityTypes.PLAYER)) {
            Player player = (Player) living;
            World world = player.getWorld();

            WorldEntity dropConfig = getDropConfig(player.getWorld().getName());
            ExpEntity expDropConfig = dropConfig.getExp();
            ItemEntity itemDropConfig = dropConfig.getItem();

            event.setKeepInventory(true);

            if (player.hasPermission("dust.death.drop.disable")) {
            }

            if (itemDropConfig.isEnable()) {
                spawnItems(dropItems(player, itemDropConfig), player.getLocation());
            }

            if (expDropConfig.isEnable()) {
                world.spawnEntity(createExperienceOrb(dropExperience(player, expDropConfig), player.getLocation()));
            }
        }
    }

    private Entity createDroppedEntity(EntityType type, Location<World> location) {
        return location.getExtent().createEntity(
                type,
                location.add(
                        Math.random() * 2,
                        Math.random() * 1.5,
                        Math.random() * 2
                ).getPosition()
        );
    }

    private WorldEntity getDropConfig(String worldName) {
        WorldEntity worldEntity;
        if (mainEntity.getWorld().containsKey(worldName)) {
            worldEntity = mainEntity.getWorld().get(worldName);
        } else {
            worldEntity = new WorldEntity(mainEntity.getItem(), mainEntity.getExp());
        }
        return worldEntity;
    }

    private int dropExperience(Player player, ExpEntity expDropConfig) {
        final int[] exp = {0};
        player.get(Keys.TOTAL_EXPERIENCE).ifPresent(integer -> {
            exp[0] = (int) (integer * expDropConfig.getRate());
            player.offer(Keys.TOTAL_EXPERIENCE, integer - exp[0]);
        });
        return exp[0];
    }

    private ExperienceOrb createExperienceOrb(int exp, Location<World> location) {
        final ExperienceOrb experienceOrb = (ExperienceOrb) createDroppedEntity(EntityTypes.EXPERIENCE_ORB, location);
        experienceOrb.offer(Keys.CONTAINED_EXPERIENCE, exp / 2);
        return experienceOrb;
    }

    private List<ItemStackSnapshot> dropItems(Player player, ItemEntity itemDropConfig) {
        List<ItemStackSnapshot> droppedItems = Lists.newArrayList();

        PlayerInventory playerInventory = (PlayerInventory) player.getInventory();

        droppedItems.addAll(inventoryDropItems(playerInventory.getHotbar(), itemDropConfig));
        droppedItems.addAll(inventoryDropItems(playerInventory.getMainGrid(), itemDropConfig));
        if (itemDropConfig.isEquipment()) {
            droppedItems.addAll(inventoryDropItems(playerInventory.getEquipment(), itemDropConfig));
        }

        return droppedItems;
    }

    private List<ItemStackSnapshot> inventoryDropItems(Inventory inventory, ItemEntity itemDropConfig) {
        double chance = itemDropConfig.getChance();
        final List<ItemStackSnapshot> inventoryDroppedItems = Lists.newArrayList();
        if (itemDropConfig.isEnable()) {
            for (Inventory slot : inventory.slots()) {
                slot.peek().ifPresent(itemStack -> {
                    // 掉落白名单
                    if (!itemDropConfig.getWhitelist().contains(itemStack.getType().getName())) {
                        int quantity = itemStack.getQuantity();
                        if (quantity > chance * 100) {
                            ItemStack dropItemStack = itemStack.copy();
                            int dropCount = (int) (quantity * chance);
                            dropItemStack.setQuantity(dropCount);
                            inventoryDroppedItems.add(dropItemStack.createSnapshot());
                            itemStack.setQuantity(quantity - dropCount);
                            slot.set(itemStack);
                        } else if (Math.random() < chance) {
                            inventoryDroppedItems.add(itemStack.createSnapshot());
                            slot.set(ItemStack.of(ItemTypes.AIR));
                        }
                    }
                });
            }
        }
        return inventoryDroppedItems.stream()
                .filter(snapshot -> !snapshot.isEmpty())
                .collect(Collectors.toList());
    }

    private Item createItemEntity(ItemStackSnapshot itemStackSnapshot, Location<World> location) {
        final Item item = (Item) createDroppedEntity(EntityTypes.ITEM, location);
        item.offer(Keys.REPRESENTED_ITEM, itemStackSnapshot);
        item.offer(Keys.PICKUP_DELAY, 15);
        return item;
    }

    private void spawnItems(List<ItemStackSnapshot> items, Location<World> location) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            final DropItemEvent.Destruct dropItemEvent = SpongeEventFactory.createDropItemEventDestruct(
                    frame.getCurrentCause(),
                    ImmutableList.copyOf(items)
            );
            Sponge.getEventManager().post(dropItemEvent);
            if (!dropItemEvent.isCancelled()) {
                final List<Item> dropItems = items.stream()
                        .filter(snapshot -> !snapshot.isEmpty())
                        .map(snapshot -> createItemEntity(snapshot, location))
                        .collect(Collectors.toList());
                location.getExtent().spawnEntities(dropItems);
            }
        }
    }
}
