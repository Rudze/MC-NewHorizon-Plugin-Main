package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.city.*;
import fr.rudy.newhorizon.ui.CityGUI;
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCommande réservée aux joueurs !");
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
                    player.sendMessage("§cVous êtes déjà dans une ville !");
                    return true;
                }

                player.sendMessage("§aEntrez le nom de votre ville dans le chat. Tapez 'quitter' pour annuler.");
                CityChatListener.waitingForCityName.put(uuid, player.getLocation());
                CityChatListener.cityCreationMode.add(uuid);
                break;

            case "setspawn":
                String cityOfPlayer = cityManager.getCityName(uuid);
                CityRank rankOfPlayer = cityManager.getCityRank(uuid);

                if (cityOfPlayer == null || rankOfPlayer == null) {
                    player.sendMessage("§cVous n'appartenez à aucune ville.");
                    return true;
                }

                if (!(rankOfPlayer == CityRank.LEADER || rankOfPlayer == CityRank.COLEADER)) {
                    player.sendMessage("§cSeul le chef ou le sous-chef peut modifier le spawn.");
                    return true;
                }

                if (cityManager.updateCitySpawn(cityOfPlayer, player.getLocation())) {
                    player.sendMessage("§aSpawn de la ville §e" + cityOfPlayer + " §amis à jour !");
                } else {
                    player.sendMessage("§cErreur lors de la mise à jour du spawn.");
                }
                break;

            case "tp":
                if (args.length < 2) {
                    player.sendMessage("§cUtilisation: /city tp <nom>");
                    return true;
                }

                Location loc = cityManager.getCityLocation(args[1]);
                if (loc != null) {
                    player.teleport(loc);
                    player.sendMessage("§aTéléporté à la ville §e" + args[1]);
                } else {
                    player.sendMessage("§cCette ville est introuvable.");
                }
                break;

            case "remove":
                String playerCity = cityManager.getCityName(uuid);
                CityRank playerRank = cityManager.getCityRank(uuid);

                if (playerCity == null || playerRank != CityRank.LEADER) {
                    player.sendMessage("§cSeul le chef peut supprimer la ville.");
                    return true;
                }

                if (cityManager.removeCity(uuid)) {
                    player.sendMessage("§aVotre ville §e" + playerCity + " §aa été supprimée.");
                } else {
                    player.sendMessage("§cErreur lors de la suppression.");
                }
                break;

            case "leave":
                String cityLeave = cityManager.getCityName(uuid);
                CityRank rankLeave = cityManager.getCityRank(uuid);

                if (cityLeave == null || rankLeave == null) {
                    player.sendMessage("§cVous n'êtes dans aucune ville.");
                    return true;
                }

                if (rankLeave == CityRank.LEADER) {
                    player.sendMessage("§cVous devez transférer le rôle ou supprimer la ville.");
                    return true;
                }

                if (cityManager.removeMember(uuid)) {
                    player.sendMessage("§aVous avez quitté la ville §e" + cityLeave + "§a.");
                } else {
                    player.sendMessage("§cErreur lors de votre départ.");
                }
                break;

            case "like":
                if (args.length < 2) {
                    player.sendMessage("§cUtilisation: /city like <ville>");
                    return true;
                }

                if (cityManager.hasLikedCity(uuid, args[1])) {
                    player.sendMessage("§cVous avez déjà liké cette ville.");
                    return true;
                }

                if (cityManager.likeCity(uuid, args[1])) {
                    player.sendMessage("§aVous avez liké la ville §e" + args[1] + " §a!");
                } else {
                    player.sendMessage("§cVille introuvable ou erreur.");
                }
                break;

            case "info":
                String infoCity = cityManager.getCityName(uuid);
                CityRank infoRank = cityManager.getCityRank(uuid);

                if (infoCity == null || infoRank == null) {
                    player.sendMessage("§cVous n'appartenez à aucune ville.");
                    return true;
                }

                Location spawn = cityManager.getCityLocation(infoCity);
                int likes = cityManager.getLikes(uuid);

                player.sendMessage("§8§m---------------------------");
                player.sendMessage("§6§l» §eInformations sur votre ville");
                player.sendMessage("§f➤ §eNom : §b" + infoCity);
                player.sendMessage("§f➤ §eGrade : §b" + infoRank.getDisplayName());
                player.sendMessage("§f➤ §eLikes : §b" + likes + " ⭐");
                player.sendMessage("§f➤ §eSpawn : §7" + (spawn != null ? formatLoc(spawn) : "§cNon défini"));
                player.sendMessage("§8§m---------------------------");
                break;

            case "accept":
                String accepted = Main.get().getPendingInvites().remove(uuid);
                if (accepted == null) {
                    player.sendMessage("§cAucune invitation en attente.");
                    return true;
                }

                if (cityManager.getCityName(uuid) != null) {
                    player.sendMessage("§cVous êtes déjà dans une ville.");
                    return true;
                }

                if (cityManager.setMember(accepted, uuid, CityRank.MEMBER)) {
                    player.sendMessage("§aVous avez rejoint la ville §b" + accepted + "§a !");
                } else {
                    player.sendMessage("§cErreur lors de l'ajout.");
                }
                break;

            case "deny":
                if (Main.get().getPendingInvites().remove(uuid) != null) {
                    player.sendMessage("§cInvitation refusée.");
                } else {
                    player.sendMessage("§cAucune invitation en attente.");
                }
                break;

            case "invite":
                if (!isLeaderOrCoLeader(uuid)) {
                    player.sendMessage("§cSeul le chef ou le sous-chef peut inviter.");
                    return true;
                }

                player.sendMessage("§aEntrez le pseudo à inviter dans le chat. Tapez 'quitter' pour annuler.");
                CityInviteListener.awaitingInviteInput.add(uuid);
                break;

            case "promote":
                if (!isLeader(uuid)) {
                    player.sendMessage("§cSeul le chef peut promouvoir un joueur.");
                    return true;
                }

                player.sendMessage("§aEntrez le pseudo à promouvoir. Tapez 'quitter' pour annuler.");
                CityInviteListener.awaitingPromoteInput.add(uuid);
                break;

            case "confirm":
                UUID target = CityInviteListener.awaitingConfirmPromote.remove(uuid);
                if (target == null) {
                    player.sendMessage("§cAucune promotion en attente.");
                    return true;
                }

                String leaderCity = cityManager.getCityName(uuid);
                if (!leaderCity.equals(cityManager.getCityName(target))) {
                    player.sendMessage("§cCe joueur n'est plus dans votre ville.");
                    return true;
                }

                if (cityManager.setMember(leaderCity, uuid, CityRank.COLEADER)
                        && cityManager.setMember(leaderCity, target, CityRank.LEADER)) {
                    Player targetPlayer = Bukkit.getPlayer(target);
                    player.sendMessage("§aVous avez promu §e" + (targetPlayer != null ? targetPlayer.getName() : "le joueur") + " §aChef !");
                    if (targetPlayer != null) targetPlayer.sendMessage("§aVous êtes désormais le Chef de la ville !");
                } else {
                    player.sendMessage("§cErreur lors de la promotion.");
                }
                break;

            case "demote":
                if (!isLeader(uuid)) {
                    player.sendMessage("§cSeul le chef peut rétrograder un joueur.");
                    return true;
                }

                player.sendMessage("§aEntrez le pseudo à rétrograder. Tapez 'quitter' pour annuler.");
                CityInviteListener.awaitingDemoteInput.add(uuid);
                break;

            case "claim":
                if (!isLeaderOrCoLeader(uuid)) {
                    player.sendMessage("§cSeuls le chef ou le sous-chef peuvent revendiquer.");
                    return true;
                }

                int claimCityId = cityManager.getCityId(uuid);
                double claimCost = 50.0;

                if (cityBankManager.getBalance(claimCityId) < claimCost) {
                    player.sendMessage("§cLa banque doit contenir au moins §e" + claimCost + " pièces.");
                    return true;
                }

                Chunk chunk = player.getLocation().getChunk();
                if (Main.get().getClaimManager().isChunkClaimed(chunk)) {
                    player.sendMessage("§cCe chunk est déjà revendiqué.");
                    return true;
                }

                if (Main.get().getClaimManager().claimChunk(claimCityId, chunk)) {
                    cityBankManager.withdraw(claimCityId, claimCost);
                    player.sendMessage("§aChunk revendiqué ! §7(-" + claimCost + " pièces)");
                } else {
                    player.sendMessage("§cErreur lors de la revendication.");
                }
                break;

            case "unclaim":
                if (!isLeaderOrCoLeader(uuid)) {
                    player.sendMessage("§cSeuls le chef ou le sous-chef peuvent libérer un chunk.");
                    return true;
                }

                int cityIdUnclaim = cityManager.getCityId(uuid);
                Chunk currentChunk = player.getLocation().getChunk();

                if (Main.get().getClaimManager().unclaimChunk(cityIdUnclaim, currentChunk)) {
                    player.sendMessage("§aChunk libéré !");
                } else {
                    player.sendMessage("§cCe chunk n'est pas revendiqué par votre ville.");
                }
                break;

            case "deposit":
                if (cityManager.getCityName(uuid) == null) {
                    player.sendMessage("§cVous devez être dans une ville pour faire cela.");
                    return true;
                }

                player.sendMessage("§aEntrez le montant à déposer dans le chat. Tapez 'quitter' pour annuler.");
                CityChatListener.awaitingDeposit.add(uuid);
                break;

            case "withdraw":
                CityRank rankWithdraw = cityManager.getCityRank(uuid);
                if (rankWithdraw == null || (rankWithdraw != CityRank.LEADER && rankWithdraw != CityRank.COLEADER)) {
                    player.sendMessage("§cSeuls le chef ou le sous-chef peuvent retirer.");
                    return true;
                }

                player.sendMessage("§aEntrez le montant à retirer dans le chat. Tapez 'quitter' pour annuler.");
                CityChatListener.awaitingWithdraw.add(uuid);
                break;

            case "list":
                new CityGUI().openCityList(player);
                break;

            case "setbanner":
                String cityName = cityManager.getCityName(uuid);
                CityRank rank = cityManager.getCityRank(uuid);

                if (cityName == null || rank != CityRank.LEADER) {
                    player.sendMessage("§cSeul le chef de ville peut définir la bannière.");
                    return true;
                }

                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand == null || !(itemInHand.getItemMeta() instanceof BannerMeta)) {
                    player.sendMessage("§cVous devez tenir une §ebannière personnalisée §cdans votre main.");
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

                    player.sendMessage("§a✅ La bannière de votre ville a été mise à jour avec succès !");
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                    player.sendMessage("§c❌ Une erreur est survenue lors de la sauvegarde de la bannière.");
                }
                break;



            default:
                player.sendMessage("§cCommande inconnue. Utilisation: /city <create|...>");
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
