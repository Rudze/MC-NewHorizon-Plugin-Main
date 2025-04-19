package fr.rudy.newhorizon.city;

import fr.rudy.newhorizon.Main;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;

public class CityChatListener implements Listener {

    public static final Map<UUID, Location> waitingForCityName = new HashMap<>();
    public static final Set<UUID> cityCreationMode = new HashSet<>();
    public static final Set<UUID> awaitingDeposit = new HashSet<>();
    public static final Set<UUID> awaitingWithdraw = new HashSet<>();

    private final CityManager cityManager = Main.get().getCityManager();
    private final CityBankManager cityBankManager = Main.get().getCityBankManager();
    private final Economy vaultEconomy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String msg = ChatColor.stripColor(event.getMessage()).trim();

        // ⛔ Annuler avec "quitter"
        if (msg.equalsIgnoreCase("quitter")) {
            if (waitingForCityName.containsKey(uuid)) {
                waitingForCityName.remove(uuid);
                cityCreationMode.remove(uuid);
                player.sendMessage("§cCréation annulée.");
            }
            if (awaitingDeposit.remove(uuid)) {
                player.sendMessage("§cDépôt annulé.");
            }
            if (awaitingWithdraw.remove(uuid)) {
                player.sendMessage("§cRetrait annulé.");
            }
            event.setCancelled(true);
            return;
        }

        // 🏦 Dépôt
        if (awaitingDeposit.contains(uuid)) {
            event.setCancelled(true);
            awaitingDeposit.remove(uuid);

            try {
                double amount = Double.parseDouble(msg);
                if (amount <= 0) {
                    player.sendMessage("§cMontant invalide.");
                    return;
                }

                if (!vaultEconomy.has(player, amount)) {
                    player.sendMessage("§cVous n'avez pas assez de pièces.");
                    return;
                }

                int cityId = cityManager.getCityId(uuid);
                if (cityBankManager.deposit(cityId, amount)) {
                    vaultEconomy.withdrawPlayer(player, amount);
                    player.sendMessage("§aVous avez déposé §e" + amount + "§a pièces dans la banque de votre ville !");
                } else {
                    player.sendMessage("§cErreur lors du dépôt.");
                }

            } catch (NumberFormatException e) {
                player.sendMessage("§cVeuillez entrer un montant valide.");
            }
            return;
        }

        // 🏧 Retrait
        if (awaitingWithdraw.contains(uuid)) {
            event.setCancelled(true);
            awaitingWithdraw.remove(uuid);

            try {
                double amount = Double.parseDouble(msg);
                if (amount <= 0) {
                    player.sendMessage("§cMontant invalide.");
                    return;
                }

                int cityId = cityManager.getCityId(uuid);
                if (cityBankManager.withdraw(cityId, amount)) {
                    vaultEconomy.depositPlayer(player, amount);
                    player.sendMessage("§aVous avez retiré §e" + amount + "§a pièces de la banque de la ville !");
                } else {
                    player.sendMessage("§cFonds insuffisants dans la banque de la ville.");
                }

            } catch (NumberFormatException e) {
                player.sendMessage("§cVeuillez entrer un montant valide.");
            }
            return;
        }

        // 🏙️ Nom de ville
        if (!waitingForCityName.containsKey(uuid)) return;

        event.setCancelled(true);
        String cityName = msg;
        Location location = waitingForCityName.remove(uuid);

        if (cityCreationMode.remove(uuid)) {
            if (cityManager.getCityLocation(cityName) != null) {
                player.sendMessage("§cUne ville avec ce nom existe déjà.");
                return;
            }

            if (cityManager.createCity(uuid, cityName, location)) {
                player.sendMessage("§aVille §e" + cityName + " §acréée avec succès ! Vous êtes le chef 👑");
            } else {
                player.sendMessage("§cErreur lors de la création de la ville.");
            }

        } else {
            String currentCity = cityManager.getCityName(uuid);
            CityRank rank = cityManager.getCityRank(uuid);

            if (currentCity == null || rank == null) {
                player.sendMessage("§cVous n'appartenez à aucune ville.");
                return;
            }

            if (!(rank == CityRank.LEADER || rank == CityRank.COLEADER)) {
                player.sendMessage("§cSeul le chef ou le sous-chef peut modifier le spawn.");
                return;
            }

            if (cityManager.createCity(uuid, currentCity, location)) {
                player.sendMessage("§aSpawn de votre ville §e" + currentCity + " §amis à jour !");
            } else {
                player.sendMessage("§cErreur lors de la mise à jour du spawn.");
            }
        }
    }
}
