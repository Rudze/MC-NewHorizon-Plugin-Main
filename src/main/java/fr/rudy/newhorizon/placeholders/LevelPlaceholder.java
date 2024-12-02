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
        if (identifier == null || identifier.isEmpty()) {
            return "";
        }

        // Identifier des parties du placeholder : exemple "level_<playername>"
        String[] parts = identifier.split("_");
        if (parts.length == 0) {
            return null; // Si aucun identifiant valide, retourne null
        }

        String placeholder = parts[0]; // Le type de placeholder, par exemple "level" ou "exp"
        String targetPlayerName = parts.length > 1 ? parts[1] : null; // Nom du joueur cible si fourni

        UUID targetUuid;

        // Récupération du joueur ciblé
        if (targetPlayerName != null) {
            Player targetPlayer = Main.get().getServer().getPlayerExact(targetPlayerName);
            if (targetPlayer == null) {
                return "Joueur introuvable"; // Si le joueur n'est pas trouvé
            }
            targetUuid = targetPlayer.getUniqueId();
        } else {
            // Si aucun joueur cible n'est spécifié, on utilise le joueur actuel
            if (player == null) {
                return ""; // Aucun joueur actuel (erreur)
            }
            targetUuid = player.getUniqueId();
        }

        // Placeholder pour le niveau
        if (placeholder.equalsIgnoreCase("level")) {
            return String.valueOf(Main.get().getLevelsManager().getLevel(targetUuid));
        }

        // Placeholder pour l'expérience
        if (placeholder.equalsIgnoreCase("exp")) {
            return String.valueOf(Main.get().getLevelsManager().getExp(targetUuid));
        }

        return null; // Retourne null si le placeholder n'est pas reconnu
    }

}
