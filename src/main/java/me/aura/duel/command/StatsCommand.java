package me.aura.duel.command;

import me.aura.duel.duel.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class StatsCommand implements CommandExecutor {

    private final StatsManager statsManager;

    public StatsCommand(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (command.getName().equalsIgnoreCase("dueltop")) {
            showTop(sender);
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cاستخدم: /duelstats <player>");
                return true;
            }

            showStats(sender, player.getUniqueId(), player.getName());
            return true;
        }

        if (args.length == 1) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

            if (!target.hasPlayedBefore() && !target.isOnline()) {
                sender.sendMessage("§cهذا اللاعب غير معروف.");
                return true;
            }

            showStats(
                    sender,
                    target.getUniqueId(),
                    target.getName() == null ? args[0] : target.getName()
            );
            return true;
        }

        sender.sendMessage("§cالاستخدام: /duelstats [player]");
        return true;
    }

    private void showStats(
            CommandSender sender,
            UUID uuid,
            String name
    ) {
        sender.sendMessage("§8§m------------------------");
        sender.sendMessage("§6§lAURA §f- إحصائيات اللاعب");
        sender.sendMessage("§eاللاعب: §f" + name);
        sender.sendMessage("§aالانتصارات: §f" + statsManager.getWins(uuid));
        sender.sendMessage("§cالخسارات: §f" + statsManager.getLosses(uuid));
        sender.sendMessage("§bالقتلات: §f" + statsManager.getKills(uuid));
        sender.sendMessage("§dELO: §f" + statsManager.getElo(uuid));
        sender.sendMessage("§8§m------------------------");
    }

    private void showTop(CommandSender sender) {
        List<StatsManager.PlayerStats> leaderboard =
                statsManager.getLeaderboard();

        sender.sendMessage("§8§m------------------------");
        sender.sendMessage("§6§lAURA §f- أفضل اللاعبين");

        if (leaderboard.isEmpty()) {
            sender.sendMessage("§cلا توجد إحصائيات حتى الآن.");
            return;
        }

        int rank = 1;

        for (StatsManager.PlayerStats stats : leaderboard) {
            if (rank > 10) {
                break;
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(stats.uuid());
            String name = player.getName() == null
                    ? "Unknown"
                    : player.getName();

            sender.sendMessage(
                    "§e#" + rank
                            + " §f" + name
                            + " §7- §d" + stats.elo() + " ELO"
                            + " §8| §a" + stats.wins() + " Wins"
            );

            rank++;
        }

        sender.sendMessage("§8§m------------------------");
    }
          }
