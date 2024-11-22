package fr.rudy.newhorizon.level;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.utils.DatabaseManager;
import fr.rudy.newhorizon.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final HashMap<UUID, Integer> playerLevels;
    private final HashMap<UUID, Integer> playerExp;
    private final DatabaseManager databaseManager;
    private final Map<Integer, Integer> levelRequirements;
    private final String RESOURCE_WORLD_NAME = "world_resource";

    public PlayerListener(HashMap<UUID, Integer> playerLevels, HashMap<UUID, Integer> playerExp, DatabaseManager databaseManager, Map<Integer, Integer> levelRequirements) {
        this.playerLevels = playerLevels;
        this.playerExp = playerExp;
        this.databaseManager = databaseManager;
        this.levelRequirements = levelRequirements;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        Main plugin = Main.getInstance();
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Récupérer l'instance du monde "world_resource"
        World newhorizonWorld = Main.getInstance().getServer().getWorld("world_newhorizon");

        // Vérifier si le joueur est dans le monde "world_resource"
        World playerWorld = player.getWorld();

        // Debug : Afficher le monde actuel du joueur
        /*player.sendMessage("§e[Debug] Vous êtes actuellement dans le monde : " + playerWorld.getName());*/

        // Vérification du monde
        if (newhorizonWorld == null || playerWorld.equals(newhorizonWorld)) {
            /*player.sendMessage("§c[Debug] Vous n'êtes pas dans le monde 'world_resource', aucun XP n'a été gagné.");*/
            return; // Sortir si le joueur n'est pas dans le bon monde
        }


        // Initialiser les variables du joueur
        int currentLevel = playerLevels.getOrDefault(uuid, 1);
        int currentExp = playerExp.getOrDefault(uuid, 0);
        int expToAdd = 0;

        // Vérifier le type de bloc cassé et déterminer l'expérience à ajouter
        Material blockType = event.getBlock().getType();
        if (blockType.toString().endsWith("_LOG")) {
            expToAdd = 1;

        //Minerait
        } else if (blockType == Material.COAL_ORE) {
            expToAdd = 1;
        } else if (blockType == Material.DEEPSLATE_COAL_ORE) {
            expToAdd = 1;
        } else if (blockType == Material.IRON_ORE) {
            expToAdd = 3;
        } else if (blockType == Material.DEEPSLATE_IRON_ORE) {
            expToAdd = 3;
        } else if (blockType == Material.COPPER_ORE) {
            expToAdd = 2;
        } else if (blockType == Material.DEEPSLATE_COPPER_ORE) {
            expToAdd = 2;
        } else if (blockType == Material.GOLD_ORE) {
            expToAdd = 4;
        } else if (blockType == Material.DEEPSLATE_GOLD_ORE) {
            expToAdd = 4;
        } else if (blockType == Material.REDSTONE_ORE) {
            expToAdd = 1;
        } else if (blockType == Material.DEEPSLATE_REDSTONE_ORE) {
            expToAdd = 1;
        } else if (blockType == Material.EMERALD_ORE) {
            expToAdd = 5;
        } else if (blockType == Material.DEEPSLATE_EMERALD_ORE) {
            expToAdd = 5;
        } else if (blockType == Material.LAPIS_ORE) {
            expToAdd = 1;
        } else if (blockType == Material.DEEPSLATE_LAPIS_ORE) {
            expToAdd = 1;
        } else if (blockType == Material.DIAMOND_ORE) {
            expToAdd = 15;
        } else if (blockType == Material.DEEPSLATE_DIAMOND_ORE) {
            expToAdd = 15;
        } else if (blockType == Material.NETHER_GOLD_ORE) {
            expToAdd = 1;
        } else if (blockType == Material.NETHER_QUARTZ_ORE) {
            expToAdd = 1;
        } else if (blockType == Material.ANCIENT_DEBRIS) {
            expToAdd = 35;
        } else if (blockType == Material.LAPIS_ORE) {
            expToAdd = 1;
        } else if (blockType == Material.LAPIS_ORE) {
            expToAdd = 1;




        //Agriculture
        } else if (blockType == Material.WHEAT) {
            if (event.getBlock().getBlockData().getAsString().contains("age=7")) {
                expToAdd = 1;
            }
        } else if (blockType == Material.CARROT) {
            if (event.getBlock().getBlockData().getAsString().contains("age=7")) {
                expToAdd = 1;
            }
        } else if (blockType == Material.POTATO) {
            if (event.getBlock().getBlockData().getAsString().contains("age=7")) {
                expToAdd = 1;
            }
        } else if (blockType == Material.BEETROOT) {
            if (event.getBlock().getBlockData().getAsString().contains("age=7")) {
                expToAdd = 1;
            }
        } else if (blockType == Material.LEGACY_NETHER_WARTS) {
            if (event.getBlock().getBlockData().getAsString().contains("age=7")) {
                expToAdd = 1;
            }
        } else if (blockType == Material.CHORUS_FRUIT) {
            expToAdd = 1;
        }

        // Ajouter l'expérience si un bloc valide a été cassé
        if (expToAdd > 0) {
            currentExp += expToAdd;
            playerExp.put(uuid, currentExp);

            int expToNextLevel = levelRequirements.getOrDefault(currentLevel, Integer.MAX_VALUE);

            // Vérifier si le joueur atteint le niveau suivant
            if (currentExp >= expToNextLevel) {
                playerLevels.put(uuid, currentLevel + 1);
                playerExp.put(uuid, 0); // Réinitialiser l'expérience

                int newLevel = currentLevel + 1;
                MessageUtil.broadcastMessage(plugin.getPrefixInfo(), "Félicitations ! Vous avez atteint le niveau " + newLevel + " !");

                // Exécuter la commande LuckPerms
                String command = "lp user " + player.getName() + " permission set level." + newLevel + " true";
                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command);

            } else {
                MessageUtil.broadcastMessage(plugin.getPrefixInfo(), "§eVous avez " + currentExp + " d'expérience. Il vous reste " + (expToNextLevel - currentExp) + " pour atteindre le niveau suivant.");
            }

            // Sauvegarder automatiquement les données
            databaseManager.savePlayerData(playerLevels, playerExp);
        }
    }

    @EventHandler
    public void onAnimalBreed(EntityBreedEvent event) {
        // Vérifier si le parent est un joueur et un animal
        if (event.getBreeder() instanceof Player && event.getEntity() instanceof Animals) {
            Player player = (Player) event.getBreeder();
            UUID uuid = player.getUniqueId();

            // Gagner de l'expérience pour la reproduction
            int currentLevel = playerLevels.getOrDefault(uuid, 1);
            int currentExp = playerExp.getOrDefault(uuid, 0) + 5; // Gain d'expérience pour la reproduction

            int expToNextLevel = levelRequirements.getOrDefault(currentLevel, Integer.MAX_VALUE);

            playerExp.put(uuid, currentExp);

            // Vérifier si le joueur atteint le niveau suivant
            if (currentExp >= expToNextLevel) {
                playerLevels.put(uuid, currentLevel + 1);
                playerExp.put(uuid, 0); // Réinitialiser l'expérience

                int newLevel = currentLevel + 1;
                player.sendMessage("§aFélicitations ! Vous avez atteint le niveau " + newLevel + " !");

                // Exécuter la commande LuckPerms
                String command = "lp user " + player.getName() + " permission set level." + newLevel + " true";
                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command);

            } else {
                player.sendMessage("§eVous avez " + currentExp + " d'expérience. Il vous reste " + (expToNextLevel - currentExp) + " pour atteindre le niveau suivant.");
            }

            // Sauvegarder automatiquement les données
            databaseManager.savePlayerData(playerLevels, playerExp);
        }
    }
}

