package me.aura.duel;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class DuelsPvP extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("kit").setExecutor(this);
        getLogger().info("DuelsPvP enabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!sender.hasPermission("duel.admin")) {
            sender.sendMessage("§cNo permission!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§e/kit add <name> <item>");
            sender.sendMessage("§e/kit set <name>");
            sender.sendMessage("§e/kit delete <name>");
            sender.sendMessage("§e/kit list");
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "add" -> addKit(sender, args);
            case "set" -> setKit(sender, args);
            case "delete" -> deleteKit(sender, args);
            case "list" -> listKits(sender);
            default -> sender.sendMessage("§cUnknown command.");
        }

        return true;
    }

    private void addKit(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage("§c/kit add <name> <item>");
            return;
        }

        String name = args[1].toLowerCase(Locale.ROOT);
        Material icon = Material.matchMaterial(args[2]);

        if (icon == null || !icon.isItem()) {
            sender.sendMessage("§cInvalid item!");
            return;
        }

        String path = "kits." + name;

        if (getConfig().contains(path)) {
            sender.sendMessage("§cKit already exists!");
            return;
        }

        getConfig().set(path + ".icon", icon.name());
        saveConfig();

        sender.sendMessage("§aKit created: §e" + name);
    }

    private void setKit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cPlayers only!");
            return;
        }

        if (args.length != 2) {
            sender.sendMessage("§c/kit set <name>");
            return;
        }

        String name = args[1].toLowerCase(Locale.ROOT);
        String path = "kits." + name;

        if (!getConfig().contains(path)) {
            sender.sendMessage("§cKit not found!");
            return;
        }

        List<ItemStack> contents = Arrays.stream(
                player.getInventory().getStorageContents()
        ).map(item -> item == null ? null : item.clone())
         .collect(Collectors.toCollection(ArrayList::new));

        List<ItemStack> armor = Arrays.stream(
                player.getInventory().getArmorContents()
        ).map(item -> item == null ? null : item.clone())
         .collect(Collectors.toCollection(ArrayList::new));

        getConfig().set(path + ".contents", contents);
        getConfig().set(path + ".armor", armor);

        saveConfig();

        sender.sendMessage("§aKit saved successfully!");
    }

    private void deleteKit(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("§c/kit delete <name>");
            return;
        }

        String path = "kits." +
                args[1].toLowerCase(Locale.ROOT);

        if (!getConfig().contains(path)) {
            sender.sendMessage("§cKit not found!");
            return;
        }

        getConfig().set(path, null);
        saveConfig();

        sender.sendMessage("§aKit deleted!");
    }

    private void listKits(CommandSender sender) {
        ConfigurationSection section =
                getConfig().getConfigurationSection("kits");

        if (section == null || section.getKeys(false).isEmpty()) {
            sender.sendMessage("§cNo kits found!");
            return;
        }

        sender.sendMessage("§6Available kits:");

        for (String kit : section.getKeys(false)) {
            sender.sendMessage("§e- " + kit);
        }
    }
    }
