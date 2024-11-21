package fr.rudy.newhorizon.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class LevelPlaceholder extends PlaceholderExpansion {

    private final HashMap<UUID, Integer> playerLevels;
    private final HashMap<UUID, Integer> playerExp;

    public LevelPlaceholder(HashMap<UUID, Integer> playerLevels, HashMap<UUID, Integer> playerExp) {
        this.playerLevels = playerLevels;
        this.playerExp = playerExp;
    }

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
            return String.valueOf(playerLevels.getOrDefault(uuid, 1));
        }

        // Placeholder pour l'expérience du joueur
        if (identifier.equalsIgnoreCase("exp")) {
            return String.valueOf(playerExp.getOrDefault(uuid, 0));
        }

        return null; // Retourne null si le placeholder n'est pas reconnu
    }
}
