package org.imradigamer.spleefBorregos;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpleefCommand implements CommandExecutor {

    private final SpleefManager spleefManager;

    public SpleefCommand(SpleefManager spleefManager) {
        this.spleefManager = spleefManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("spleef")) {
            // Ensure the command is executed by a player
            if (!(sender instanceof Player)) {
                sender.sendMessage("§c¡Este comando solo puede ser ejecutado por jugadores!");
                return true;
            }

            Player player = (Player) sender;

            // Join the player to the Spleef lobby
            spleefManager.joinLobby(player);
            player.sendMessage("§a¡Te has unido al lobby de Spleef!");

            return true;
        }

        return false;
    }
}
