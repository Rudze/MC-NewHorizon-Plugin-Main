package fr.rudy.newhorizon.itemscustom;

import dev.lone.itemsadder.api.FontImages.PlayerHudsHolderWrapper;
import dev.lone.itemsadder.api.FontImages.PlayerQuantityHudWrapper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class PotionDrinkListener implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.POTION) {
            Player player = event.getPlayer();

            ItemMeta meta = item.getItemMeta();
            boolean isPlainWater = (meta != null && !meta.hasDisplayName() && !meta.hasLore());

            // 50 % de chance d'appliquer l'effet de faim si c'est juste de l'eau
            if (isPlainWater && random.nextDouble() < 0.4) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 20 * 20, 0));
            }

            // Mise à jour du HUD de soif (ItemsAdder)
            PlayerHudsHolderWrapper hudsHolder = new PlayerHudsHolderWrapper(player);
            PlayerQuantityHudWrapper thirstHud = new PlayerQuantityHudWrapper(hudsHolder, "newhorizon:thirst_bar");

            float currentThirst = thirstHud.getFloatValue();
            float maxThirst = 20.0f;
            float newThirst = Math.min(currentThirst + 1, maxThirst);
            thirstHud.setFloatValue(newThirst);
        }
    }
}
