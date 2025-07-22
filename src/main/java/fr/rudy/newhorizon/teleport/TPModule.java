package fr.rudy.newhorizon.teleport;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.utils.MessageUtil;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class TPModule {

    private final Map<UUID, TPRequest> requests = new HashMap<>();
    private final Map<UUID, Boolean> toggleState = new HashMap<>();

        public void sendRequest(Player sender, Player target) {
        UUID targetId = target.getUniqueId();

        requests.put(targetId, new TPRequest(sender.getUniqueId(), target.getUniqueId()));
            MessageUtil.sendMessage(sender, Main.get().getPrefixInfo(), "Demande envoyée à " + target.getName() + ".");
            MessageUtil.sendMessage(target, Main.get().getPrefixInfo(), sender.getName() + " souhaite se téléporter à vous.");
            MessageUtil.sendMessage(target, Main.get().getPrefixInfo(), "Utilisez /tpaccept ou /tpdeny pour répondre.");
    }

    public boolean acceptRequest(Player target) {
        UUID targetId = target.getUniqueId();

        if (!requests.containsKey(targetId)) {
            // Utiliser MessageUtil pour envoyer le message
            MessageUtil.sendMessage(target, Main.get().getPrefixError(), "Aucune demande de téléportation trouvée.");
            return false;
        }

        TPRequest request = requests.remove(targetId);
        Player sender = target.getServer().getPlayer(request.getSenderId());
        if (sender == null) {
            // Utiliser MessageUtil pour envoyer le message
            MessageUtil.sendMessage(target, Main.get().getPrefixError(), "Le joueur qui a envoyé la demande n'est plus connecté.");
            return false;
        }

        sender.teleport(target.getLocation());
        // Messages utilisant MessageUtil
        MessageUtil.sendMessage(sender, Main.get().getPrefixInfo(), "Téléportation réussie !");
        MessageUtil.sendMessage(target, Main.get().getPrefixInfo(), "Vous avez accepté la demande.");
        return true;
    }


    public boolean denyRequest(Player target) {
        UUID targetId = target.getUniqueId();

        if (!requests.containsKey(targetId)) {
            MessageUtil.sendMessage(target, Main.get().getPrefixError(), "Aucune demande de téléportation trouvée.");
            return false;
        }

        TPRequest request = requests.remove(targetId);
        Player sender = target.getServer().getPlayer(request.getSenderId());
        if (sender != null) {
            MessageUtil.sendMessage(sender, Main.get().getPrefixError(), "Votre demande de téléportation a été refusée.");
        }
        MessageUtil.sendMessage(target, Main.get().getPrefixError(), "Demande refusée.");
        return true;
    }

    public void toggleRequests(Player player) {
        UUID playerId = player.getUniqueId();
        boolean currentState = toggleState.getOrDefault(playerId, true);
        toggleState.put(playerId, !currentState);

        // Utiliser MessageUtil pour envoyer le message avec le préfixe
        MessageUtil.sendMessage(player, Main.get().getPrefixInfo(),
                "§eDemandes de téléportation " + (currentState ? "désactivées" : "activées") + ".");
    }


    public boolean isRequestsEnabled(Player player) {
        return toggleState.getOrDefault(player.getUniqueId(), true);
    }
}