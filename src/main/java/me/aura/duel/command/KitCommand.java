package me.aura.duel.command;

import me.aura.duel.kit.KitManager;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public final class KitCommand implements CommandExecutor {

    private final KitManager kitManager;

    public KitCommand(KitManager kitManager) {
        this.kitManager = kitManager;
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

            case "add" -> {
                if (args.length != 3) {
                    sender.sendMessage("§c/kit add <name> <item>");
                    return true;
                }

                Material icon = Material.matchMaterial(args[2]);

                if (icon == null || !icon.isItem()) {
                    sender.sendMessage("§cاسم العنصر غير صحيح.");
                    return true;
                }

                if (kitManager.createKit(args[1], icon)) {
                    sender.sendMessage("§aتم إنشاء الكِت: §e" + args[1]);
                } else {
                    sender.sendMessage("§cالكِت موجود مسبقًا أو الاسم غير صالح.");
                }
            }

            case "set" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cهذا الأمر للاعبين فقط.");
                    return true;
                }

                if (args.length != 2) {
                    sender.sendMessage("§c/kit set <name>");
                    return true;
                }

                if (kitManager.saveKit(args[1], player)) {
                    sender.sendMessage("§aتم حفظ محتويات الكِت بنجاح.");
                } else {
                    sender.sendMessage("§cالكِت غير موجود.");
                }
            }

            case "icon" -> {
                if (args.length != 3) {
                    sender.sendMessage("§c/kit icon <name> <item>");
                    return true;
                }

                Material icon = Material.matchMaterial(args[2]);

                if (icon == null || !icon.isItem()) {
                    sender.sendMessage("§cاسم العنصر غير صحيح.");
                    return true;
                }

                if (kitManager.setIcon(args[1], icon)) {
                    sender.sendMessage("§aتم تغيير أيقونة الكِت.");
                } else {
                    sender.sendMessage("§cالكِت غير موجود.");
                }
            }

            case "delete" -> {
                if (args.length != 2) {
                    sender.sendMessage("§c/kit delete <name>");
                    return true;
                }

                if (kitManager.deleteKit(args[1])) {
                    sender.sendMessage("§aتم حذف الكِت.");
                } else {
                    sender.sendMessage("§cالكِت غير موجود.");
                }
            }

            case "list" -> {
                Set<String> kits = kitManager.getKitNames();

                if (kits.isEmpty()) {
                    sender.sendMessage("§cلا توجد كِتات.");
                    return true;
                }

                sender.sendMessage("§6قائمة الكِتات:");

                for (String kit : kits) {
                    sender.sendMessage("§e- " + kit);
                }
            }

            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m------------------------");
        sender.sendMessage("§6§lAURA §fKit Commands");
        sender.sendMessage("§e/kit add <name> <item>");
        sender.sendMessage("§e/kit set <name>");
        sender.sendMessage("§e/kit icon <name> <item>");
        sender.sendMessage("§e/kit delete <name>");
        sender.sendMessage("§e/kit list");
        sender.sendMessage("§8§m------------------------");
    }
                                         }
