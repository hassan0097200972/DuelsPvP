package me.aura.duel.gui;

import me.aura.duel.kit.KitManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class KitMenu implements Listener {

    private final JavaPlugin plugin;
    private final KitManager kitManager;

    public KitMenu(JavaPlugin plugin, KitManager kitManager) {
        this.plugin = plugin;
        this.kitManager = kitManager;
    }

    public void open(Player player) {
        if (!player.getWorld().getName().equalsIgnoreCase("duel")) {
            player.sendMessage("§cهذه القائمة متاحة داخل عالم المبارزات فقط.");
            return;
        }

        MenuHolder holder = new MenuHolder();

        Inventory inventory = Bukkit.createInventory(
                holder,
                54,
                "§8§lAURA §7» §fاختيار الكِت"
        );

        holder.setInventory(inventory);

        int slot = 10;

        for (String kit : kitManager.getKitNames()) {

            if (slot >= 44) {
                break;
            }

            Material icon = kitManager.getIcon(kit);

            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName("§6§l" + kit);
                List<String> lore = new ArrayList<>();
                lore.add("§7");
                lore.add("§aاضغط لاختيار هذا الكِت");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            inventory.setItem(slot, item);
            holder.addKit(slot, kit);

            slot++;

            if (slot % 9 == 8) {
                slot += 2;
            }
        }

        if (holder.getKits().isEmpty()) {
            ItemStack empty = new ItemStack(Material.BARRIER);
            ItemMeta meta = empty.getItemMeta();

            if (meta != null) {
                meta.setDisplayName("§cلا توجد كِتات متاحة");
                empty.setItemMeta(meta);
            }

            inventory.setItem(22, empty);
        }

        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!(event.getView().getTopInventory().getHolder()
                instanceof MenuHolder holder)) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedInventory() == null
                || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        String kit = holder.getKit(event.getSlot());

        if (kit == null) {
            return;
        }

        if (!player.getWorld().getName().equalsIgnoreCase("duel")) {
            player.closeInventory();
            return;
        }

        if (kitManager.giveKit(kit, player)) {
            player.closeInventory();
            player.sendMessage("§aتم اختيار كِت §e" + kit);
        } else {
            player.sendMessage("§cتعذر اختيار هذا الكِت.");
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder()
                instanceof MenuHolder) {
            event.setCancelled(true);
        }
    }

    private static final class MenuHolder implements InventoryHolder {

        private final Map<Integer, String> kits = new HashMap<>();
        private Inventory inventory;

        private void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        private void addKit(int slot, String name) {
            kits.put(slot, name);
        }

        private String getKit(int slot) {
            return kits.get(slot);
        }

        private Map<Integer, String> getKits() {
            return kits;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}
