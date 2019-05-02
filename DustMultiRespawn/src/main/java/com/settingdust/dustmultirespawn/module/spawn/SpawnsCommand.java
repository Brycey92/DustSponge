package com.settingdust.dustmultirespawn.module.spawn;

import com.settingdust.dustmultirespawn.DustMultiRespawn;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Map;

public class SpawnsCommand {
    private DustMultiRespawn plugin = DustMultiRespawn.getInstance();
    private ConfigurationNode locale = plugin.getLocale();
    private SpawnsProvider spawnsProvider;

    SpawnsCommand(SpawnsProvider spawnsProvider) {
        Sponge.getCommandManager().register(plugin, CommandSpec.builder()
                .permission("dust.spawn.use")
                .description(Text.of(locale
                        .getNode("command")
                        .getNode("spawn")
                        .getNode("desc")
                        .getString()))
                .arguments(GenericArguments.requiringPermission(
                        GenericArguments.optional(
                                GenericArguments.string(Text.of("name"))),
                        "dust.spawn.choose"))
                .executor(new Main())
                .child(CommandSpec.builder()
                                .permission("dust.spawn.add")
                                .description(Text.of(locale
                                        .getNode("command")
                                        .getNode("add")
                                        .getNode("desc")
                                        .getString()))
                                .arguments(GenericArguments.string(Text.of("name")))
                                .executor(new Add())
                                .build()
                        , "add", "a", "set", "s")
                .child(CommandSpec.builder()
                                .permission("dust.spawn.remove")
                                .description(Text.of(locale
                                        .getNode("command")
                                        .getNode("remove")
                                        .getNode("desc")
                                        .getString()))
                                .arguments(GenericArguments.string(Text.of("name")))
                                .executor(new Remove())
                                .build()
                        , "remove", "rm", "delete", "del")
                .child(CommandSpec.builder()
                                .permission("dust.spawn.list")
                                .description(Text.of(locale
                                        .getNode("command")
                                        .getNode("list")
                                        .getNode("desc")
                                        .getString()))
                                .executor(new List())
                                .build()
                        , "list", "l", "all")
                .build(), "spawn", "dustspawn", "ds");

        this.spawnsProvider = spawnsProvider;
    }

    class Main implements CommandExecutor {
        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (src instanceof Player) {
                Player player = (Player) src;
                SpawnNode spawnNode;
                if (args.getOne("name").isPresent()) {
                    String name = String.valueOf(args.getOne("name").get());
                    if (spawnsProvider.getLocations().containsKey(name)) {
                        if(spawnsProvider.canPlayerUseSpawn(player, name)) {
                            spawnNode = spawnsProvider.getLocations().get(name);
                        } else {
                            src.sendMessage(Text.builder()
                                    .color(TextColors.RED)
                                    .append(Text.of(locale
                                            .getNode("command")
                                            .getNode("noPermission")
                                            .getString().replaceAll("%name%", name)
                                    )).build()
                            );
                            return CommandResult.success();
                        }
                    } else {
                        src.sendMessage(Text.builder()
                                .color(TextColors.RED)
                                .append(Text.of(locale
                                        .getNode("command")
                                        .getNode("notExist")
                                        .getString()
                                )).build()
                        );
                        return CommandResult.success();
                    }
                } else {
                    spawnNode = spawnsProvider.getSpawnLocation(player.getLocation(), player);
                }

                player.setLocationSafely(spawnNode.location);
                player.setRotation(spawnNode.rotation);
                src.sendMessage(Text.builder()
                        .color(TextColors.GREEN)
                        .append(Text.of(locale
                            .getNode("operation")
                            .getNode("spawn")
                            .getNode("success")
                            .getString()
                        )).build()
                );
            } else {
                src.sendMessage(Text.builder()
                        .color(TextColors.RED)
                        .append(Text.of(locale
                                .getNode("command")
                                .getNode("onlyPlayer")
                                .getString()
                        )).build()
                );
            }
            return CommandResult.success();
        }
    }

    class Add implements CommandExecutor {
        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (src instanceof Player) {
                Player player = (Player) src;
                String name = String.valueOf(args.getOne("name").get());
                spawnsProvider.add(name, player.getLocation(), player.getRotation());
                src.sendMessage(Text.builder()
                        .color(TextColors.GREEN)
                        .append(Text.of(locale
                                .getNode("operation")
                                .getNode("add")
                                .getNode("success")
                                .getString().replaceAll("%name%", name)
                        )).build()
                );
            } else {
                src.sendMessage(Text.builder()
                        .color(TextColors.RED)
                        .append(Text.of(locale
                                .getNode("command")
                                .getNode("onlyPlayer")
                                .getString()
                        )).build()
                );
            }
            return CommandResult.success();
        }
    }

    class Remove implements CommandExecutor {
        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            String name = String.valueOf(args.getOne("name").get());
            if (spawnsProvider.remove(name)) {
                src.sendMessage(Text.builder()
                        .color(TextColors.GREEN)
                        .append(Text.of(locale
                                .getNode("operation")
                                .getNode("remove")
                                .getNode("success")
                                .getString().replaceAll("%name%", name)
                        )).build()
                );
            } else {
                src.sendMessage(Text.builder()
                        .color(TextColors.RED)
                        .append(Text.of(locale
                                .getNode("command")
                                .getNode("notExist")
                                .getString()
                        )).build()
                );
            }
            return CommandResult.success();
        }
    }

    class List implements CommandExecutor {
        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            Map<String, SpawnNode> spawns = spawnsProvider.getLocations();
            if (spawns.size() == 0) {
                src.sendMessage(Text.builder()
                        .color(TextColors.RED)
                        .append(Text.of(locale
                                .getNode("operation")
                                .getNode("list")
                                .getNode("empty")
                                .getString()
                        )).build()
                );
            } else {
                src.sendMessage(Text.builder()
                        .color(TextColors.RED)
                        .append(Text.of(locale
                                .getNode("operation")
                                .getNode("list")
                                .getNode("empty")
                                .getString()
                        )).build()
                );
                for (String key : spawns.keySet()) {
                    Location location = spawns.get(key).location;
                    src.sendMessage(Text.builder()
                        .append(Text.builder(key)
                                .style(TextStyles.ITALIC)
                                .color(TextColors.GREEN)
                                .onHover(TextActions.showText(Text.of(locale
                                        .getNode("operation")
                                        .getNode("list")
                                        .getNode("click")
                                        .getString().replaceAll("%name%", key)
                                )))
                                .onClick(TextActions.runCommand("/spawn " + key))
                                .build())
                        .color(TextColors.YELLOW)
                        .append(Text.of(" - "))
                        .append(Text.of(locale
                                .getNode("operation")
                                .getNode("list")
                                .getNode("location")
                                .getString()))
                        .append(Text.of(": "))
                        .append(Text.of(((World) location.getExtent()).getName()))
                        .append(Text.of(", "))
                        .append(Text.of(location.getBlockPosition().toString()))
                        .build()
                    );
                }
            }
            return CommandResult.success();
        }
    }
}
