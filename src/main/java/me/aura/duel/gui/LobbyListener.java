package me.aura.duel.gui;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class LobbyListener implements Listener {

    private final KitMenu kitMenu;
    private final NamespacedKey itemKey;

    public LobbyListener(JavaPlugin plugin, KitMenu kitMenu) {
        this.kitMenu = kitMenu;
        this.itemKey = new NamespacedKey(plugin, "aura_lobby_item");
    }

    public void giveLobbyItems(Player player) {
        if (!player.getWorld().getName().equalsIgnoreCase("duel")) {
            return;
        }

        player.getInventory().removeItem(
                createLobbyItem(Material.IRON_SWORD, "kits")
        );

        player.getInventory().removeItem(
                createLobbyItem(Material.CRAFTING_TABLE, "kits")
        );

        player.getInventory().setItem(
                0,
                createLobbyItem(
                        Material.IRON_SWORD,
                        "kits"
                )
        );

        player.getInventory().setItem(
                4,
                createLobbyItem(
                        Material.CRAFTING_TABLE,
                        "kits"
                )
        );
    }

    private ItemStack createLobbyItem(Material material, String type) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(
                    type.equals("kits")
                            ? "§6§lAURA §fاختيار الكِت"
                            : "§6§lAURA §fالقائمة"
            );

            meta.setLore(List.of(
                    "§7اضغط بالزر الأيمن للفتح"
            ));

            meta.getPersistentDataContainer().set(
                    itemKey,
                    PersistentDataType.STRING,
                    type
            );

            item.setItemMeta(meta);
        }

        return item;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR
                && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.getWorld().getName().equalsIgnoreCase("duel")) {
            return;
        }

        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) {
            return;
        }

        String type = item.getItemMeta()
                .getPersistentDataContainer()
                .get(itemKey, PersistentDataType.STRING);

        if (type == null) {
            return;
        }

        event.setCancelled(true);

        if (type.equals("kits")) {
            kitMenu.open(player);
        }
    }
}
