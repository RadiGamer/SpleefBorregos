package org.imradigamer.spleefBorregos;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class SpleefLobby implements Listener {

    private static final String PREFIX = ChatColor.DARK_AQUA + "[BORREGOS] " + ChatColor.RESET;
    private final JavaPlugin plugin;
    private final SpleefManager spleefManager;
    private final World world;
    private final List<Player> players = new ArrayList<>();
    private boolean gameActive = false;
    private boolean countdownActive = false; // New flag
    private BukkitTask countdownTask; // Stores countdown task for cancellation


    public SpleefLobby(JavaPlugin plugin, SpleefManager spleefManager, World world) {
        this.plugin = plugin;
        this.spleefManager = spleefManager;
        this.world = world;
    }

    public void addPlayer(Player player) {
        // Prevent adding the player twice
        if (players.contains(player)) {
            player.sendMessage(PREFIX + ChatColor.RED + " Ya estás en la partida.");
            return;
        }

        players.add(player);
        player.teleport(world.getSpawnLocation());
        sendMessageToLobby(player.getName() + " se ha unido al lobby.");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        checkPlayersForCountdown(); // Ensure countdown logic works
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
        if (players.size() >= 2 && !gameActive && !countdownActive) { // Start countdown if 2 or more
            sendMessageToLobby("Hay suficientes jugadores. El juego comenzará pronto...");
            startCountdown();
        } else if (players.size() < 2 && countdownActive) { // Cancel countdown if players < 2
            cancelCountdown();
            sendMessageToLobby(ChatColor.RED + "Demasiados jugadores han salido. La cuenta regresiva ha sido cancelada.");
        } else if (!gameActive && !countdownActive) {
            sendMessageToLobby("Esperando a más jugadores para comenzar...");
        }
    }



    public void startCountdown() {
        if (gameActive || players.size() < 2 || countdownActive) return;

        countdownActive = true;
        sendMessageToLobby("El lobby está listo. El juego comenzará en 30 segundos...");

        for (Player player : players) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        }

        countdownTask = new BukkitRunnable() {
            int timeLeft = 30; // Countdown time in seconds

            @Override
            public void run() {
                if (players.size() < 2) { // Cancel if too many players leave
                    cancelCountdown();
                    return;
                }

                // Send action bar update to all players
                for (Player player : players) {
                    sendActionBar(player, ChatColor.YELLOW + "Inicio en: " + ChatColor.RED + timeLeft + "s");
                }

                if (timeLeft <= 0) {
                    startGame();
                    this.cancel(); // Stop the countdown
                }

                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // Runs every second
    }
    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }



    private void startGame() {
        if (gameActive || players.size() < 2) {
            cancelCountdown(); // Prevents starting if players leave before the game starts
            return;
        }

        sendMessageToLobby("¡El juego está comenzando!");
        gameActive = true;
        countdownActive = false;

        Location gameStartLocation = new Location(world, -3, 95, 16);
        for (Player player : players) {
            player.teleport(gameStartLocation);
            giveUnbreakableShovel(player);
            player.setGameMode(GameMode.SURVIVAL);
        }


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
    private void giveExitItem(Player player) {
        ItemStack exitItem = new ItemStack(Material.RED_BED);
        ItemMeta meta = exitItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "Salir");
            exitItem.setItemMeta(meta);
        }
        player.getInventory().setItem(8, exitItem); // Slot 9 (Index 8)
    }


    public void removePlayer(Player player) {
        if (!players.contains(player)) return;

        players.remove(player);
        player.getInventory().clear();
        teleportToSpawn(player);

        sendMessageToLobby(ChatColor.RED + player.getName() + " ha salido del juego.");

        // If there are fewer than 2 players left, cancel the countdown
        checkPlayersForCountdown();
    }

    private void teleportToSpawn(Player player) {
        World spawnWorld = Bukkit.getWorld("World");
        if (spawnWorld == null) {
            plugin.getLogger().severe("¡El mundo de spawn 'World' no está cargado!");
            return;
        }
        player.teleport(spawnWorld.getSpawnLocation());
        player.sendMessage(PREFIX + ChatColor.GREEN + "Has sido enviado al mundo de spawn.");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
    }


    private void cancelCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        countdownActive = false;
        sendMessageToLobby(ChatColor.RED + "La cuenta regresiva ha sido cancelada.");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if the player is holding the "Salir" bed
        if (item != null && item.getType() == Material.RED_BED) {
            ItemMeta meta = item.getItemMeta();

            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(ChatColor.RED + "Salir")) {
                event.setCancelled(true); // Prevent any interaction

                if (players.contains(player)) { // Ensure the player is in the game
                    sendMessageToLobby(ChatColor.RED + player.getName() + " ha salido del juego.");
                    removePlayer(player); // Remove from game and teleport to spawn
                    checkPlayersForCountdown(); // Cancel countdown if needed
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();

        if (item != null && item.getType() == Material.RED_BED) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (players.contains(player)) {
            sendMessageToLobby(ChatColor.RED + player.getName() + " se ha desconectado del juego.");
            removePlayer(player); // Fully remove the player
            checkPlayersForCountdown(); // Ensure countdown cancels if needed
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        teleportToSpawn(player);
    }



    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (players.contains(player)) {
                event.setCancelled(true);
            }
        }
    }



}

