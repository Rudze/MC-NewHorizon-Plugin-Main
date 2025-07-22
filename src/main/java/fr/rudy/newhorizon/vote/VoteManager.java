/*
 * Decompiled with CFR 0.152.
 */
package fr.rudy.newhorizon.vote;

import fr.rudy.newhorizon.Main;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class VoteManager {
    private final Main plugin;
    private final Connection connection;

    public VoteManager(Main plugin) {
        this.plugin = plugin;
        this.connection = plugin.getDatabase();
    }

    public void addVote(UUID uuid) {
        try {
            PreparedStatement stmt = this.connection.prepareStatement("INSERT INTO newhorizon_votes (uuid, pending_votes) VALUES (?, 1) ON CONFLICT(uuid) DO UPDATE SET pending_votes = pending_votes + 1");
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
            stmt = this.connection.prepareStatement("INSERT INTO newhorizon_vote_party (id, total_votes) VALUES (0, 1) ON CONFLICT(id) DO UPDATE SET total_votes = total_votes + 1");
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getPendingVotes(UUID uuid) {
        try {
            PreparedStatement stmt = this.connection.prepareStatement("SELECT pending_votes FROM newhorizon_votes WHERE uuid = ?");
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("pending_votes");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void clearVotes(UUID uuid) {
        try {
            PreparedStatement stmt = this.connection.prepareStatement("UPDATE newhorizon_votes SET pending_votes = 0 WHERE uuid = ?");
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getTotalVotes() {
        try {
            ResultSet rs = this.connection.createStatement().executeQuery("SELECT total_votes FROM newhorizon_vote_party WHERE id = 0");
            if (rs.next()) {
                return rs.getInt("total_votes");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void resetVoteParty() {
        try {
            this.connection.createStatement().executeUpdate("UPDATE newhorizon_vote_party SET total_votes = 0 WHERE id = 0");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

