package fr.rudy.newhorizon.party;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.utils.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class PartyManager {

    private final Plugin plugin;
    private final Main main = Main.get();

    // clé = leader UUID, valeur = party
    private final Map<UUID, NewHorizonParty> activeParties = new HashMap<>();
    // clé = invité UUID, valeur = leader UUID qui l’a invité
    private final Map<UUID, UUID> invitations = new HashMap<>();

    public PartyManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void invite(Player leader, Player target) {
        if (leader.getUniqueId().equals(target.getUniqueId())) return;

        invitations.put(target.getUniqueId(), leader.getUniqueId());
        MessageUtil.sendMessage(target, main.getPrefixInfo(), leader.getName() + " vous a invité à rejoindre son groupe.");
        MessageUtil.sendMessage(target, main.getPrefixInfo(), "Utilisez &d/party join " + leader.getName() + " &bpour accepter.");
    }

    public boolean join(Player joiner, Player leader) {
        UUID leaderId = leader.getUniqueId();
        UUID joinerId = joiner.getUniqueId();

        // Vérifie invitation
        if (!invitations.containsKey(joinerId) || !invitations.get(joinerId).equals(leaderId)) {
            return false;
        }

        // Forcer à quitter son groupe actuel
        leave(joiner);

        // Créer ou récupérer la party du leader
        NewHorizonParty party = activeParties.computeIfAbsent(leaderId, id -> new NewHorizonParty(leader, plugin));

        // Ajouter le joueur s'il n'y est pas déjà
        party.addPlayer(joiner);

        invitations.remove(joinerId);
        party.getPlayers().forEach(p ->
                MessageUtil.sendMessage(p, main.getPrefixInfo(), joiner.getName() + " a rejoint le groupe.")
        );
        return true;
    }

    public void leave(Player player) {
        UUID playerId = player.getUniqueId();

        // Retirer le joueur de toutes les parties (en tant que leader ou membre)
        Iterator<Map.Entry<UUID, NewHorizonParty>> it = activeParties.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, NewHorizonParty> entry = it.next();
            UUID leaderId = entry.getKey();
            NewHorizonParty party = entry.getValue();

            if (leaderId.equals(playerId)) {
                // Si c'est le leader, dissoudre le groupe
                for (Player member : new ArrayList<>(party.getPlayers())) {
                    if (!member.getUniqueId().equals(playerId)) {
                        MessageUtil.sendMessage(member, main.getPrefixError(), "Le groupe a été dissous car le chef l’a quitté.");
                    }
                }
                it.remove();
            } else if (party.getPlayers().contains(player)) {
                // Sinon enlever le joueur du groupe
                party.removePlayer(player);
                MessageUtil.sendMessage(player, main.getPrefixInfo(), "Tu as quitté le groupe.");
                for (Player member : party.getPlayers()) {
                    MessageUtil.sendMessage(member, main.getPrefixInfo(), player.getName() + " a quitté le groupe.");
                }
                if (party.getPlayers().isEmpty()) {
                    it.remove();
                }
            }
        }

        // Supprimer les invitations liées
        invitations.entrySet().removeIf(e -> e.getKey().equals(playerId) || e.getValue().equals(playerId));
    }

    public List<Player> getPartyMembers(Player player) {
        return activeParties.values().stream()
                .filter(p -> p.getPlayers().contains(player))
                .findFirst()
                .map(NewHorizonParty::getPlayers)
                .orElse(List.of());
    }

    public boolean isLeader(Player player) {
        return activeParties.containsKey(player.getUniqueId());
    }

    public boolean createParty(Player leader) {
        if (!isLeader(leader)) {
            activeParties.put(leader.getUniqueId(), new NewHorizonParty(leader, plugin));
            MessageUtil.sendMessage(leader, main.getPrefixInfo(), "Groupe créé avec succès.");
            return true;
        } else {
            MessageUtil.sendMessage(leader, main.getPrefixError(), "Tu fais déjà partie d'un groupe.");
            return false;
        }
    }
}
