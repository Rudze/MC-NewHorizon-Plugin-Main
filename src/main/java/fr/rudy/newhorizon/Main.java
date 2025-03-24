package fr.rudy.newhorizon;

import fr.rudy.newhorizon.chat.Chat;
import fr.rudy.newhorizon.commands.*;
import fr.rudy.newhorizon.config.ConfigManager;
import fr.rudy.newhorizon.economy.EconomyManager;
import fr.rudy.newhorizon.economy.VaultEconomy;
import fr.rudy.newhorizon.events.Events;
import fr.rudy.newhorizon.home.HomesManager;
import fr.rudy.newhorizon.level.LevelsManager;
import fr.rudy.newhorizon.level.PlayerListener;
import fr.rudy.newhorizon.placeholders.LevelPlaceholder;
import fr.rudy.newhorizon.teleport.TPModule;
import fr.rudy.newhorizon.ui.MenuItemManager;
import fr.rudy.newhorizon.utils.LevelCalculator;
import fr.rudy.newhorizon.warp.WarpManager;

import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Main extends JavaPlugin implements Listener {
    private static Main instance = null;

    public static Main get() {
        return instance;
    }

    private Connection database;
    private HomesManager homesManager;
    private LevelsManager levelsManager;
    private EconomyManager economyManager;

    private String prefixError;
    private String prefixInfo;
    private TPModule tpModule;
    private WarpManager warpManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialisation de la configuration
        saveDefaultConfig();

        // Initialisation de la base de données
        try {
            database = DriverManager.getConnection("jdbc:sqlite:database.db");

            try (Statement statement = database.createStatement()) {
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS newhorizon_player_data (" +
                                "uuid VARCHAR(36) PRIMARY KEY, " +
                                "experience INT DEFAULT 0, " +
                                "money DOUBLE DEFAULT 0.0, " +
                                "home_world VARCHAR(64), " +
                                "home_x DOUBLE, " +
                                "home_y DOUBLE, " +
                                "home_z DOUBLE, " +
                                "home_yaw FLOAT, " +
                                "home_pitch FLOAT" +
                                ")"
                );
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Charger LuckPerms
        LuckPerms luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        if (luckPerms == null) {
            getLogger().severe("LuckPerms n'est pas installé ! Le plugin ne fonctionnera pas correctement.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialiser EconomyManager
        economyManager = new EconomyManager(database);
        economyManager.addMoneyColumnIfNotExists();

        // Setup Vault
        setupVault();

        // Initialiser HomeManager et WarpManager
        homesManager = new HomesManager();
        levelsManager = new LevelsManager();
        warpManager = new WarpManager();
        warpManager.loadWarpsFromConfig();

        // Initialiser le gestionnaire de chat
        new Chat(this, luckPerms);

        // Vérifier si PlaceholderAPI est installé
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new LevelPlaceholder().register();
        } else {
            getLogger().warning("PlaceholderAPI non détecté. Les placeholders ne fonctionneront pas.");
        }

        // Enregistrer les événements et les commandes
        Bukkit.getPluginManager().registerEvents(new Events(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new MenuItemManager(this), this);

        tpModule = new TPModule();

        getCommand("level").setExecutor(new LevelCommand());
        getCommand("tpa").setExecutor(new TeleportCommands(tpModule));
        getCommand("tpaccept").setExecutor(new TeleportCommands(tpModule));
        getCommand("tpdeny").setExecutor(new TeleportCommands(tpModule));
        getCommand("tptoggle").setExecutor(new TeleportCommands(tpModule));
        getCommand("event").setExecutor(new EventsCommand());
        getCommand("sethome").setExecutor(new HomeCommand());
        getCommand("home").setExecutor(new HomeCommand());
        getCommand("warp").setExecutor(new WarpCommand(warpManager));

        // Enregistrement de la commande /coins
        getCommand("coins").setExecutor(new CoinsCommand(economyManager));

        // Charger les préfixes depuis la configuration
        prefixError = getConfig().getString("general.prefixError", "&c[Erreur] ");
        prefixInfo = getConfig().getString("general.prefixInfo", "&a[Info] ");

        getLogger().info("NewHorizon plugin activé avec succès !");
    }

    @Override
    public void onDisable() {
        // Déconnexion de la base
        try {
            if (database != null && !database.isClosed()) {
                database.close();
            }
        } catch (SQLException ignored) {}

        getLogger().info("NewHorizon plugin désactivé proprement.");
    }

    private void setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            getServer().getServicesManager().register(
                    Economy.class,
                    new VaultEconomy(economyManager),
                    this,
                    ServicePriority.Normal
            );
        } else {
            getLogger().warning("Vault n'est pas installé. L'économie ne fonctionnera pas correctement.");
        }
    }

    public String getPrefixError() {
        return prefixError;
    }

    public String getPrefixInfo() {
        return prefixInfo;
    }

    public Connection getDatabase() {
        return database;
    }

    public HomesManager getHomesManager() {
        return homesManager;
    }

    public LevelsManager getLevelsManager() {
        return levelsManager;
    }
}
