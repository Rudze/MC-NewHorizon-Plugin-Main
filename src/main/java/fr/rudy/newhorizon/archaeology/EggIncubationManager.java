package fr.rudy.newhorizon.archaeology;

import dev.lone.itemsadder.api.CustomBlock;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Particle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EggIncubationManager {

    private final Plugin plugin;
    private final Connection database;
    private final Map<Location, Integer> incubationLevels = new HashMap<>();
    private final Set<Location> incubatingEggs = new HashSet<>();

    public EggIncubationManager(Plugin plugin) {
        this.plugin = plugin;
        this.database = fr.rudy.newhorizon.Main.get().getDatabase();
        loadIncubatingEggs();
    }

    private void loadIncubatingEggs() {
        try (PreparedStatement ps = database.prepareStatement("SELECT world, x, y, z, stage FROM newhorizon_egg_blocks")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Location loc = new Location(
                        Bukkit.getWorld(rs.getString("world")),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"));
                int stage = rs.getInt("stage");
                incubationLevels.put(loc, stage);
                incubatingEggs.add(loc);
                resumeIncubation(loc, stage);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void startIncubation(Block block) {
        Location loc = block.getLocation();
        CustomBlock baseBlock = CustomBlock.byAlreadyPlaced(block);
        if (baseBlock == null) return;

        String baseId = baseBlock.getNamespacedID();
        if (!baseId.startsWith("newhorizon:egg_")) return;

        String baseDinoName = baseId.replace("newhorizon:egg_", "").replaceAll("_\\d+", "");
        String mobName = baseDinoName + "baby";

        if (incubationLevels.containsKey(loc)) return;
        incubationLevels.put(loc, 0);
        incubatingEggs.add(loc);
        saveEgg(loc, 0);

        runIncubationTask(loc, baseDinoName, mobName);
    }

    private void resumeIncubation(Location loc, int initialStage) {
        Block block = loc.getBlock();
        CustomBlock baseBlock = CustomBlock.byAlreadyPlaced(block);
        if (baseBlock == null) return;

        String baseId = baseBlock.getNamespacedID();
        if (!baseId.startsWith("newhorizon:egg_")) return;

        String baseDinoName = baseId.replace("newhorizon:egg_", "").replaceAll("_\\d+", "");
        String mobName = baseDinoName + "baby";

        runIncubationTask(loc, baseDinoName, mobName);
    }

    private void runIncubationTask(Location loc, String baseDinoName, String mobName) {
        new BukkitRunnable() {
            int level = incubationLevels.getOrDefault(loc, -1);

            @Override
            public void run() {
                if (!incubationLevels.containsKey(loc)) {
                    cancel();
                    return;
                }

                level++;
                if (level == 1) {
                    replaceBlock(loc, "newhorizon:egg_" + baseDinoName + "_1");
                    loc.getWorld().playSound(loc, "minecraft:block.turtle_egg.crack", 1.0f, 1.0f);
                } else if (level == 2) {
                    replaceBlock(loc, "newhorizon:egg_" + baseDinoName + "_2");
                    loc.getWorld().playSound(loc, "minecraft:block.turtle_egg.crack", 1.0f, 1.2f);
                } else {
                    Block finalBlock = loc.getBlock();
                    CustomBlock customBlock = CustomBlock.byAlreadyPlaced(finalBlock);
                    if (customBlock != null) customBlock.remove();
                    else finalBlock.setType(Material.AIR);

                    loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.clone().add(0.5, 0.5, 0.5), 20);

                    try {
                        MythicBukkit.inst().getAPIHelper().spawnMythicMob(mobName, loc);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Erreur spawn MythicMob: " + mobName);
                    }

                    incubationLevels.remove(loc);
                    incubatingEggs.remove(loc);
                    deleteEgg(loc);
                    cancel();
                    return;
                }

                incubationLevels.put(loc, level);
                updateEggStage(loc, level);
            }
        }.runTaskTimer(plugin, 0, 2400);
    }

    private void replaceBlock(Location location, String customBlockId) {
        Block block = location.getBlock();
        block.setType(Material.AIR);
        CustomBlock customBlock = CustomBlock.getInstance(customBlockId);
        if (customBlock != null) customBlock.place(location);
    }

    private void saveEgg(Location loc, int stage) {
        try (PreparedStatement ps = database.prepareStatement(
                "INSERT OR REPLACE INTO newhorizon_egg_blocks (world, x, y, z, stage) VALUES (?, ?, ?, ?, ?)");) {
            ps.setString(1, loc.getWorld().getName());
            ps.setInt(2, loc.getBlockX());
            ps.setInt(3, loc.getBlockY());
            ps.setInt(4, loc.getBlockZ());
            ps.setInt(5, stage);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateEggStage(Location loc, int stage) {
        saveEgg(loc, stage);
    }

    public void cancelIncubation(Location loc) {
        incubationLevels.remove(loc);
        incubatingEggs.remove(loc);
        deleteEgg(loc);
    }

    private void deleteEgg(Location loc) {
        try (PreparedStatement ps = database.prepareStatement(
                "DELETE FROM newhorizon_egg_blocks WHERE world = ? AND x = ? AND y = ? AND z = ?")) {
            ps.setString(1, loc.getWorld().getName());
            ps.setInt(2, loc.getBlockX());
            ps.setInt(3, loc.getBlockY());
            ps.setInt(4, loc.getBlockZ());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isIncubating(Location loc) {
        return incubationLevels.containsKey(loc);
    }
}
