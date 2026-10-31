package me.aura.duel.duel;

import me.aura.duel.arena.ArenaManager;
import me.aura.duel.kit.KitManager;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DuelManager {

    private final JavaPlugin plugin;
    private final ArenaManager arenaManager;
    private final KitManager kitManager;

    private final Map<UUID, DuelRequest> requests = new HashMap<>();
    private final Map<UUID, Match> matches = new HashMap<>();

    public DuelManager(
            JavaPlugin plugin,
            ArenaManager arenaManager,
            KitManager kitManager
    ) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.kitManager = kitManager;
    }

    public boolean sendRequest(Player sender, Player target, String kit) {
        if (sender == null || target == null
                || sender.equals(target)) {
            return false;
        }

        if (!kitManager.exists(kit)
                || isInDuel(sender.getUniqueId())
                || isInDuel(target.getUniqueId())
                || requests.containsKey(target.getUniqueId())) {
            return false;
        }

        requests.put(
                target.getUniqueId(),
                new DuelRequest(sender.getUniqueId(), kit)
        );

        sender.sendMessage("§aتم إرسال طلب المبارزة إلى §e"
                + target.getName());

        target.sendMessage("§6وصل إليك طلب مبارزة من §e"
                + sender.getName());

        target.sendMessage("§aلقبول الطلب: §e/duel accept");
        target.sendMessage("§cلرفض الطلب: §e/duel deny");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            DuelRequest request = requests.get(target.getUniqueId());

            if (request != null
                    && request.sender.equals(sender.getUniqueId())) {
                requests.remove(target.getUniqueId());

                Player onlineTarget = Bukkit.getPlayer(target.getUniqueId());

                if (onlineTarget != null) {
                    onlineTarget.sendMessage("§cانتهت صلاحية طلب المبارزة.");
                }

                Player onlineSender = Bukkit.getPlayer(sender.getUniqueId());

                if (onlineSender != null) {
                    onlineSender.sendMessage("§cانتهت صلاحية طلب المبارزة.");
                }
            }
        }, 1200L);

        return true;
    }

    public boolean acceptRequest(Player target) {
        DuelRequest request = requests.remove(target.getUniqueId());

        if (request == null) {
            return false;
        }

        Player sender = Bukkit.getPlayer(request.sender);

        if (sender == null || !sender.isOnline()) {
            target.sendMessage("§cاللاعب غير متصل.");
            return false;
        }

        if (isInDuel(sender.getUniqueId())
                || isInDuel(target.getUniqueId())) {
            target.sendMessage("§cأحد اللاعبين داخل مبارزة بالفعل.");
            return false;
        }

        String arena = findAvailableArena(request.kit);

        if (arena == null) {
            target.sendMessage("§cلا توجد ساحة متاحة لهذا الكِت.");
            sender.sendMessage("§cلا توجد ساحة متاحة لهذا الكِت.");
            return false;
        }

        if (!arenaManager.setBusy(arena, true)) {
            target.sendMessage("§cتعذر حجز الساحة.");
            return false;
        }

        Location spawn1 = arenaManager.getSpawn(arena, 1);
        Location spawn2 = arenaManager.getSpawn(arena, 2);

        if (spawn1 == null || spawn2 == null
                || spawn1.getWorld() == null
                || spawn2.getWorld() == null) {
            arenaManager.setBusy(arena, false);
            target.sendMessage("§cإعدادات الساحة غير مكتملة.");
            return false;
        }

        Match match = new Match(
                sender.getUniqueId(),
                target.getUniqueId(),
                arena,
                request.kit,
                new PlayerState(sender),
                new PlayerState(target)
        );

        matches.put(sender.getUniqueId(), match);
        matches.put(target.getUniqueId(), match);

        sender.teleport(spawn1);
        target.teleport(spawn2);

        kitManager.giveKit(request.kit, sender);
        kitManager.giveKit(request.kit, target);

        sender.sendMessage("§aبدأت المبارزة!");
        target.sendMessage("§aبدأت المبارزة!");

        return true;
    }

    public boolean denyRequest(Player target) {
        DuelRequest request = requests.remove(target.getUniqueId());

        if (request == null) {
            return false;
        }

        Player sender = Bukkit.getPlayer(request.sender);

        target.sendMessage("§cرفضت طلب المبارزة.");

        if (sender != null) {
            sender.sendMessage("§cتم رفض طلب المبارزة.");
        }

        return true;
    }

    public boolean endDuel(Player winner) {
        if (winner == null) {
            return false;
        }

        Match match = matches.get(winner.getUniqueId());

        if (match == null || match.ended) {
            return false;
        }

        match.ended = true;

        Player player1 = Bukkit.getPlayer(match.player1);
        Player player2 = Bukkit.getPlayer(match.player2);

        if (player1 != null) {
            matches.remove(player1.getUniqueId());
            match.state1.restore(player1);
            player1.sendMessage("§aانتهت المبارزة!");
            player1.sendMessage("§6الفائز: §e" + winner.getName());
        }

        if (player2 != null) {
            matches.remove(player2.getUniqueId());
            match.state2.restore(player2);
            player2.sendMessage("§aانتهت المبارزة!");
            player2.sendMessage("§6الفائز: §e" + winner.getName());
        }

        arenaManager.setBusy(match.arena, false);

        return true;
    }

    public boolean handleQuit(Player player) {
        Match match = matches.get(player.getUniqueId());

        if (match == null) {
            return false;
        }

        UUID opponentId = match.player1.equals(player.getUniqueId())
                ? match.player2 : match.player1;

        Player opponent = Bukkit.getPlayer(opponentId);

        if (opponent != null) {
            endDuel(opponent);
        } else {
            match.ended = true;
            matches.remove(match.player1);
            matches.remove(match.player2);
            arenaManager.setBusy(match.arena, false);
        }

        return true;
    }

    public boolean isInDuel(UUID player) {
        return matches.containsKey(player);
    }

    public boolean isInDuel(Player player) {
        return player != null && isInDuel(player.getUniqueId());
    }

    public String getArena(Player player) {
        Match match = player == null
                ? null : matches.get(player.getUniqueId());

        return match == null ? null : match.arena;
    }

    public String getKit(Player player) {
        Match match = player == null
                ? null : matches.get(player.getUniqueId());

        return match == null ? null : match.kit;
    }

    private String findAvailableArena(String kit) {
        for (String arena : arenaManager.getArenaNames()) {
            if (!arenaManager.isEnabled(arena)
                    || !arenaManager.isReady(arena)
                    || arenaManager.isBusy(arena)) {
                continue;
            }

            if (arenaManager.getArenaKits(arena).contains(
                    kit.toLowerCase()
            )) {
                return arena;
            }
        }

        return null;
    }

    private static final class DuelRequest {

        private final UUID sender;
        private final String kit;

        private DuelRequest(UUID sender, String kit) {
            this.sender = sender;
            this.kit = kit;
        }
    }

    private static final class Match {

        private final UUID player1;
        private final UUID player2;
        private final String arena;
        private final String kit;
        private final PlayerState state1;
        private final PlayerState state2;
        private boolean ended;

        private Match(
                UUID player1,
                UUID player2,
                String arena,
                String kit,
                PlayerState state1,
                PlayerState state2
        ) {
            this.player1 = player1;
            this.player2 = player2;
            this.arena = arena;
            this.kit = kit;
            this.state1 = state1;
            this.state2 = state2;
        }
    }

    private static final class PlayerState {

        private final Location location;
        private final ItemStack[] contents;
        private final ItemStack[] armor;
        private final ItemStack[] extra;
        private final int level;
        private final float exp;
        private final int food;
        private final float saturation;
        private final GameMode gameMode;

        private PlayerState(Player player) {
            location = player.getLocation().clone();
            contents = player.getInventory().getStorageContents().clone();
            armor = player.getInventory().getArmorContents().clone();
            extra = player.getInventory().getExtraContents().clone();
            level = player.getLevel();
            exp = player.getExp();
            food = player.getFoodLevel();
            saturation = player.getSaturation();
            gameMode = player.getGameMode();

            cloneItems(contents);
            cloneItems(armor);
            cloneItems(extra);
        }

        private void restore(Player player) {
            player.getInventory().clear();
            player.getInventory().setStorageContents(contents);
            player.getInventory().setArmorContents(armor);
            player.getInventory().setExtraContents(extra);

            player.setLevel(level);
            player.setExp(exp);
            player.setFoodLevel(food);
            player.setSaturation(saturation);
            player.setGameMode(gameMode);

            if (location.getWorld() != null) {
                player.teleport(location);
            }

            player.updateInventory();
        }

        private void cloneItems(ItemStack[] items) {
            for (int i = 0; i < items.length; i++) {
                if (items[i] != null) {
                    items[i] = items[i].clone();
                }
            }
        }
    }
          }
