package fr.rudy.newhorizon;

import fr.rudy.newhorizon.chat.Chat;
import fr.rudy.newhorizon.commands.EventsCommand;
import fr.rudy.newhorizon.commands.HomeCommand;
import fr.rudy.newhorizon.commands.LevelCommand;
import fr.rudy.newhorizon.commands.TeleportCommands;
import fr.rudy.newhorizon.config.ConfigManager;
import fr.rudy.newhorizon.events.Events;
import fr.rudy.newhorizon.level.PlayerListener;
import fr.rudy.newhorizon.placeholders.LevelPlaceholder;
import fr.rudy.newhorizon.teleport.TPModule;
import fr.rudy.newhorizon.utils.DatabaseManager;
import fr.rudy.newhorizon.utils.LevelCalculator;
import net.luckperms.api.LuckPerms;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Main extends JavaPlugin implements Listener {

    private static Main instance = null;

    public static Main getInstance() {
        return instance;
    }

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private String prefixError;
    private String prefixInfo;
    private TPModule tpModule;

    // Maps pour les niveaux et l'expérience des joueurs
    private final HashMap<UUID, Integer> playerLevels = new HashMap<>();
    private final HashMap<UUID, Integer> playerExp = new HashMap<>();

    // Map pour les exigences d'expérience par niveau
    private Map<Integer, Integer> levelRequirements;

    @Override
    public void onEnable() {
        instance = this;

        // Initialisation de la configuration
        saveDefaultConfig();
        configManager = new ConfigManager();

        // Charger les configurations de niveaux
        int initialExp = configManager.getConfig().getInt("leveling.initial_exp", 100);
        double incrementPercent = configManager.getConfig().getDouble("leveling.exp_increment_percent", 30);
        int maxLevel = configManager.getConfig().getInt("leveling.max_level", 100);

        // Calculer les exigences d'expérience
        levelRequirements = LevelCalculator.calculateLevelRequirements(initialExp, incrementPercent, maxLevel);

        // Initialisation de la base de données
        String host = configManager.getConfig().getString("database.host");
        int port = configManager.getConfig().getInt("database.port");
        String username = configManager.getConfig().getString("database.username");
        String password = configManager.getConfig().getString("database.password");
        String databaseName = configManager.getConfig().getString("database.database");

        databaseManager = new DatabaseManager(host, port, username, password, databaseName);
        databaseManager.connect();

        // Charger les données des joueurs
        databaseManager.loadPlayerData(playerLevels, playerExp);

        // Charger la configuration
        saveDefaultConfig();

        // Charger LuckPerms
        LuckPerms luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        if (luckPerms == null) {
            getLogger().severe("LuckPerms n'est pas installé ! Le plugin ne fonctionnera pas correctement.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialiser le gestionnaire de chat
        new Chat(this, luckPerms);


        // Vérifier si PlaceholderAPI est installé
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new LevelPlaceholder(playerLevels, playerExp).register();
            /*getLogger().info("PlaceholderAPI détecté et intégré avec succès !");*/
        } else {
            getLogger().warning("PlaceholderAPI non détecté. Les placeholders ne fonctionneront pas.");
        }

        // Enregistrer les événements et les commandes
        getServer().getPluginManager().registerEvents(new Events(), this);

        getServer().getPluginManager().registerEvents(new PlayerListener(playerLevels, playerExp, databaseManager, levelRequirements), this);
        tpModule = new TPModule();

        getCommand("level").setExecutor(new LevelCommand(this, playerLevels, playerExp));
        getCommand("tpa").setExecutor(new TeleportCommands(tpModule));
        getCommand("tpaccept").setExecutor(new TeleportCommands(tpModule));
        getCommand("tpdeny").setExecutor(new TeleportCommands(tpModule));
        getCommand("tptoggle").setExecutor(new TeleportCommands(tpModule));
        getCommand("event").setExecutor(new EventsCommand());
        getCommand("sethome").setExecutor(new HomeCommand(databaseManager, this));
        getCommand("home").setExecutor(new HomeCommand(databaseManager, this));



        // Charger les préfixes depuis la configuration
        prefixError = getConfig().getString("general.prefixError", "&c[Erreur] ");
        prefixInfo = getConfig().getString("general.prefixInfo", "&a[Info] ");

        getLogger().info("NewHorizon plugin activé avec succès !");
    }

    public String getPrefixError() {
        return prefixError;
    }

    public String getPrefixInfo() {
        return prefixInfo;
    }

    @Override
    public void onDisable() {
        // Sauvegarder les données des joueurs
        databaseManager.savePlayerData(playerLevels, playerExp);
        databaseManager.disconnect();

        getLogger().info("NewHorizon plugin désactivé proprement.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
