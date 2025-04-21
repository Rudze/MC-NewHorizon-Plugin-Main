package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.city.*;
import fr.rudy.newhorizon.ui.CityGUI;
import fr.rudy.newhorizon.utils.MessageUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.io.BukkitObjectOutputStream;


import java.util.UUID;

public class CityCommand implements CommandExecutor {

    private final CityManager cityManager = Main.get().getCityManager();
    private final CityBankManager cityBankManager = Main.get().getCityBankManager();
    private final Economy economy = Bukkit.getServer().getServicesManager().getRegistration(Economy.class).getProvider();

    Main plugin = Main.get();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

        if (args.length == 0) {
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (cityManager.hasCity(uuid) || cityManager.getCityName(uuid) != null) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Vous êtes déjà dans une ville !");
                    return true;
                }

                MessageUtil.sendMessage(sender, plugin.getPrefixInfo(),"Entrez le nom de votre ville dans le chat. Tapez 'quitter' pour annuler.");
                CityChatListener.waitingForCityName.put(uuid, player.getLocation());
                CityChatListener.cityCreationMode.add(uuid);
                break;

            case "setspawn":
                String cityOfPlayer = cityManager.getCityName(uuid);
                CityRank rankOfPlayer = cityManager.getCityRank(uuid);

                if (cityOfPlayer == null || rankOfPlayer == null) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Vous n'appartenez à aucune ville.");
                    return true;
                }

                if (!(rankOfPlayer == CityRank.LEADER || rankOfPlayer == CityRank.COLEADER)) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Seul le chef ou le sous-chef peut modifier le spawn.");
                    return true;
                }

                if (cityManager.updateCitySpawn(cityOfPlayer, player.getLocation())) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixInfo(),"Spawn de la ville §d" + cityOfPlayer + " §bmis à jour !");
                } else {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Erreur lors de la mise à jour du spawn.");
                }
                break;

            case "tp":
                if (args.length < 2) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Utilisation: /city tp <nom>");
                    return true;
                }

                Location loc = cityManager.getCityLocation(args[1]);
                if (loc != null) {
                    player.teleport(loc);
                    MessageUtil.sendMessage(sender, plugin.getPrefixInfo(),"Téléporté à la ville §d" + args[1]);
                } else {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Cette ville est introuvable.");
                }
                break;

            case "remove":
                String playerCity = cityManager.getCityName(uuid);
                CityRank playerRank = cityManager.getCityRank(uuid);

                if (playerCity == null || playerRank != CityRank.LEADER) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Seul le chef peut supprimer la ville.");
                    return true;
                }

                if (cityManager.removeCity(uuid)) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Votre ville §c" + playerCity + " §ca été supprimée.");
                } else {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Erreur lors de la suppression.");
                }
                break;

            case "leave":
                String cityLeave = cityManager.getCityName(uuid);
                CityRank rankLeave = cityManager.getCityRank(uuid);

                if (cityLeave == null || rankLeave == null) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Vous n'êtes dans aucune ville.");
                    return true;
                }

                if (rankLeave == CityRank.LEADER) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Vous devez transférer le rôle de chef ou supprimer la ville.");
                    return true;
                }

                if (cityManager.removeMember(uuid)) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixInfo(),"Vous avez quitté la ville §d" + cityLeave + "§b.");
                } else {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Erreur lors de votre départ.");
                }
                break;

            case "like":
                if (args.length < 2) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Utilisation: /city like <ville>");
                    return true;
                }

                if (cityManager.hasLikedCity(uuid, args[1])) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Vous avez déjà liké cette ville.");
                    return true;
                }

                if (cityManager.likeCity(uuid, args[1])) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixInfo(),"Vous avez liké la ville §d" + args[1] + " §b!");
                } else {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Ville introuvable.");
                }
                break;

            case "info":
                String infoCity = cityManager.getCityName(uuid);
                CityRank infoRank = cityManager.getCityRank(uuid);

                if (infoCity == null || infoRank == null) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Vous n'appartenez à aucune ville.");
                    return true;
                }

                Location spawn = cityManager.getCityLocation(infoCity);
                int likes = cityManager.getLikes(uuid);

                player.sendMessage("§b§m---------------------------");
                player.sendMessage("§b§l» §eInformations sur votre ville");
                player.sendMessage("§f➤ §dNom : §d" + infoCity);
                player.sendMessage("§f➤ §dGrade : §d" + infoRank.getDisplayName());
                player.sendMessage("§f➤ §dLikes : §d" + likes + " ⭐");
                player.sendMessage("§f➤ §dSpawn : §7" + (spawn != null ? formatLoc(spawn) : "§cNon défini"));
                player.sendMessage("§b§m---------------------------");
                break;

            case "accept":
                String accepted = Main.get().getPendingInvites().remove(uuid);
                if (accepted == null) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Aucune invitation en attente.");
                    return true;
                }

                if (cityManager.getCityName(uuid) != null) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Vous êtes déjà dans une ville.");
                    return true;
                }

                if (cityManager.setMember(accepted, uuid, CityRank.MEMBER)) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixInfo(),"Vous avez rejoint la ville §d" + accepted + "§b !");
                } else {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Erreur lors de l'ajout.");
                }
                break;

            case "deny":
                if (Main.get().getPendingInvites().remove(uuid) != null) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Invitation refusée.");
                } else {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Aucune invitation en attente.");
                }
                break;

            case "invite":
                if (!isLeaderOrCoLeader(uuid)) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Seul le chef ou le sous-chef peut inviter.");
                    return true;
                }

                MessageUtil.sendMessage(sender, plugin.getPrefixInfo(),"Entrez le pseudo à inviter dans le chat. Tapez 'quitter' pour annuler.");
                CityInviteListener.awaitingInviteInput.add(uuid);
                break;

            case "promote":
                if (!isLeader(uuid)) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Seul le chef peut promouvoir un joueur.");
                    return true;
                }

                MessageUtil.sendMessage(sender, plugin.getPrefixInfo(),"Entrez le pseudo à promouvoir. Tapez 'quitter' pour annuler.");
                CityInviteListener.awaitingPromoteInput.add(uuid);
                break;

            case "confirm":
                UUID target = CityInviteListener.awaitingConfirmPromote.remove(uuid);
                if (target == null) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Aucune promotion en attente.");
                    return true;
                }

                String leaderCity = cityManager.getCityName(uuid);
                if (!leaderCity.equals(cityManager.getCityName(target))) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Ce joueur n'est plus dans votre ville.");
                    return true;
                }

                if (cityManager.setMember(leaderCity, uuid, CityRank.COLEADER)
                        && cityManager.setMember(leaderCity, target, CityRank.LEADER)) {
                    Player targetPlayer = Bukkit.getPlayer(target);
                    player.sendMessage("§aVous avez promu §e" + (targetPlayer != null ? targetPlayer.getName() : "le joueur") + " §aChef !");
                    if (targetPlayer != null) targetPlayer.sendMessage("§aVous êtes désormais le Chef de la ville !");
                } else {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Erreur lors de la promotion.");
                }
                break;

            case "demote":
                if (!isLeader(uuid)) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Seul le chef peut rétrograder un joueur.");
                    return true;
                }

                MessageUtil.sendMessage(sender, plugin.getPrefixInfo(),"Entrez le pseudo à rétrograder. Tapez 'quitter' pour annuler.");
                CityInviteListener.awaitingDemoteInput.add(uuid);
                break;

            case "claim":
                if (!isLeaderOrCoLeader(uuid)) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(), "&cSeuls le chef ou le sous-chef peuvent revendiquer.");
                    return true;
                }

                int claimCityId = cityManager.getCityId(uuid);

                // Obtenir le nombre de claims déjà faits
                int currentClaimCount = Main.get().getClaimManager().getClaimCount(claimCityId);

                // Calcul du prix dynamique : 50 * 2^claims
                double baseCost = 50.0;
                double claimCost = baseCost * Math.pow(2, currentClaimCount);

                // Vérif solde
                if (cityBankManager.getBalance(claimCityId) < claimCost) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),
                            "&cLa banque doit contenir au moins &c" + String.format("%.2f", claimCost) + " &cpièces.");
                    return true;
                }

                Chunk chunk = player.getLocation().getChunk();
                if (Main.get().getClaimManager().isChunkClaimed(chunk)) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(), "&cCe chunk est déjà revendiqué.");
                    return true;
                }

                if (Main.get().getClaimManager().claimChunk(claimCityId, chunk)) {
                    cityBankManager.withdraw(claimCityId, claimCost);
                    MessageUtil.sendMessage(sender, plugin.getPrefixInfo(),
                            "&bChunk revendiqué ! &7(-" + String.format("%.2f", claimCost) + " pièces)");
                } else {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(), "&cErreur lors de la revendication.");
                }
                break;

            case "unclaim":
                if (!isLeaderOrCoLeader(uuid)) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Seuls le chef ou le sous-chef peuvent libérer un chunk.");
                    return true;
                }

                int cityIdUnclaim = cityManager.getCityId(uuid);
                Chunk currentChunk = player.getLocation().getChunk();

                if (Main.get().getClaimManager().unclaimChunk(cityIdUnclaim, currentChunk)) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixInfo(),"Chunk libéré !");
                } else {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Ce chunk n'est pas revendiqué par votre ville.");
                }
                break;

            case "deposit":
                if (cityManager.getCityName(uuid) == null) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Vous devez être dans une ville pour faire cela.");
                    return true;
                }

                MessageUtil.sendMessage(sender, plugin.getPrefixInfo(),"Entrez le montant à déposer dans le chat. Tapez 'quitter' pour annuler.");
                CityChatListener.awaitingDeposit.add(uuid);
                break;

            case "withdraw":
                CityRank rankWithdraw = cityManager.getCityRank(uuid);
                if (rankWithdraw == null || (rankWithdraw != CityRank.LEADER && rankWithdraw != CityRank.COLEADER)) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Seuls le chef ou le sous-chef peuvent retirer.");
                    return true;
                }

                MessageUtil.sendMessage(sender, plugin.getPrefixInfo(),"Entrez le montant à retirer dans le chat. Tapez 'quitter' pour annuler.");
                CityChatListener.awaitingWithdraw.add(uuid);
                break;

            case "list":
                new CityGUI().openCityList(player);
                break;

            case "setbanner":
                String cityName = cityManager.getCityName(uuid);
                CityRank rank = cityManager.getCityRank(uuid);

                if (cityName == null || rank != CityRank.LEADER) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Seul le chef de ville peut définir la bannière.");
                    return true;
                }

                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand == null || !(itemInHand.getItemMeta() instanceof BannerMeta)) {
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Vous devez tenir une §ebannière personnalisée §cdans votre main.");
                    return true;
                }

                try {
                    // Sérialisation de l'ItemStack en Base64
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
                    dataOutput.writeObject(itemInHand);
                    dataOutput.close();
                    String encoded = Base64.getEncoder().encodeToString(outputStream.toByteArray());

                    try (PreparedStatement ps = Main.get().getDatabase().prepareStatement(
                            "UPDATE newhorizon_cities SET banner = ? WHERE city_name = ?")) {
                        ps.setString(1, encoded);
                        ps.setString(2, cityName);
                        ps.executeUpdate();
                    }

                    MessageUtil.sendMessage(sender, plugin.getPrefixInfo(),"La bannière de votre ville a été mise à jour avec succès !");
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                    MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Une erreur est survenue lors de la sauvegarde de la bannière.");
                }
                break;



            default:
                //MessageUtil.sendMessage(sender, plugin.getPrefixError(),"Commande inconnue. Utilisation: /city <create|...>");
                break;
        }

        return true;
    }

    private boolean isLeader(UUID uuid) {
        return cityManager.getCityRank(uuid) == CityRank.LEADER;
    }

    private boolean isLeaderOrCoLeader(UUID uuid) {
        CityRank rank = cityManager.getCityRank(uuid);
        return rank == CityRank.LEADER || rank == CityRank.COLEADER;
    }

    private String formatLoc(Location loc) {
        return "§f" + loc.getWorld().getName() + " §7(§a" +
                Math.round(loc.getX()) + "§7, §a" +
                Math.round(loc.getY()) + "§7, §a" +
                Math.round(loc.getZ()) + "§7)";
    }
}
