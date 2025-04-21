package fr.rudy.newhorizon.city;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.utils.MessageUtil;
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
    private final Main plugin = Main.get();

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
                MessageUtil.sendMessage(player, plugin.getPrefixError(), "&cCréation annulée.");
            }
            if (awaitingDeposit.remove(uuid)) {
                MessageUtil.sendMessage(player, plugin.getPrefixError(), "&cDépôt annulé.");
            }
            if (awaitingWithdraw.remove(uuid)) {
                MessageUtil.sendMessage(player, plugin.getPrefixError(), "&cRetrait annulé.");
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
                    MessageUtil.sendMessage(player, plugin.getPrefixError(), "&cMontant invalide.");
                    return;
                }

                if (!vaultEconomy.has(player, amount)) {
                    MessageUtil.sendMessage(player, plugin.getPrefixError(), "&cVous n'avez pas assez de pièces.");
                    return;
                }

                int cityId = cityManager.getCityId(uuid);
                if (cityBankManager.deposit(cityId, amount)) {
                    vaultEconomy.withdrawPlayer(player, amount);
                    MessageUtil.sendMessage(player, plugin.getPrefixInfo(), "&bVous avez déposé &d" + amount + " &bpièces dans la banque de votre ville !");
                } else {
                    MessageUtil.sendMessage(player, plugin.getPrefixError(), "&cErreur lors du dépôt.");
                }

            } catch (NumberFormatException e) {
                MessageUtil.sendMessage(player, plugin.getPrefixError(), "&cVeuillez entrer un montant valide.");
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
                    MessageUtil.sendMessage(player, plugin.getPrefixError(), "&cMontant invalide.");
                    return;
                }

                int cityId = cityManager.getCityId(uuid);
                if (cityBankManager.withdraw(cityId, amount)) {
                    vaultEconomy.depositPlayer(player, amount);
                    MessageUtil.sendMessage(player, plugin.getPrefixInfo(), "&bVous avez retiré &d" + amount + " &bpièces de la banque de la ville !");
                } else {
                    MessageUtil.sendMessage(player, plugin.getPrefixError(), "&cFonds insuffisants dans la banque de la ville.");
                }

            } catch (NumberFormatException e) {
                MessageUtil.sendMessage(player, plugin.getPrefixError(), "&cVeuillez entrer un montant valide.");
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
                MessageUtil.sendMessage(player, plugin.getPrefixError(), "&cUne ville avec ce nom existe déjà.");
                return;
            }

            if (cityManager.createCity(uuid, cityName, location)) {
                MessageUtil.sendMessage(player, plugin.getPrefixInfo(), "&bVille &d" + cityName + " &bcréée avec succès ! Vous êtes le chef 👑");
            } else {
                MessageUtil.sendMessage(player, plugin.getPrefixError(), "&cErreur lors de la création de la ville.");
            }

        } else {
            String currentCity = cityManager.getCityName(uuid);
            CityRank rank = cityManager.getCityRank(uuid);

            if (currentCity == null || rank == null) {
                MessageUtil.sendMessage(player, plugin.getPrefixError(), "&cVous n'appartenez à aucune ville.");
                return;
            }

            if (!(rank == CityRank.LEADER || rank == CityRank.COLEADER)) {
                MessageUtil.sendMessage(player, plugin.getPrefixError(), "&cSeul le chef ou le sous-chef peut modifier le spawn.");
                return;
            }

            if (cityManager.createCity(uuid, currentCity, location)) {
                MessageUtil.sendMessage(player, plugin.getPrefixInfo(), "&bSpawn de votre ville &d" + currentCity + " &bmis à jour !");
            } else {
                MessageUtil.sendMessage(player, plugin.getPrefixError(), "&cErreur lors de la mise à jour du spawn.");
            }
        }
    }
}
