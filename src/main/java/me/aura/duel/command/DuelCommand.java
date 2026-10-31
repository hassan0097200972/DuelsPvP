package me.aura.duel.command;

import me.aura.duel.duel.DuelManager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class DuelCommand implements CommandExecutor {

    private final DuelManager duelManager;

    public DuelCommand(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cهذا الأمر للاعبين فقط.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "accept" -> {
                if (duelManager.acceptRequest(player)) {
                    player.sendMessage("§aقبلت طلب المبارزة!");
                } else {
                    player.sendMessage("§cلا يوجد طلب مبارزة متاح.");
                }
            }

            case "deny" -> {
                if (!duelManager.denyRequest(player)) {
                    player.sendMessage("§cلا يوجد طلب مبارزة لرفضه.");
                }
            }

            case "leave" -> {
                if (!duelManager.isInDuel(player)) {
                    player.sendMessage("§cأنت لست داخل مبارزة.");
                    return true;
                }

                player.sendMessage("§cغادرت المبارزة.");
                duelManager.handleQuit(player);
            }

            default -> {
                if (args.length != 2) {
                    sendHelp(player);
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[0]);

                if (target == null) {
                    player.sendMessage("§cاللاعب غير متصل.");
                    return true;
                }

                String kit = args[1];

                if (!duelManager.sendRequest(player, target, kit)) {
                    player.sendMessage(
                            "§cتعذر إرسال الطلب. تأكد من اسم الكِت وحالة اللاعبين."
                    );
                }
            }
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§8§m------------------------");
        player.sendMessage("§6§lAURA §fDuels");
        player.sendMessage("§e/duel <player> <kit> §7إرسال طلب مبارزة");
        player.sendMessage("§e/duel accept §7قبول طلب");
        player.sendMessage("§e/duel deny §7رفض طلب");
        player.sendMessage("§e/duel leave §7مغادرة المبارزة");
        player.sendMessage("§8§m------------------------");
    }
}
