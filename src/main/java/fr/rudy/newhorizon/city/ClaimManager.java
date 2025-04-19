package fr.rudy.newhorizon.city;

import fr.rudy.newhorizon.Main;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClaimManager {

    private final Connection db = Main.get().getDatabase();

    // ✅ Revendiquer un chunk
    public boolean claimChunk(int cityId, Chunk chunk) {
        if (isChunkClaimed(chunk)) return false;

        try (PreparedStatement ps = db.prepareStatement(
                "INSERT INTO newhorizon_city_claims (chunk_x, chunk_z, world, city_id) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, chunk.getX());
            ps.setInt(2, chunk.getZ());
            ps.setString(3, chunk.getWorld().getName());
            ps.setInt(4, cityId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 🔍 Vérifie si un chunk est revendiqué
    public boolean isChunkClaimed(Chunk chunk) {
        try (PreparedStatement ps = db.prepareStatement(
                "SELECT 1 FROM newhorizon_city_claims WHERE chunk_x = ? AND chunk_z = ? AND world = ?")) {
            ps.setInt(1, chunk.getX());
            ps.setInt(2, chunk.getZ());
            ps.setString(3, chunk.getWorld().getName());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 🔍 Récupère l’ID de la ville propriétaire du chunk
    public Integer getChunkOwnerId(Location loc) {
        Chunk chunk = loc.getChunk();
        try (PreparedStatement ps = db.prepareStatement(
                "SELECT city_id FROM newhorizon_city_claims WHERE chunk_x = ? AND chunk_z = ? AND world = ?")) {
            ps.setInt(1, chunk.getX());
            ps.setInt(2, chunk.getZ());
            ps.setString(3, chunk.getWorld().getName());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("city_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ❌ Libère un chunk (si la ville est bien propriétaire)
    public boolean unclaimChunk(int cityId, Chunk chunk) {
        try (PreparedStatement ps = db.prepareStatement(
                "DELETE FROM newhorizon_city_claims WHERE chunk_x = ? AND chunk_z = ? AND world = ? AND city_id = ?")) {
            ps.setInt(1, chunk.getX());
            ps.setInt(2, chunk.getZ());
            ps.setString(3, chunk.getWorld().getName());
            ps.setInt(4, cityId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
