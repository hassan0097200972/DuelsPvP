package me.aura.duel.duel;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class StatsManager {

    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration data;

    public StatsManager(JavaPlugin plugin) {
        this.plugin = plugin;

        this.file = new File(plugin.getDataFolder(), "stats.yml");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public void addWin(UUID uuid) {
        String path = getPath(uuid);

        data.set(path + ".wins",
                data.getInt(path + ".wins") + 1);

        data.set(path + ".elo",
                data.getInt(path + ".elo", 1000) + 25);

        save();
    }

    public void addLoss(UUID uuid) {
        String path = getPath(uuid);

        data.set(path + ".losses",
                data.getInt(path + ".losses") + 1);

        data.set(path + ".elo",
                Math.max(0, data.getInt(path + ".elo", 1000) - 15));

        save();
    }

    public void addKill(UUID uuid) {
        String path = getPath(uuid);

        data.set(path + ".kills",
                data.getInt(path + ".kills") + 1);

        save();
    }

    public int getWins(UUID uuid) {
        return data.getInt(getPath(uuid) + ".wins");
    }

    public int getLosses(UUID uuid) {
        return data.getInt(getPath(uuid) + ".losses");
    }

    public int getKills(UUID uuid) {
        return data.getInt(getPath(uuid) + ".kills");
    }

    public int getElo(UUID uuid) {
        return data.getInt(getPath(uuid) + ".elo", 1000);
    }

    public void resetStats(UUID uuid) {
        data.set(getPath(uuid), null);
        save();
    }

    public List<LeaderboardEntry> getLeaderboard(int limit) {
        List<LeaderboardEntry> entries = new ArrayList<>();

        ConfigurationSection section =
                data.getConfigurationSection("players");

        if (section == null) {
            return entries;
        }

        for (String key : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);

                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

                entries.add(new LeaderboardEntry(
                        uuid,
                        player.getName() == null
                                ? "Unknown" : player.getName(),
                        getWins(uuid),
                        getLosses(uuid),
                        getKills(uuid),
                        getElo(uuid)
                ));

            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning(
                        "Invalid player UUID in stats.yml: " + key
                );
            }
        }

        entries.sort(
                Comparator.comparingInt(LeaderboardEntry::elo)
                        .reversed()
        );

        return entries.stream()
                .limit(Math.max(0, limit))
                .toList();
    }

    private String getPath(UUID uuid) {
        return "players." + uuid;
    }

    public void save() {
        try {
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe(
                    "Could not save player statistics!"
            );
            exception.printStackTrace();
        }
    }

    public record LeaderboardEntry(
            UUID uuid,
            String name,
            int wins,
            int losses,
            int kills,
            int elo
    ) {}
}
