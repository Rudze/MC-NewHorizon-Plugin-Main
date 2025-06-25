package fr.rudy.newhorizon.friend;

import java.sql.*;
import java.util.*;

public class FriendManager {

    private final Connection db;

    public FriendManager(Connection db) {
        this.db = db;
    }

    // === DEMANDES D'AMIS ===

    public boolean sendRequest(UUID sender, UUID receiver) {
        if (hasPendingRequest(sender, receiver) || areFriends(sender, receiver)) return false;

        try (PreparedStatement ps = db.prepareStatement(
                "INSERT OR IGNORE INTO newhorizon_friend_requests (sender_uuid, receiver_uuid) VALUES (?, ?)")) {
            ps.setString(1, sender.toString());
            ps.setString(2, receiver.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasPendingRequest(UUID sender, UUID receiver) {
        try (PreparedStatement ps = db.prepareStatement(
                "SELECT * FROM newhorizon_friend_requests WHERE sender_uuid = ? AND receiver_uuid = ?")) {
            ps.setString(1, sender.toString());
            ps.setString(2, receiver.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean acceptRequest(UUID receiver, UUID sender) {
        if (!hasPendingRequest(sender, receiver)) return false;

        try {
            db.setAutoCommit(false);

            // Supprime la demande
            try (PreparedStatement del = db.prepareStatement(
                    "DELETE FROM newhorizon_friend_requests WHERE sender_uuid = ? AND receiver_uuid = ?")) {
                del.setString(1, sender.toString());
                del.setString(2, receiver.toString());
                del.executeUpdate();
            }

            // Ajoute l’amitié dans les deux sens
            try (PreparedStatement add = db.prepareStatement(
                    "INSERT OR IGNORE INTO newhorizon_friends (player_uuid, friend_uuid) VALUES (?, ?)")) {
                add.setString(1, sender.toString());
                add.setString(2, receiver.toString());
                add.executeUpdate();

                add.setString(1, receiver.toString());
                add.setString(2, sender.toString());
                add.executeUpdate();
            }

            db.commit();
            return true;

        } catch (SQLException e) {
            try { db.rollback(); } catch (SQLException ignored) {}
            e.printStackTrace();
            return false;
        } finally {
            try { db.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    public boolean denyRequest(UUID receiver, UUID sender) {
        try (PreparedStatement ps = db.prepareStatement(
                "DELETE FROM newhorizon_friend_requests WHERE sender_uuid = ? AND receiver_uuid = ?")) {
            ps.setString(1, sender.toString());
            ps.setString(2, receiver.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<UUID> getPendingRequests(UUID receiver) {
        List<UUID> list = new ArrayList<>();
        try (PreparedStatement ps = db.prepareStatement(
                "SELECT sender_uuid FROM newhorizon_friend_requests WHERE receiver_uuid = ?")) {
            ps.setString(1, receiver.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(UUID.fromString(rs.getString("sender_uuid")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // === AMITIÉS ===

    public List<UUID> getFriends(UUID player) {
        List<UUID> list = new ArrayList<>();
        try (PreparedStatement ps = db.prepareStatement(
                "SELECT friend_uuid FROM newhorizon_friends WHERE player_uuid = ?")) {
            ps.setString(1, player.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(UUID.fromString(rs.getString("friend_uuid")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean areFriends(UUID player, UUID target) {
        try (PreparedStatement ps = db.prepareStatement(
                "SELECT * FROM newhorizon_friends WHERE player_uuid = ? AND friend_uuid = ?")) {
            ps.setString(1, player.toString());
            ps.setString(2, target.toString());
            return ps.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeFriend(UUID player, UUID target) {
        try (PreparedStatement ps = db.prepareStatement(
                "DELETE FROM newhorizon_friends WHERE (player_uuid = ? AND friend_uuid = ?) OR (player_uuid = ? AND friend_uuid = ?)")) {
            ps.setString(1, player.toString());
            ps.setString(2, target.toString());
            ps.setString(3, target.toString());
            ps.setString(4, player.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
