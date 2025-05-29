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

    public class AnalyzerStorage {

        private final Connection connection;

        public AnalyzerStorage(Plugin plugin) {
            this.connection = fr.rudy.newhorizon.Main.get().getDatabase();
        }

        public void saveAnalyzer(Location loc) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT OR IGNORE INTO newhorizon_analyzer_blocks (world, x, y, z) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, loc.getWorld().getName());
                ps.setInt(2, loc.getBlockX());
                ps.setInt(3, loc.getBlockY());
                ps.setInt(4, loc.getBlockZ());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public Set<Location> loadAnalyzers() {
            Set<Location> analyzers = new HashSet<>();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT world, x, y, z FROM newhorizon_analyzer_blocks")) {
                while (rs.next()) {
                    String worldName = rs.getString("world");
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    int z = rs.getInt("z");
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        analyzers.add(new Location(world, x, y, z));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return analyzers;
        }

        public void saveInventory(Location loc, Inventory inventory) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE newhorizon_analyzer_blocks SET data = ? WHERE world = ? AND x = ? AND y = ? AND z = ?")) {
                ps.setString(1, serializeInventory(inventory));
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
                    "SELECT data FROM newhorizon_analyzer_blocks WHERE world = ? AND x = ? AND y = ? AND z = ?")) {
                ps.setString(1, loc.getWorld().getName());
                ps.setInt(2, loc.getBlockX());
                ps.setInt(3, loc.getBlockY());
                ps.setInt(4, loc.getBlockZ());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String data = rs.getString("data");
                    if (data != null) {
                        return deserializeInventory(data);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String serializeInventory(Inventory inventory) {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                 BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
                dataOutput.writeInt(inventory.getSize());
                for (ItemStack item : inventory.getContents()) {
                    dataOutput.writeObject(item);
                }
                return Base64.getEncoder().encodeToString(outputStream.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        private Inventory deserializeInventory(String data) {
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
                 BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
                int size = dataInput.readInt();
                Inventory inventory = Bukkit.createInventory(null, size, AnalyzerGUI.ANALYZER_GUI_TITLE);
                ItemStack[] items = new ItemStack[size];
                for (int i = 0; i < size; i++) {
                    items[i] = (ItemStack) dataInput.readObject();
                }
                inventory.setContents(items);
                return inventory;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        public void close() {
            // No action needed
        }

        public void deleteAnalyzer(Location loc) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM newhorizon_analyzer_blocks WHERE world = ? AND x = ? AND y = ? AND z = ?")) {
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
