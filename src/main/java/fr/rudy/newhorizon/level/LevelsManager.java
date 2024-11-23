package fr.rudy.newhorizon.level;

import fr.rudy.newhorizon.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class LevelsManager {
    private final Connection database;
    private final HashMap<UUID, Integer> cache = new HashMap<>();

    private final int initialExp;
    private final int expIncrementPercent;

    public LevelsManager() {
        database = Main.get().getDatabase();

        initialExp = Main.get().getConfig().getInt("levels.initialExp");
        expIncrementPercent = Main.get().getConfig().getInt("levels.expIncrementPercent");
    }

    public int getExp(UUID player) {
        if (cache.containsKey(player)) return cache.get(player);

        try (PreparedStatement statement = database.prepareStatement(
                "SELECT uuid, experience " +
                        "FROM newhorizon_player_data " +
                        "WHERE uuid = ?;"
        )) {
            statement.setString(1, player.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) return -1;

                final int exp = resultSet.getInt("experience");
                cache.put(player, exp);
                return exp;
            }
        } catch (SQLException exception) {
            //TODO: Message + Stacktrace
            exception.printStackTrace();
        }

        return -1;
    }

    public boolean setExp(UUID player, int exp) {
        try (PreparedStatement statement = database.prepareStatement(
                "INSERT INTO newhorizon_player_data (uuid, experience) " +
                        "VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "experience = VALUES(experience);"
        )) {
            statement.setString(1, player.toString());
            statement.setInt(2, exp);
            statement.executeUpdate();
            cache.merge(player, exp, (prev, next) -> next);
        } catch (SQLException exception) {
            //TODO: Message + Stacktrace
            exception.printStackTrace();
            return false;
        }

        return true;
    }

    public int getLevel(UUID player) {
        final double playerExp = getExp(player);
        double expNeeded = initialExp;
        int level = 1;

        while (playerExp > expNeeded) {
            expNeeded += expNeeded * (1 + expIncrementPercent / 100.0);
            level++;
        }

        return level;
    }

    public int expToNextLevel(UUID player) {
        double nextLevelExp = initialExp;
        for (int i = 1; i < getLevel(player); i++) {
            nextLevelExp += nextLevelExp * (1 + expIncrementPercent / 100.0);
        }

        return (int) Math.round(nextLevelExp - getExp(player));
    }

}
