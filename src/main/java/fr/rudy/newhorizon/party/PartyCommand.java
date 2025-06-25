package fr.rudy.newhorizon.party;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class PartyCommand implements CommandExecutor {

    private final PartyManager partyManager;
    private final PartyMenu partyMenu;

    public PartyCommand(PartyManager partyManager, PartyMenu partyMenu) {
        this.partyManager = partyManager;
        this.partyMenu = partyMenu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return false;

        if (args.length == 0) {
            partyMenu.open(p);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "invite" -> {
                if (args.length != 2) {
                    p.sendMessage("§eUtilisation: /party invite <pseudo>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    p.sendMessage("§cJoueur introuvable.");
                    return true;
                }
                partyManager.invite(p, target);
                p.sendMessage("§aInvitation envoyée à " + target.getName());
            }

            case "join" -> {
                if (args.length != 2) {
                    p.sendMessage("§eUtilisation: /party join <pseudo>");
                    return true;
                }
                Player leader = Bukkit.getPlayer(args[1]);
                if (leader == null) {
                    p.sendMessage("§cJoueur introuvable.");
                    return true;
                }
                if (partyManager.join(p, leader)) {
                    p.sendMessage("§aTu as rejoint la party de " + leader.getName());
                } else {
                    p.sendMessage("§cAucune invitation trouvée.");
                }
            }

            case "leave" -> partyManager.leave(p);

            default -> p.sendMessage("§eUtilisation: /party invite <pseudo>, /party join <pseudo>, /party leave");
        }

        return true;
    }
}
