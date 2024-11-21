package fr.rudy.newhorizon.config;

import fr.rudy.newhorizon.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    // Obtenir la configuration de config.yml
    public FileConfiguration getConfig() {
        return Main.getInstance().getConfig();
    }

    // Sauvegarder les modifications de config.yml
    public void saveConfig() {
        Main.getInstance().saveConfig();
    }

    // Recharger config.yml
    public void reloadConfig() {
        Main.getInstance().reloadConfig();
    }
}
