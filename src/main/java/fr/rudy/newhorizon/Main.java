package fr.rudy.newhorizon;

import fr.rudy.newhorizon.chat.Chat;
import fr.rudy.newhorizon.commands.EventsCommand;
import fr.rudy.newhorizon.commands.HomeCommand;
import fr.rudy.newhorizon.commands.LevelCommand;
import fr.rudy.newhorizon.commands.TeleportCommands;
import fr.rudy.newhorizon.config.ConfigManager;
import fr.rudy.newhorizon.events.Events;
import fr.rudy.newhorizon.home.HomesManager;
import fr.rudy.newhorizon.level.LevelsManager;
import fr.rudy.newhorizon.level.PlayerListener;
import fr.rudy.newhorizon.placeholders.LevelPlaceholder;
import fr.rudy.newhorizon.teleport.TPModule;
import fr.rudy.newhorizon.utils.LevelCalculator;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Main extends JavaPlugin implements Listener {
    private static Main instance = null;

    public static Main get() {
        return instance;
    }

    private Connection database;
    private HomesManager homesManager;
    private LevelsManager levelsManager;

    private String prefixError;
    private String prefixInfo;
    private TPModule tpModule;

    @Override
    public void onEnable() {
        instance = this;

        // Initialisation de la configuration
        saveDefaultConfig();

        // Charger les configurations de niveaux
        //int initialExp = getConfig().getInt("leveling.initial_exp", 100);
        //double incrementPercent = getConfig().getDouble("leveling.exp_increment_percent", 30);
        //int maxLevel = getConfig().getInt("leveling.max_level", 100);

        // Calculer les exigences d'expérience
        //levelRequirements = LevelCalculator.calculateLevelRequirements(initialExp, incrementPercent, maxLevel);

        // Initialisation de la base de données
        try {
            database = DriverManager.getConnection(
                    "jdbc:mysql://" + getConfig().getString("database.host") + ":" + getConfig().getInt("database.port") + "/" + getConfig().getString("database.database"),
                    getConfig().getString("database.username"),
                    getConfig().getString("database.password")
            );

            try (Statement statement = database.createStatement()) {
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS newhorizon_player_data (" +
                                "uuid VARCHAR(36) PRIMARY KEY, " +
                                "experience INT DEFAULT 0, " +
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
            //TODO: Message + Stacktrace
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

        homesManager = new HomesManager();
        levelsManager = new LevelsManager();

        // Initialiser le gestionnaire de chat
        new Chat(this, luckPerms);

        // Vérifier si PlaceholderAPI est installé
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new LevelPlaceholder().register();
            /*getLogger().info("PlaceholderAPI détecté et intégré avec succès !");*/
        } else {
            getLogger().warning("PlaceholderAPI non détecté. Les placeholders ne fonctionneront pas.");
        }

        // Enregistrer les événements et les commandes
        Bukkit.getPluginManager().registerEvents(new Events(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        tpModule = new TPModule();

        getCommand("level").setExecutor(new LevelCommand());
        getCommand("tpa").setExecutor(new TeleportCommands(tpModule));
        getCommand("tpaccept").setExecutor(new TeleportCommands(tpModule));
        getCommand("tpdeny").setExecutor(new TeleportCommands(tpModule));
        getCommand("tptoggle").setExecutor(new TeleportCommands(tpModule));
        getCommand("event").setExecutor(new EventsCommand());
        getCommand("sethome").setExecutor(new HomeCommand());
        getCommand("home").setExecutor(new HomeCommand());

        // Charger les préfixes depuis la configuration
        prefixError = getConfig().getString("general.prefixError", "&c[Erreur] ");
        prefixInfo = getConfig().getString("general.prefixInfo", "&a[Info] ");

        getLogger().info("NewHorizon plugin activé avec succès !");
    }

    @Override
    public void onDisable() {
        // Déconnexion de la base
        try {
            database.close();
            //TODO: Message
        } catch (SQLException ignored) {}

        getLogger().info("NewHorizon plugin désactivé proprement.");
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
