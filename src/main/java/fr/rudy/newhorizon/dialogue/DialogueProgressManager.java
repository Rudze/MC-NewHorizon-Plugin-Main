/*
 * Decompiled with CFR 0.152.
 */
package fr.rudy.newhorizon.dialogue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DialogueProgressManager {
    private final Connection database;

    public DialogueProgressManager(Connection database) {
        this.database = database;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public int getPlayerDialogueStep(UUID uuid, String npc) {
        try (PreparedStatement st = this.database.prepareStatement("SELECT step FROM newhorizon_player_dialogues WHERE uuid = ? AND npc = ?");){
            st.setString(1, uuid.toString());
            st.setString(2, npc);
            ResultSet rs = st.executeQuery();
            if (!rs.next()) return 0;
            int n = rs.getInt("step");
            return n;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setPlayerDialogueStep(UUID uuid, String npc, int step) {
        try (PreparedStatement st = this.database.prepareStatement("INSERT INTO newhorizon_player_dialogues (uuid, npc, step) VALUES (?, ?, ?) ON CONFLICT(uuid, npc) DO UPDATE SET step = excluded.step");){
            st.setString(1, uuid.toString());
            st.setString(2, npc);
            st.setInt(3, step);
            st.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAllPlayerDialogues(UUID uuid) {
        try (PreparedStatement st = this.database.prepareStatement("DELETE FROM newhorizon_player_dialogues WHERE uuid = ?");){
            st.setString(1, uuid.toString());
            st.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

