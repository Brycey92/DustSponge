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
import org.spongepowered.api.text.format.TextColors;
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
                        .getNode("desc").getString()))
                .executor(new Main())
                .child(CommandSpec.builder()
                                .permission("dust.spawn.add")
                                .description(Text.of(locale
                                        .getNode("command")
                                        .getNode("add")
                                        .getNode("desc").getString()))
                                .arguments(GenericArguments.string(Text.of("name")))
                                .executor(new Add())
                                .build()
                        , "add", "a", "set", "s")
                .child(CommandSpec.builder()
                                .permission("dust.spawn.remove")
                                .description(Text.of(locale
                                        .getNode("command")
                                        .getNode("remove")
                                        .getNode("desc").getString()))
                                .arguments(GenericArguments.string(Text.of("name")))
                                .executor(new Remove())
                                .build()
                        , "remove", "rm", "delete", "del")
                .child(CommandSpec.builder()
                                .permission("dust.spawn.list")
                                .description(Text.of(locale
                                        .getNode("command")
                                        .getNode("list")
                                        .getNode("desc").getString()))
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
                player.setLocationSafely(spawnsProvider.getSpawnLocation(player.getLocation(), player));
            } else {
                src.sendMessage(Text.builder()
                        .color(TextColors.RED)
                        .append(Text.of(locale
                                .getNode("command")
                                .getNode("onlyPlayer").getString())).build()
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
                spawnsProvider.add(name, player.getLocation());
                src.sendMessage(Text.builder()
                        .color(TextColors.GREEN)
                        .append(Text.of(locale
                                .getNode("operation")
                                .getNode("add")
                                .getNode("success").getString())).build()
                );
            } else {
                src.sendMessage(Text.builder()
                        .color(TextColors.RED)
                        .append(Text.of(locale
                                .getNode("command")
                                .getNode("onlyPlayer").getString())).build()
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
                                .getNode("success").getString())).build()
                );
            } else {
                src.sendMessage(Text.builder()
                        .color(TextColors.RED)
                        .append(Text.of(locale
                                .getNode("command")
                                .getNode("notExist").getString())).build()
                );
            }
            return CommandResult.success();
        }
    }

    class List implements CommandExecutor {
        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            Map<String, Location> spawns = spawnsProvider.getLocations();
            if (spawns.size() == 0) {
                src.sendMessage(
                        Text.builder().color(TextColors.RED)
                                .append(Text.of(
                                        locale.getNode("operation")
                                                .getNode("list")
                                                .getNode("empty").getString()
                                )).build()
                );
            } else {
                for (String key : spawns.keySet()) {
                    Location location = spawns.get(key);
                    src.sendMessage(
                            Text.joinWith(Text.of(" "),
                                    Text.builder()
                                            .color(TextColors.AQUA)
                                            .append(Text.of(key))
                                            .append(Text.of(":")).build(),
                                    Text.of(TextColors.AQUA),
                                    Text.of(location.getX()),
                                    Text.of(location.getY()),
                                    Text.of(location.getZ()),
                                    Text.of(((World) location.getExtent()).getName())
                            )
                    );
                }
            }
            return CommandResult.success();
        }
    }
}
