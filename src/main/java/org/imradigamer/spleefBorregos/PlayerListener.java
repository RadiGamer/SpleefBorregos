package org.imradigamer.spleefBorregos;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private final GameManager gameManager;

    public PlayerListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!gameManager.isGameRunning()) return;

        Player player = event.getPlayer();
        gameManager.checkForElimination(player);
    }
}
