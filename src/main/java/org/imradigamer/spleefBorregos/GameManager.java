package org.imradigamer.spleefBorregos;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class GameManager {

    private final SpleefBorregos plugin;
    private boolean gameRunning = false;
    private final Set<Player> players = new HashSet<>();

    public GameManager(SpleefBorregos plugin) {
        this.plugin = plugin;
    }

    public void startGame() {
        if (!gameRunning) {
            gameRunning = true;
            players.clear();
            players.addAll(Bukkit.getOnlinePlayers());
            for (Player player : players) {
                player.setGameMode(GameMode.SURVIVAL);
                player.getInventory().addItem(new ItemStack(Material.GOLDEN_SHOVEL));
            }
        }
    }

    public void stopGame() {
        if (gameRunning) {
            gameRunning = false;
            resetPlayers();
        }
    }

    public void checkForElimination(Player player) {
        if (player.getLocation().getY() < 50) {
            player.teleport(player.getWorld().getHighestBlockAt(0, 170).getLocation());
            Bukkit.broadcastMessage(player.getName() + " has been eliminated!");
            players.remove(player);
            checkForWinner();
        }
    }

    private void resetPlayers() {
        for (Player player : players) {
            player.setGameMode(GameMode.SURVIVAL);
        }
        players.clear();
    }

    private void checkForWinner() {
        if (players.size() == 1) {
            Player winner = players.iterator().next();
            Bukkit.broadcastMessage(winner.getName() + " has won the Spleef game!");
            stopGame();
        }
    }

    public boolean isGameRunning() {
        return gameRunning;
    }
}