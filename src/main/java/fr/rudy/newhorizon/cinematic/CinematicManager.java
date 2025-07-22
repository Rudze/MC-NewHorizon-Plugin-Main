/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.plugin.Plugin
 */
package fr.rudy.newhorizon.cinematic;

import fr.rudy.newhorizon.cinematic.CinematicMoveListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.bukkit.plugin.Plugin;

public class CinematicManager {
    private final Plugin plugin;
    private final Connection connection;

    public CinematicManager(Plugin plugin, Connection connection) {
        this.plugin = plugin;
        this.connection = connection;
        new CinematicMoveListener(this);
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public boolean hasPlayedCinematic(UUID uuid, String cinematicId) {
        try (PreparedStatement stmt = this.connection.prepareStatement("SELECT played FROM newhorizon_cinematics WHERE uuid = ? AND cinematic_id = ?");){
            boolean bl;
            block14: {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, cinematicId);
                ResultSet rs = stmt.executeQuery();
                try {
                    boolean bl2 = bl = rs.next() && rs.getBoolean("played");
                    if (rs == null) break block14;
                }
                catch (Throwable throwable) {
                    if (rs != null) {
                        try {
                            rs.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                rs.close();
            }
            return bl;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public void markCinematicAsPlayed(UUID uuid, String cinematicId) {
        try (PreparedStatement stmt = this.connection.prepareStatement("REPLACE INTO newhorizon_cinematics (uuid, cinematic_id, played) VALUES (?, ?, 1)");){
            stmt.setString(1, uuid.toString());
            stmt.setString(2, cinematicId);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

