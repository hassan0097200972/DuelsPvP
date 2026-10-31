package me.aura.duel.kit;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class KitManager {

    private final JavaPlugin plugin;

    public KitManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean createKit(String name, Material icon) {
        if (!isValidName(name) || icon == null || !icon.isItem()) {
            return false;
        }

        String path = getPath(name);

        if (plugin.getConfig().contains(path)) {
            return false;
        }

        plugin.getConfig().set(path + ".icon", icon.name());
        plugin.getConfig().set(path + ".contents", new ArrayList<>());
        plugin.getConfig().set(path + ".armor", new ArrayList<>());

        plugin.saveConfig();
        return true;
    }

    public boolean saveKit(String name, Player player) {
        if (!exists(name) || player == null) {
            return false;
        }

        String path = getPath(name);

        List<ItemStack> contents = cloneItems(
                player.getInventory().getStorageContents()
        );

        List<ItemStack> armor = cloneItems(
                player.getInventory().getArmorContents()
        );

        plugin.getConfig().set(path + ".contents", contents);
        plugin.getConfig().set(path + ".armor", armor);

        plugin.saveConfig();
        return true;
    }

    public boolean deleteKit(String name) {
        if (!exists(name)) {
            return false;
        }

        plugin.getConfig().set(getPath(name), null);
        plugin.saveConfig();

        return true;
    }

    public boolean setIcon(String name, Material icon) {
        if (!exists(name) || icon == null || !icon.isItem()) {
            return false;
        }

        plugin.getConfig().set(
                getPath(name) + ".icon",
                icon.name()
        );

        plugin.saveConfig();
        return true;
    }

    public boolean giveKit(String name, Player player) {
        if (!exists(name) || player == null) {
            return false;
        }

        String path = getPath(name);

        List<?> contents = plugin.getConfig().getList(
                path + ".contents"
        );

        List<?> armor = plugin.getConfig().getList(
                path + ".armor"
        );

        player.getInventory().clear();
        player.getInventory().setArmorContents(
                readItems(armor, 4)
        );

        player.getInventory().setStorageContents(
                readItems(contents, 36)
        );

        player.updateInventory();
        return true;
    }

    public Material getIcon(String name) {
        if (!exists(name)) {
            return Material.BARRIER;
        }

        String material = plugin.getConfig().getString(
                getPath(name) + ".icon"
        );

        if (material == null) {
            return Material.BARRIER;
        }

        Material icon = Material.matchMaterial(material);

        return icon != null && icon.isItem()
                ? icon
                : Material.BARRIER;
    }

    public boolean exists(String name) {
        return isValidName(name)
                && plugin.getConfig().contains(getPath(name));
    }

    public Set<String> getKitNames() {
        ConfigurationSection section =
                plugin.getConfig().getConfigurationSection("kits");

        if (section == null) {
            return Set.of();
        }

        return section.getKeys(false);
    }

    private String getPath(String name) {
        return "kits." + name.toLowerCase(Locale.ROOT);
    }

    private boolean isValidName(String name) {
        return name != null
                && name.matches("[a-zA-Z0-9_-]{1,32}");
    }

    private List<ItemStack> cloneItems(ItemStack[] items) {
        List<ItemStack> result = new ArrayList<>();

        for (ItemStack item : items) {
            result.add(item == null ? null : item.clone());
        }

        return result;
    }

    private ItemStack[] readItems(List<?> items, int size) {
        ItemStack[] result = new ItemStack[size];

        if (items == null) {
            return result;
        }

        for (int i = 0; i < Math.min(items.size(), size); i++) {
            Object object = items.get(i);

            if (object instanceof ItemStack item) {
                result[i] = item.clone();
            }
        }

        return result;
    }
}
