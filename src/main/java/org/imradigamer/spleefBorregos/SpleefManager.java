package org.imradigamer.spleefBorregos;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SpleefManager {

    private final JavaPlugin plugin;
    private final List<SpleefLobby> lobbies = new ArrayList<>();
    private int lobbyCount = 0; // Counter for unique lobby world names

    public SpleefManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void joinLobby(Player player) {
        // Attempt to find an existing lobby
        for (SpleefLobby lobby : lobbies) {
            if (!lobby.isFull() && !lobby.isGameActive()) {
                lobby.addPlayer(player);
                if (lobby.getPlayers().size() >= 2) {
                    lobby.startCountdown();
                }
                return;
            }
        }

        // No available lobby found, create a new one
        createNewLobby(player);
    }

    private void createNewLobby(Player player) {
        player.sendMessage("Creando un nuevo lobby de Spleef...");

        String baseWorldName = "SpleefArena"; // Base arena world
        String newWorldName = "SpleefLobby_" + (++lobbyCount);

        if (!cloneWorld(baseWorldName, newWorldName)) {
            player.sendMessage("No se pudo crear un nuevo lobby. Por favor, contacta al administrador.");
            return;
        }

        World newWorld = Bukkit.getWorld(newWorldName);
        if (newWorld == null) {
            player.sendMessage("No se pudo cargar el mundo del nuevo lobby: " + newWorldName);
            return;
        }

        SpleefLobby newLobby = new SpleefLobby(plugin, this, newWorld);
        lobbies.add(newLobby);

        newLobby.addPlayer(player);
        newLobby.sendMessageToLobby("Bienvenido al nuevo lobby. Esperando más jugadores...");
    }

    private boolean cloneWorld(String baseWorldName, String newWorldName) {
        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv clone " + baseWorldName + " " + newWorldName);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("No se pudo clonar el mundo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void removeLobby(SpleefLobby lobby) {
        lobbies.remove(lobby);

        String worldName = lobby.getWorld().getName();

        if (Bukkit.unloadWorld(worldName, false)) {
            File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
            if (worldFolder.exists()) {
                deleteDirectory(worldFolder);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"mv remove "+ worldName);
            }
        } else {
            plugin.getLogger().warning("No se pudo descargar el mundo: " + worldName);
        }
    }

    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                deleteDirectory(file);
            }
        }
        directory.delete();
    }
    public List<SpleefLobby> getLobbies() {
        return lobbies;
    }

}
