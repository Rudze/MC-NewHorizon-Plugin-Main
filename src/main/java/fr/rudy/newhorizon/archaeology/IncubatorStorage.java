package fr.rudy.newhorizon.archaeology;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.sql.*;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

public class IncubatorStorage {

    private final Connection connection;

    public IncubatorStorage(Plugin plugin) {
        this.connection = fr.rudy.newhorizon.Main.get().getDatabase();
    }

    public void saveIncubator(Location loc) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR IGNORE INTO newhorizon_incubator_blocks (world, x, y, z) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, loc.getWorld().getName());
            ps.setInt(2, loc.getBlockX());
            ps.setInt(3, loc.getBlockY());
            ps.setInt(4, loc.getBlockZ());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Set<Location> loadIncubators() {
        Set<Location> result = new HashSet<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT world, x, y, z FROM newhorizon_incubator_blocks")) {
            while (rs.next()) {
                World world = Bukkit.getWorld(rs.getString("world"));
                if (world != null) {
                    result.add(new Location(world, rs.getInt("x"), rs.getInt("y"), rs.getInt("z")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void saveInventory(Location loc, Inventory inv) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE newhorizon_incubator_blocks SET data = ? WHERE world = ? AND x = ? AND y = ? AND z = ?")) {
            ps.setString(1, serializeInventory(inv));
            ps.setString(2, loc.getWorld().getName());
            ps.setInt(3, loc.getBlockX());
            ps.setInt(4, loc.getBlockY());
            ps.setInt(5, loc.getBlockZ());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Inventory loadInventory(Location loc) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT data FROM newhorizon_incubator_blocks WHERE world = ? AND x = ? AND y = ? AND z = ?")) {
            ps.setString(1, loc.getWorld().getName());
            ps.setInt(2, loc.getBlockX());
            ps.setInt(3, loc.getBlockY());
            ps.setInt(4, loc.getBlockZ());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String data = rs.getString("data");
                if (data != null) return deserializeInventory(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String serializeInventory(Inventory inventory) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             BukkitObjectOutputStream out = new BukkitObjectOutputStream(bos)) {
            out.writeInt(inventory.getSize());
            for (ItemStack item : inventory.getContents()) out.writeObject(item);
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Inventory deserializeInventory(String data) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(data));
             BukkitObjectInputStream in = new BukkitObjectInputStream(bis)) {
            int size = in.readInt();
            Inventory inventory = Bukkit.createInventory(null, size, IncubatorGUI.INCUBATOR_GUI_TITLE);
            ItemStack[] contents = new ItemStack[size];
            for (int i = 0; i < size; i++) contents[i] = (ItemStack) in.readObject();
            inventory.setContents(contents);
            return inventory;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void delete(Location loc) {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM newhorizon_incubator_blocks WHERE world = ? AND x = ? AND y = ? AND z = ?")) {
            ps.setString(1, loc.getWorld().getName());
            ps.setInt(2, loc.getBlockX());
            ps.setInt(3, loc.getBlockY());
            ps.setInt(4, loc.getBlockZ());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
