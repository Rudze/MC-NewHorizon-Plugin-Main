package fr.rudy.newhorizon.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class VaultEconomy implements Economy {
    private final EconomyManager economyManager;

    public VaultEconomy(EconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    @Override
    public boolean isEnabled() {
        return true; // Indique que le système d'économie est actif.
    }

    @Override
    public String getName() {
        return "VaultEconomy"; // Nom de ton système d'économie.
    }

    @Override
    public boolean hasBankSupport() {
        return false; // Pas de support pour les banques dans ce système.
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        return String.format("%.2f coins", amount);
    }

    @Override
    public String currencyNamePlural() {
        return "coins"; // Nom au pluriel.
    }

    @Override
    public String currencyNameSingular() {
        return "coin"; // Nom au singulier.
    }

    @Override
    public boolean hasAccount(String s) {
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return true; // Tous les joueurs ont un compte.
    }

    @Override
    public boolean hasAccount(String s, String s1) {
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return true; // Comptes non spécifiques aux mondes.
    }

    @Override
    public double getBalance(String s) {
        return 0;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return economyManager.getMoney(player.getUniqueId());
    }

    @Override
    public double getBalance(String s, String s1) {
        return 0;
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player); // Pas de gestion spécifique par monde.
    }

    @Override
    public boolean has(String s, double v) {
        return false;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount; // Vérifie si le joueur a suffisamment d'argent.
    }

    @Override
    public boolean has(String s, String s1, double v) {
        return false;
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount); // Pas de gestion spécifique par monde.
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        double currentBalance = economyManager.getMoney(player.getUniqueId());
        if (currentBalance < amount) {
            return new EconomyResponse(amount, currentBalance, EconomyResponse.ResponseType.FAILURE, "Not enough money");
        }
        economyManager.setMoney(player.getUniqueId(), currentBalance - amount);
        return new EconomyResponse(amount, currentBalance - amount, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, String s1, double v) {
        return null;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount); // Pas de gestion spécifique par monde.
    }

    @Override
    public EconomyResponse depositPlayer(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        double currentBalance = economyManager.getMoney(player.getUniqueId());
        economyManager.setMoney(player.getUniqueId(), currentBalance + amount);
        return new EconomyResponse(amount, currentBalance + amount, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(String s, String s1, double v) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount); // Pas de gestion spécifique par monde.
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not available");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not available");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not available");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not available");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not available");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not available");
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not available");
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not available");
    }

    @Override
    public List<String> getBanks() {
        return null; // Pas de gestion des banques, donc retourne null.
    }

    @Override
    public boolean createPlayerAccount(String s) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return true; // Les comptes sont créés automatiquement dans ce système.
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player); // Pas de gestion spécifique par monde.
    }
}
