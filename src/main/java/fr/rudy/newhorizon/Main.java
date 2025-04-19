package fr.rudy.newhorizon;

import fr.rudy.newhorizon.chat.Chat;
import fr.rudy.newhorizon.city.*;
import fr.rudy.newhorizon.commands.*;
import fr.rudy.newhorizon.economy.EconomyManager;
import fr.rudy.newhorizon.economy.VaultEconomy;
import fr.rudy.newhorizon.events.Events;
import fr.rudy.newhorizon.home.HomesManager;
import fr.rudy.newhorizon.level.LevelsManager;
import fr.rudy.newhorizon.level.PlayerListener;
import fr.rudy.newhorizon.placeholders.NewHorizonPlaceholder;
import fr.rudy.newhorizon.teleport.TPModule;
import fr.rudy.newhorizon.ui.CityGUIListener;
import fr.rudy.newhorizon.ui.MenuItemManager;
import fr.rudy.newhorizon.ui.TablistManager;
import fr.rudy.newhorizon.warp.WarpManager;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Main extends JavaPlugin implements Listener {

    private static Main instance;

    public static Main get() {
        return instance;
    }

    private Connection database;
    private HomesManager homesManager;
    private LevelsManager levelsManager;
    private EconomyManager economyManager;
    private WorldSpawnManager worldSpawnManager;
    private TablistManager tablistManager;
    private TPModule tpModule;
    private WarpManager warpManager;
    private CityManager cityManager;
    private ClaimManager claimManager;
    private CityBankManager cityBankManager;

    private String prefixError;
    private String prefixInfo;

    private final Map<UUID, String> pendingInvites = new HashMap<>();
    public Map<UUID, String> getPendingInvites() {
        return pendingInvites;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        setupDatabase();
        setupManagers();
        setupVault();

        // PlaceholderAPI
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            NewHorizonPlaceholder.registerExpansion();
        }

        // Listeners
        Bukkit.getPluginManager().registerEvents(new Events(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new MenuItemManager(this), this);
        Bukkit.getPluginManager().registerEvents(new CityChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new CityInviteListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChunkProtectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new CityGUIListener(), this);

        // Tablist
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            tablistManager = new TablistManager(this);
            tablistManager.start();
        }

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
        getCommand("city").setExecutor(new CityCommand());
        getCommand("cityadmin").setExecutor(new CityAdminCommand());
        getCommand("wiki").setExecutor(new WikiCommand());



        // Préfixes
        prefixError = getConfig().getString("general.prefixError", "&c[Erreur] ");
        prefixInfo = getConfig().getString("general.prefixInfo", "&a[Info] ");

        // Chargement automatique des mondes
        loadAllWorlds();

        getLogger().info("✅ NewHorizon plugin activé !");
    }

    private void setupDatabase() {
        try {
            database = DriverManager.getConnection("jdbc:sqlite:database.db");

            try (Statement statement = database.createStatement()) {
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS newhorizon_player_data (" +
                                "uuid VARCHAR(36) PRIMARY KEY, " +
                                "experience INT DEFAULT 0, " +
                                "money DOUBLE DEFAULT 0.0, " +
                                "home_world VARCHAR(64), " +
                                "home_x DOUBLE, home_y DOUBLE, home_z DOUBLE, " +
                                "home_yaw FLOAT, home_pitch FLOAT)"
                );

                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS newhorizon_world_spawns (" +
                                "world_name VARCHAR(64) PRIMARY KEY, " +
                                "x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT)"
                );

                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS newhorizon_cities (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "owner_uuid TEXT NOT NULL, " +
                                "city_name TEXT UNIQUE NOT NULL, " +
                                "world TEXT NOT NULL, " +
                                "x DOUBLE NOT NULL, y DOUBLE NOT NULL, z DOUBLE NOT NULL, " +
                                "yaw FLOAT, pitch FLOAT, " +
                                "likes INTEGER DEFAULT 0, " +
                                "liked_by TEXT DEFAULT '', " +
                                "members TEXT DEFAULT '', " +
                                "banner TEXT, " +
                                "bank_balance DOUBLE DEFAULT 0.0" +
                                ")"
                );


                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS newhorizon_city_claims (" +
                                "chunk_x INTEGER NOT NULL, " +
                                "chunk_z INTEGER NOT NULL, " +
                                "world TEXT NOT NULL, " +
                                "city_id INTEGER NOT NULL, " +
                                "PRIMARY KEY (chunk_x, chunk_z, world))"
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void setupManagers() {
        economyManager = new EconomyManager(database);
        economyManager.addMoneyColumnIfNotExists();

        homesManager = new HomesManager();
        levelsManager = new LevelsManager();
        warpManager = new WarpManager();
        warpManager.loadWarpsFromConfig();
        worldSpawnManager = new WorldSpawnManager(database);
        cityManager = new CityManager();
        claimManager = new ClaimManager();
        tpModule = new TPModule();
        cityBankManager = new CityBankManager();

        // LuckPerms
        LuckPerms luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        if (luckPerms == null) {
            getLogger().severe("❌ LuckPerms manquant !");
            Bukkit.getPluginManager().disablePlugin(this);
        } else {
            new Chat(this, luckPerms);
        }
    }

    private void loadAllWorlds() {
        File worldFolder = getServer().getWorldContainer();
        File[] files = worldFolder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && new File(file, "level.dat").exists()) {
                    String worldName = file.getName();
                    if (Bukkit.getWorld(worldName) == null) {
                        getLogger().info("🔄 Chargement du monde: " + worldName);
                        new WorldCreator(worldName).createWorld();
                    }
                }
            }
        }
    }

    private void setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            getServer().getServicesManager().register(
                    Economy.class,
                    new VaultEconomy(economyManager),
                    this,
                    ServicePriority.Normal
            );
            getLogger().info("✅ Économie intégrée à Vault !");
        } else {
            getLogger().warning("⚠️ Vault manquant !");
        }
    }

    @Override
    public void onDisable() {
        try {
            if (database != null && !database.isClosed()) {
                database.close();
            }
        } catch (SQLException ignored) {}

        getLogger().info("🛑 Plugin NewHorizon désactivé proprement.");
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

    public WorldSpawnManager getWorldSpawnManager() {
        return worldSpawnManager;
    }

    public CityManager getCityManager() {
        return cityManager;
    }

    public ClaimManager getClaimManager() {
        return claimManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public CityBankManager getCityBankManager() {
        return cityBankManager;
    }

}