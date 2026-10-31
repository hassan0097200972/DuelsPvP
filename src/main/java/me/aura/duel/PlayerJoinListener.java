package me.aura.duel;

import me.aura.duel.gui.LobbyListener;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final LobbyListener lobbyListener;

    public PlayerJoinListener(LobbyListener lobbyListener) {
        this.lobbyListener = lobbyListener;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        giveLobbyItemsIfNeeded(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        giveLobbyItemsIfNeeded(event.getPlayer());
    }

    private void giveLobbyItemsIfNeeded(Player player) {
        World world = player.getWorld();

        if (!world.getName().equalsIgnoreCase("duel")) {
            return;
        }

        lobbyListener.giveLobbyItems(player);
    }
}
