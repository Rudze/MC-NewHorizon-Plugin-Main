package fr.rudy.newhorizon.placeholders;

import fr.rudy.newhorizon.Main;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.HashMap;
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
        return true; // Permet de s'assurer que l'expansion reste active après un reload.
    }

    @Override
    public boolean canRegister() {
        return true; // Permet l'enregistrement de l'expansion.
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        UUID uuid = player.getUniqueId();

        // Placeholder pour le niveau du joueur
        if (identifier.equalsIgnoreCase("level")) {
            return String.valueOf(Main.get().getLevelsManager().getLevel(uuid));
        }

        // Placeholder pour l'expérience du joueur
        if (identifier.equalsIgnoreCase("exp")) {
            return String.valueOf(Main.get().getLevelsManager().getExp(uuid));
        }

        return null; // Retourne null si le placeholder n'est pas reconnu
    }
}
