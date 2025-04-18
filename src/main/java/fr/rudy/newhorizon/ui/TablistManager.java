package fr.rudy.newhorizon.ui;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

public class TablistManager {

    private final JavaPlugin plugin;

    public TablistManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateTab(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 40L); // toutes les 2 secondes
    }

    public void updateTab(Player player) {
        // Ajoute des espaces ou caractères invisibles (ex: points colorés transparents) pour simuler du centrage
        String leftPadding = "§r§7      "; // 6 espaces gris clair
        String rightPadding = "      §r"; // 6 espaces de l’autre côté

        String header = "\n\n\n" +
                leftPadding + "§f\uE0D0" + rightPadding + "\n\n\n\n\n" +
                leftPadding + "§fSoutenez-nous avec le §b/boutique §f!" + rightPadding + "\n";

        String footer = "\n" +
                leftPadding + "§7/site | /discord | /boutique" + rightPadding + "\n ";

        String nameFormat = "%vault_prefix% §7[lvl %newhorizon_level_" + player.getName() + "%] §f" + player.getName();
        String formattedName = PlaceholderAPI.setPlaceholders(player, nameFormat);

        player.setPlayerListName(formattedName);
        player.setPlayerListHeaderFooter(header, footer);
    }
}
