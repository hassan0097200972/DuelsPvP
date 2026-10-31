package me.aura.duel.arena;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public final class ArenaManager {

    private final JavaPlugin plugin;
    private final Set<String> busyArenas = new HashSet<>();

    public ArenaManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean createArena(String name, World world) {
        if (!isValidName(name) || world == null || exists(name)) {
            return false;
        }

        String path = getPath(name);

        plugin.getConfig().set(path + ".world", world.getName());
        plugin.getConfig().set(path + ".enabled", true);
        plugin.getConfig().set(path + ".kits", new java.util.ArrayList<>());

        plugin.saveConfig();
        return true;
    }

    public boolean deleteArena(String name) {
        if (!exists(name) || isBusy(name)) {
            return false;
        }

        plugin.getConfig().set(getPath(name), null);
        plugin.saveConfig();

        return true;
    }

    public boolean setSpawn(String name, int number, Location location) {
        if (!exists(name) || location == null || (number != 1 && number != 2)) {
            return false;
        }

        String path = getPath(name);

        String worldName = plugin.getConfig().getString(path + ".world");

        if (worldName == null || !location.getWorld().getName().equals(worldName)) {
            return false;
        }

        plugin.getConfig().set(
                path + ".spawn" + number,
                location.clone()
        );

        plugin.saveConfig();
        return true;
    }

    public boolean setCorner(String name, int number, Location location) {
        if (!exists(name) || location == null || (number != 1 && number != 2)) {
            return false;
        }

        String path = getPath(name);

        String worldName = plugin.getConfig().getString(path + ".world");

        if (worldName == null || !location.getWorld().getName().equals(worldName)) {
            return false;
        }

        plugin.getConfig().set(
                path + ".pos" + number,
                location.clone()
        );

        plugin.saveConfig();
        return true;
    }

    public boolean setArenaKits(String name, Set<String> kits) {
        if (!exists(name) || kits == null) {
            return false;
        }

        plugin.getConfig().set(
                getPath(name) + ".kits",
                kits.stream().map(String::toLowerCase).toList()
        );

        plugin.saveConfig();
        return true;
    }

    public boolean setEnabled(String name, boolean enabled) {
        if (!exists(name) || (isBusy(name) && !enabled)) {
            return false;
        }

        plugin.getConfig().set(
                getPath(name) + ".enabled",
                enabled
        );

        plugin.saveConfig();
        return true;
    }

    public Location getSpawn(String name, int number) {
        if (!exists(name) || (number != 1 && number != 2)) {
            return null;
        }

        Location location = plugin.getConfig().getLocation(
                getPath(name) + ".spawn" + number
        );

        return location == null ? null : location.clone();
    }

    public Location getCorner(String name, int number) {
        if (!exists(name) || (number != 1 && number != 2)) {
            return null;
        }

        Location location = plugin.getConfig().getLocation(
                getPath(name) + ".pos" + number
        );

        return location == null ? null : location.clone();
    }

    public String getWorldName(String name) {
        return plugin.getConfig().getString(
                getPath(name) + ".world"
        );
    }

    public boolean isInsideArena(String name, Location location) {
        if (!exists(name) || location == null || location.getWorld() == null) {
            return false;
        }

        Location first = getCorner(name, 1);
        Location second = getCorner(name, 2);

        if (first == null || second == null) {
            return false;
        }

        if (!location.getWorld().equals(first.getWorld())
                || !location.getWorld().equals(second.getWorld())) {
            return false;
        }

        double minX = Math.min(first.getX(), second.getX());
        double maxX = Math.max(first.getX(), second.getX());
        double minY = Math.min(first.getY(), second.getY());
        double maxY = Math.max(first.getY(), second.getY());
        double minZ = Math.min(first.getZ(), second.getZ());
        double maxZ = Math.max(first.getZ(), second.getZ());

        return location.getX() >= minX && location.getX() <= maxX
                && location.getY() >= minY && location.getY() <= maxY
                && location.getZ() >= minZ && location.getZ() <= maxZ;
    }

    public boolean isReady(String name) {
        return exists(name)
                && getSpawn(name, 1) != null
                && getSpawn(name, 2) != null
                && getCorner(name, 1) != null
                && getCorner(name, 2) != null;
    }

    public boolean exists(String name) {
        return isValidName(name)
                && plugin.getConfig().contains(getPath(name));
    }

    public boolean isEnabled(String name) {
        return exists(name)
                && plugin.getConfig().getBoolean(
                        getPath(name) + ".enabled",
                        false
                );
    }

    public boolean isBusy(String name) {
        return busyArenas.contains(name.toLowerCase());
    }

    public boolean setBusy(String name, boolean busy) {
        if (!exists(name)) {
            return false;
        }

        if (busy) {
            return busyArenas.add(name.toLowerCase());
        }

        busyArenas.remove(name.toLowerCase());
        return true;
    }

    public Set<String> getArenaNames() {
        ConfigurationSection section =
                plugin.getConfig().getConfigurationSection("arenas");

        if (section == null) {
            return Set.of();
        }

        return section.getKeys(false);
    }

    public Set<String> getArenaKits(String name) {
        if (!exists(name)) {
            return Set.of();
        }

        return new HashSet<>(
                plugin.getConfig().getStringList(
                        getPath(name) + ".kits"
                )
        );
    }

    private String getPath(String name) {
        return "arenas." + name.toLowerCase();
    }

    private boolean isValidName(String name) {
        return name != null
                && name.matches("[a-zA-Z0-9_-]{1,32}");
    }
                               }
