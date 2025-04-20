package fr.rudy.newhorizon.ui;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class NameTagManager {

    public static void update(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        // Utilise un nom d'équipe unique, max 16 caractères
        String teamName = "nh_" + player.getUniqueId().toString().substring(0, 14);

        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        // Appliquer le prefix/suffix avec PlaceholderAPI
        team.setPrefix(PlaceholderAPI.setPlaceholders(player, "%vault_prefix% "));
        team.setSuffix(PlaceholderAPI.setPlaceholders(player, "\n§7[lvl %newhorizon_level_" + player.getName() + "%]"));

        team.addEntry(player.getName());

        // Assigner le scoreboard au joueur
        player.setScoreboard(scoreboard);
    }
}
