package fr.rudy.newhorizon.itemscustom;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.List;

public class CustomItems {

    private final JavaPlugin plugin;

    public CustomItems(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack getFlightPotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();

        meta.setDisplayName("§b§lPotion de Vol");

        // Utiliser une potion sans effet pour éviter le "No Effects"
        meta.setBasePotionData(new PotionData(PotionType.WATER)); // ou UNCRAFTABLE si besoin
        meta.setColor(Color.AQUA);
        meta.setLore(List.of(
                "§7Effet: §fPermet de voler",
                "§7Durée: §f10 minutes",
                "",
                "§8» Boire pour activer l'effet"
        ));

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "flight_potion"),
                PersistentDataType.INTEGER,
                1
        );

        potion.setItemMeta(meta);
        return potion;
    }
}
