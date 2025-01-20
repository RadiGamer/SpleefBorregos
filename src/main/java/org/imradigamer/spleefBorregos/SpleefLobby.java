package org.imradigamer.spleefBorregos;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class SpleefLobby {

    private static final String PREFIX = ChatColor.DARK_AQUA + "[BORREGOS] " + ChatColor.RESET;
    private final JavaPlugin plugin;
    private final SpleefManager spleefManager;
    private final World world;
    private final List<Player> players = new ArrayList<>();
    private boolean gameActive = false;
    private boolean countdownActive = false; // New flag

    public SpleefLobby(JavaPlugin plugin, SpleefManager spleefManager, World world) {
        this.plugin = plugin;
        this.spleefManager = spleefManager;
        this.world = world;
    }

    public void addPlayer(Player player) {
        players.add(player);
        player.teleport(world.getSpawnLocation());
        sendMessageToLobby(player.getName() + " se ha unido al lobby.");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        checkPlayersForCountdown();
    }

    public boolean isFull() {
        return players.size() >= 10; // Example maximum players
    }

    public boolean isGameActive() {
        return gameActive;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public World getWorld() {
        return world;
    }

    private void checkPlayersForCountdown() {
        if (players.size() >= 3 && !gameActive && !countdownActive) {
            sendMessageToLobby("Hay suficientes jugadores. El juego comenzará pronto...");
            startCountdown();
        } else if (!gameActive && !countdownActive) {
            sendMessageToLobby("Esperando a más jugadores para comenzar...");
        }
    }

    public void startCountdown() {
        if (gameActive || players.size() < 2 || countdownActive) return;

        countdownActive = true; // Mark the countdown as active
        sendMessageToLobby("El lobby está listo. El juego comenzará en 30 segundos...");
        for (Player player : players) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        }
        Bukkit.getScheduler().runTaskLater(plugin, this::startGame, 30 * 20L);
    }

    private void startGame() {
        if (gameActive) return;

        sendMessageToLobby("¡El juego está comenzando!");
        for (Player player : players) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.8f);
        }
        gameActive = true;
        countdownActive = false; // Reset the countdown flag

        // Teleport all players to the starting position
        Location gameStartLocation = new Location(world, -3, 95, 16);
        for (Player player : players) {
            player.teleport(gameStartLocation);
            giveUnbreakableShovel(player);
            player.setGameMode(GameMode.ADVENTURE);
        }

        // Example: End the game after 5 minutes
        Bukkit.getScheduler().runTaskLater(plugin, this::endLobby, 5 * 60 * 20L);
    }

    public void endLobby() {
        sendMessageToLobby("El juego ha terminado. Regresando al mundo de spawn en 10 segundos...");

        // Remove shovels from players
        for (Player player : players) {
            removeShovel(player);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            World spawnWorld = Bukkit.getWorld("World");
            if (spawnWorld == null) {
                plugin.getLogger().severe("¡El mundo de spawn 'World' no está cargado!");
                return;
            }

            Location spawnLocation = spawnWorld.getSpawnLocation();
            for (Player player : players) {
                player.getInventory().clear();
                player.teleport(spawnLocation);
                player.sendMessage(PREFIX + ChatColor.GREEN + "Has sido enviado al mundo de spawn.");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            }

            players.clear();

            spleefManager.removeLobby(this);
            gameActive = false; // Reset the game state
            countdownActive = false; // Reset the countdown flag
        }, 10 * 20L);
    }

    public void sendMessageToLobby(String message) {
        for (Player player : players) {
            player.sendMessage(PREFIX + ChatColor.YELLOW + message);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }
    }

    private void giveUnbreakableShovel(Player player) {
        ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL);
        ItemMeta meta = shovel.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);
            shovel.setItemMeta(meta);
        }
        player.getInventory().addItem(shovel);
        player.sendMessage(PREFIX + ChatColor.AQUA + "¡Has recibido una pala inrompible!");
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1.0f, 1.5f);
    }

    private void removeShovel(Player player) {
        player.getInventory().remove(Material.DIAMOND_SHOVEL);
    }
}

