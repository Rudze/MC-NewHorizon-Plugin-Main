package fr.rudy.newhorizon.placeholders;

import fr.rudy.newhorizon.Main;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LevelPlaceholder extends PlaceholderExpansion {
    @Override
    public String getIdentifier() {
        return "newhorizon";
    }

    @Override
    public String getAuthor() {
        return "Rudy";
    }

    @Override
    public String getVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return "";
        }

        // Ex: %newhorizon_level_KmiLKze_%
        String[] parts = identifier.split("_");

        if (parts.length == 1) {
            // %newhorizon_level% ou %newhorizon_exp%
            String placeholder = parts[0];

            if (player == null) return "";

            if (placeholder.equalsIgnoreCase("level")) {
                return String.valueOf(Main.get().getLevelsManager().getLevel(player.getUniqueId()));
            }

            if (placeholder.equalsIgnoreCase("exp")) {
                return String.valueOf(Main.get().getLevelsManager().getExp(player.getUniqueId()));
            }

        } else if (parts.length == 2) {
            // %newhorizon_level_<playername>%
            String placeholder = parts[0];
            String targetPlayerName = parts[1];

            Player targetPlayer = Bukkit.getPlayer(targetPlayerName); // insensible à la casse

            if (targetPlayer == null) {
                return "Joueur introuvable";
            }

            UUID targetUuid = targetPlayer.getUniqueId();

            if (placeholder.equalsIgnoreCase("level")) {
                return String.valueOf(Main.get().getLevelsManager().getLevel(targetUuid));
            }

            if (placeholder.equalsIgnoreCase("exp")) {
                return String.valueOf(Main.get().getLevelsManager().getExp(targetUuid));
            }
        }

        return null; // placeholder inconnu
    }
}
