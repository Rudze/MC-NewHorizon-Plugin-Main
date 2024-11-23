package fr.rudy.newhorizon.level;

import fr.rudy.newhorizon.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.*;
import java.util.regex.Pattern;

public class PlayerListener implements Listener {
    private final LevelsManager levelsManager;
    private final List<HashMap<String, Integer>> breakBlocks;

    public PlayerListener() {
        levelsManager = Main.get().getLevelsManager();
        breakBlocks = (List<HashMap<String, Integer>>) Main.get().getConfig().getList("levels.break_blocks", new ArrayList<>());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        //if(event.getPlayer().getWorld().equals(Main.get().getServer().getWorld("world_newhorizon"))) return;

        blocksLoop:
        for (HashMap<String, Integer> block : breakBlocks) {
            final Set<String> keys = new HashSet<>(block.keySet());
            if (keys.stream().findFirst().isEmpty()) continue;

            final String name = keys.stream().findFirst().get();
            if (!Pattern.compile(name.replace("*", ".*").toLowerCase())
                    .matcher(event.getBlock().getType().toString().toLowerCase())
                    .matches()
            ) continue;
            keys.remove(name);

            for (String metadata : keys)
                if (!event.getBlock().getBlockData().getAsString().toLowerCase().contains((metadata + "=" + block.get(metadata)).toLowerCase()))
                    continue blocksLoop;
            levelsManager.setExp(event.getPlayer().getUniqueId(), levelsManager.getExp(event.getPlayer().getUniqueId()) + block.get(name));
        }


        /*Main plugin = Main.get();
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Récupérer l'instance du monde "world_resource"
        World newhorizonWorld = Main.get().getServer().getWorld("world_newhorizon");

        // Vérifier si le joueur est dans le monde "world_resource"
        World playerWorld = player.getWorld();

        // Debug : Afficher le monde actuel du joueur

        // Vérification du monde
        if (newhorizonWorld == null || playerWorld.equals(newhorizonWorld)) {
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
            }

            // Sauvegarder automatiquement les données

            databaseManager.savePlayerData(playerLevels, playerExp);
        }*/
    }

    @EventHandler
    public void onAnimalBreed(EntityBreedEvent event) {
        /*// Vérifier si le parent est un joueur et un animal
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
            }

            // Sauvegarder automatiquement les données
            databaseManager.savePlayerData(playerLevels, playerExp);
        }*/
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        /*// Vérifiez si le joueur a effectivement pêché un poisson
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();

            // Gagner de l'expérience pour avoir pêché un poisson
            int currentLevel = playerLevels.getOrDefault(uuid, 1);
            int currentExp = playerExp.getOrDefault(uuid, 0);
            int expToAdd = Main.get().getConfig().getInt("leveling.fishing_exp", 10);
            // Quantité d'XP ajoutée pour la pêche

            currentExp += expToAdd;
            playerExp.put(uuid, currentExp);

            int expToNextLevel = levelRequirements.getOrDefault(currentLevel, Integer.MAX_VALUE);

            // Vérifier si le joueur atteint le niveau suivant
            if (currentExp >= expToNextLevel) {
                playerLevels.put(uuid, currentLevel + 1);
                playerExp.put(uuid, 0); // Réinitialiser l'expérience
                int newLevel = currentLevel + 1;

                player.sendMessage("§aFélicitations ! Vous avez atteint le niveau " + newLevel + " !");
                String command = "lp user " + player.getName() + " permission set level." + newLevel + " true";
                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command);

            } else {
            }

            // Sauvegarder automatiquement les données
            databaseManager.savePlayerData(playerLevels, playerExp);
        }*/
    }
}

