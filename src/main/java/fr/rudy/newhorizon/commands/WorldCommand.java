package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.utils.MessageUtil;
import fr.rudy.newhorizon.world.WorldSpawnManager;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.io.File;

public class WorldCommand implements CommandExecutor {

    private final String permission = "newhorizon.admin.world";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(permission)) {
            MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Vous n'avez pas la permission !");
            return true;
        }

        if (args.length < 2) {
            MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Utilisation : /world <create|remove|tp|setspawn> <nom> [joueur]");
            return true;
        }

        String action = args[0].toLowerCase();
        String worldName = args[1];

        switch (action) {
            case "create":
                return createWorld(sender, worldName);
            case "remove":
                return removeWorld(sender, worldName);
            case "tp":
                String targetName = args.length >= 3 ? args[2] : sender.getName();
                return teleportToWorld(sender, worldName, targetName);
            case "setspawn":
                return setWorldSpawn(sender, worldName);
            default:
                MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Commande inconnue. Utilisez: create, remove, tp, setspawn");
                return true;
        }
    }

    private boolean createWorld(CommandSender sender, String worldName) {
        if (Bukkit.getWorld(worldName) != null) {
            MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Le monde §b" + worldName + "§b existe déjà !");
            return true;
        }

        WorldCreator creator = new WorldCreator(worldName);
        creator.createWorld();
        MessageUtil.sendMessage(sender, Main.get().getPrefixInfo(), "Monde §b" + worldName + "§b créé avec succès !");
        return true;
    }

    private boolean removeWorld(CommandSender sender, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            Bukkit.unloadWorld(world, false);
        }

        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (!worldFolder.exists()) {
            MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Le dossier du monde §b" + worldName + "§b n'existe pas.");
            return true;
        }

        if (deleteFolder(worldFolder)) {
            MessageUtil.sendMessage(sender, Main.get().getPrefixInfo(), "Le monde §b" + worldName + "§b a été supprimé !");
        } else {
            MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Erreur lors de la suppression du monde §b" + worldName);
        }
        return true;
    }

    private boolean teleportToWorld(CommandSender sender, String worldName, String targetName) {
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Joueur introuvable : §b" + targetName);
            return true;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
            if (!worldFolder.exists() || !worldFolder.isDirectory()) {
                MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Le monde §b" + worldName + "§b n'existe pas dans les fichiers !");
                return true;
            }

            MessageUtil.sendMessage(sender, Main.get().getPrefixInfo(), "Chargement du monde §b" + worldName + "§b...");
            world = new WorldCreator(worldName).createWorld();
            if (world == null) {
                MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Erreur lors du chargement du monde §b" + worldName + "§b.");
                return true;
            }

            MessageUtil.sendMessage(sender, Main.get().getPrefixInfo(), "Monde §b" + worldName + "§b chargé avec succès !");
        }

        WorldSpawnManager spawnManager = Main.get().getWorldSpawnManager();
        Location spawn = spawnManager.getSpawn(worldName);

        if (spawn == null) {
            spawn = world.getSpawnLocation();
            spawn.setYaw(0f);
            spawn.setPitch(0f);
            MessageUtil.sendMessage(sender, Main.get().getPrefixInfo(), "Aucun spawn personnalisé trouvé. Téléportation au spawn par défaut.");
        } else {
            MessageUtil.sendMessage(sender, Main.get().getPrefixInfo(), "Téléportation vers le spawn précis de §b" + worldName + "§b.");
        }

        target.teleport(spawn);
        if (!sender.getName().equalsIgnoreCase(target.getName())) {
            MessageUtil.sendMessage(target, Main.get().getPrefixInfo(), "Vous avez été téléporté dans §b" + worldName + " §bpar un administrateur.");
        }
        return true;
    }

    private boolean setWorldSpawn(CommandSender sender, String worldName) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Seuls les joueurs peuvent exécuter cette commande.");
            return true;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Le monde §b" + worldName + "§b n'est pas chargé !");
            return true;
        }

        Player player = (Player) sender;
        if (!player.getWorld().equals(world)) {
            MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Vous devez être dans le monde §b" + worldName + "§b pour définir son spawn.");
            return true;
        }

        Location loc = player.getLocation();
        Main.get().getWorldSpawnManager().setSpawn(worldName, loc);
        MessageUtil.sendMessage(sender, Main.get().getPrefixInfo(), "Le spawn précis du monde §b" + worldName + "§b a été défini !");
        return true;
    }

    private boolean deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        return folder.delete();
    }
}
