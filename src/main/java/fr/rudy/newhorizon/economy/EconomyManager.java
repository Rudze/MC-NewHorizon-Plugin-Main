package fr.rudy.newhorizon.economy;

import java.math.BigDecimal;
import java.sql.*;
import java.util.UUID;

public class EconomyManager {
    private final Connection database;

    public EconomyManager(Connection database) {
        this.database = database;
    }

    public void addMoneyColumnIfNotExists() {
        try (Statement statement = database.createStatement()) {
            statement.executeUpdate("ALTER TABLE newhorizon_player_data ADD COLUMN money DOUBLE DEFAULT 0;");
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate column name")) {
                e.printStackTrace();
            }
        }
    }

    public BigDecimal getMoney(UUID player) {
        try (PreparedStatement statement = database.prepareStatement(
                "SELECT money FROM newhorizon_player_data WHERE uuid = ?;"
        )) {
            statement.setString(1, player.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return BigDecimal.valueOf(resultSet.getDouble("money"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    public void setMoney(UUID player, BigDecimal amount) {
        try (PreparedStatement statement = database.prepareStatement(
                "UPDATE newhorizon_player_data SET money = ? WHERE uuid = ?;"
        )) {
            statement.setDouble(1, amount.doubleValue());
            statement.setString(2, player.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addMoney(UUID player, BigDecimal amount) {
        BigDecimal currentBalance = getMoney(player);
        setMoney(player, currentBalance.add(amount));
    }

    public boolean hasEnough(UUID player, BigDecimal amount) {
        return getMoney(player).compareTo(amount) >= 0;
    }

    public void removeMoney(UUID player, BigDecimal amount) {
        BigDecimal currentBalance = getMoney(player);
        setMoney(player, currentBalance.subtract(amount));
    }
}
