package org.imradigamer.spleefBorregos;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpleefBorregos extends JavaPlugin {

    private GameManager gameManager;
    private SpleefManager spleefManager;

    @Override
    public void onEnable() {
        spleefManager = new SpleefManager(this);
        this.getCommand("spleef").setExecutor(new SpleefCommand(spleefManager));
        Bukkit.getPluginManager().registerEvents(new SpleefListener(spleefManager), this);
        getLogger().info("Spleef Plugin Enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("Spleef Plugin Disabled");
    }
}
