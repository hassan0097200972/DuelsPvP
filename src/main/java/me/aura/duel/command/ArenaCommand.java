package me.aura.duel.command;

import me.aura.duel.arena.ArenaManager;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ArenaCommand implements CommandExecutor {

    private final ArenaManager arenaManager;

    public ArenaCommand(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!sender.hasPermission("duel.admin")) {
            sender.sendMessage("§cليس لديك صلاحية.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "create" -> {
                if (!(sender instanceof Player player) || args.length != 2) {
                    sender.sendMessage("§c/arena create <name>");
                    return true;
                }

                World world = player.getWorld();

                if (!world.getName().equalsIgnoreCase("duel")) {
                    sender.sendMessage("§cيجب أن تكون داخل عالم duel.");
                    return true;
                }

                if (
