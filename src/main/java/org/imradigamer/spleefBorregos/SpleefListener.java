package org.imradigamer.spleefBorregos;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public class SpleefListener implements Listener {

    private final SpleefManager spleefManager;
    private static final String PREFIX = ChatColor.DARK_AQUA + "[BORREGOS] " + ChatColor.RESET;

    public SpleefListener(SpleefManager spleefManager) {
        this.spleefManager = spleefManager;
    }

    // Disable fall damage
    @EventHandler
    public void onPlayerFallDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    // Detect when a player falls below the arena level
    @EventHandler
    public void onPlayerFall(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();

        if (location.getY() < 50) {
            handlePlayerFall(player);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        SpleefLobby lobby = findPlayerLobby(player);
        if (!lobby.isGameActive()) {
            event.setCancelled(true);
            player.sendMessage(PREFIX + ChatColor.RED + "¡No puedes romper bloques antes de que el juego comience!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        Block block = event.getBlock();

        if (block.getType() == Material.SNOW_BLOCK) {
            event.setDropItems(false);
            player.getInventory().addItem(new ItemStack(Material.SNOWBALL, 4));
            block.setType(Material.AIR);
            player.playSound(player.getLocation(), Sound.BLOCK_SNOW_BREAK, 1.0f, 1.0f);
        }
    }

    private void handlePlayerFall(Player player) {
        // Find the lobby the player is in
        World spawnWorld = Bukkit.getWorld("World");
        Location spawnLocation = spawnWorld.getSpawnLocation();
        SpleefLobby lobby = findPlayerLobby(player);
        if (lobby == null) return; // Player not in any lobby

        if (!lobby.isGameActive()) {
            // Teleport back to lobby spawn if game hasn't started
            player.teleport(lobby.getWorld().getSpawnLocation());
            player.sendMessage(PREFIX + ChatColor.YELLOW + "Has sido teletransportado al spawn del lobby.");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        } else {
            // Remove the player and teleport them to spectator area if game is active

            player.getInventory().clear(); // Remove items (e.g., shovel)
            player.teleport(spawnLocation);
            player.sendMessage(PREFIX + ChatColor.RED + "Has sido eliminado. ¡Ahora estás en modo espectador!");
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 1.0f);
            lobby.sendMessageToLobby(PREFIX + ChatColor.RED + player.getName() + " ha sido eliminado.");
            lobby.getPlayers().remove(player);

            // Check if the game should end
            if (lobby.getPlayers().size() <= 1) {
                lobby.endLobby();
            }
        }
    }

    @EventHandler
    public void onSnowballHitBlock(ProjectileHitEvent event) {
        // Check if the projectile is a snowball
        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.SNOWBALL) {
            return;
        }

        // Check if the snowball hit a block
        Block hitBlock = event.getHitBlock();
        if (hitBlock != null && isBreakable(hitBlock.getType())) {
            hitBlock.breakNaturally();
            Location blockLocation = hitBlock.getLocation();
            blockLocation.getWorld().playSound(blockLocation, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
        }
    }

    private boolean isBreakable(Material material) {
        // Define which block types the snowball can break
        return material == Material.SNOW_BLOCK || material == Material.ICE;
    }

    private SpleefLobby findPlayerLobby(Player player) {
        for (SpleefLobby lobby : spleefManager.getLobbies()) {
            if (lobby.getPlayers().contains(player)) {
                return lobby;
            }
        }
        return null;
    }
}
