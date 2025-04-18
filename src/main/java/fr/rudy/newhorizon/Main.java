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
import fr.rudy.newhorizon.ui.TablistManager;

import fr.rudy.newhorizon.world.WorldSpawnManager;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
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
    private WorldSpawnManager worldSpawnManager;

    private TablistManager tablistManager;

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
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS newhorizon_world_spawns (" +
                                "world_name VARCHAR(64) PRIMARY KEY, " +
                                "x DOUBLE, " +
                                "y DOUBLE, " +
                                "z DOUBLE, " +
                                "yaw FLOAT, " +
                                "pitch FLOAT" +
                                ")"
                );
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialiser EconomyManager AVANT setupVault()
        economyManager = new EconomyManager(database);
        economyManager.addMoneyColumnIfNotExists();

        // Enregistrement de VaultEconomy dans Vault
        setupVault();

        // Charger LuckPerms
        LuckPerms luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        if (luckPerms == null) {
            getLogger().severe("LuckPerms n'est pas installé ! Le plugin ne fonctionnera pas correctement.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialiser les autres modules
        homesManager = new HomesManager();
        levelsManager = new LevelsManager();
        warpManager = new WarpManager();
        warpManager.loadWarpsFromConfig();
        worldSpawnManager = new WorldSpawnManager(database);

        // Gestion du chat avec LuckPerms
        new Chat(this, luckPerms);

        // PlaceholderAPI
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new LevelPlaceholder().register();
        } else {
            getLogger().warning("PlaceholderAPI non détecté. Les placeholders ne fonctionneront pas.");
        }

        // Événements et UI
        Bukkit.getPluginManager().registerEvents(new Events(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new MenuItemManager(this), this);

        // TabList
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            tablistManager = new TablistManager(this);
            tablistManager.start();
        } else {
            getLogger().warning("PlaceholderAPI est requis pour la TabList dynamique.");
        }

        // Téléportation
        tpModule = new TPModule();

        // Commandes
        getCommand("level").setExecutor(new LevelCommand());
        getCommand("tpa").setExecutor(new TeleportCommands(tpModule));
        getCommand("tpaccept").setExecutor(new TeleportCommands(tpModule));
        getCommand("tpdeny").setExecutor(new TeleportCommands(tpModule));
        getCommand("tptoggle").setExecutor(new TeleportCommands(tpModule));
        getCommand("event").setExecutor(new EventsCommand());
        getCommand("sethome").setExecutor(new HomeCommand());
        getCommand("home").setExecutor(new HomeCommand());
        getCommand("warp").setExecutor(new WarpCommand(warpManager));
        getCommand("coins").setExecutor(new CoinsCommand(economyManager));
        getCommand("world").setExecutor(new WorldCommand());

        // Préfixes
        prefixError = getConfig().getString("general.prefixError", "&c[Erreur] ");
        prefixInfo = getConfig().getString("general.prefixInfo", "&a[Info] ");

        // Chargement automatique des mondes
        File worldFolder = getServer().getWorldContainer();
        File[] files = worldFolder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && new File(file, "level.dat").exists()) {
                    String worldName = file.getName();

                    // Ne recharge pas les mondes déjà chargés
                    if (Bukkit.getWorld(worldName) == null) {
                        getLogger().info("🔄 Chargement du monde: " + worldName);
                        WorldCreator creator = new WorldCreator(worldName);
                        creator.createWorld();
                    }
                }
            }
        }

        getLogger().info("✅ NewHorizon plugin activé avec succès !");

    }

    @Override
    public void onDisable() {
        // Déconnexion propre
        try {
            if (database != null && !database.isClosed()) {
                database.close();
            }
        } catch (SQLException ignored) {}

        getLogger().info("🛑 NewHorizon plugin désactivé proprement.");
    }

    private void setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            if (economyManager == null) {
                getLogger().severe("❌ economyManager est NULL au moment de setupVault !");
            }

            getServer().getServicesManager().register(
                    Economy.class,
                    new VaultEconomy(economyManager),
                    this,
                    ServicePriority.Normal
            );

            getLogger().info("✅ Système d'économie enregistré dans Vault !");
        } else {
            getLogger().warning("⚠️ Vault n'est pas installé. L'économie ne fonctionnera pas correctement.");
        }
    }

    // Getters
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

    public WorldSpawnManager getWorldSpawnManager() { return worldSpawnManager; }
}