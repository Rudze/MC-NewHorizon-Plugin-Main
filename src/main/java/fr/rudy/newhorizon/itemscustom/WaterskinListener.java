package fr.rudy.newhorizon.itemscustom;

import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.PlayerHudsHolderWrapper;
import dev.lone.itemsadder.api.FontImages.PlayerQuantityHudWrapper;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class WaterskinListener implements Listener {

    @EventHandler
    public void onUseWaterskin(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        CustomStack stack = CustomStack.byItemStack(item);
        if (stack == null || !stack.getNamespacedID().equals("newhorizon:waterskin")) return;

        event.setCancelled(true); // Annule l’action par défaut

        int durability = stack.getDurability();
        if (durability <= 0) {
            return;
        }

        PlayerHudsHolderWrapper huds = new PlayerHudsHolderWrapper(player);
        PlayerQuantityHudWrapper thirstHud = new PlayerQuantityHudWrapper(huds, "newhorizon:thirst_bar");

        float current = thirstHud.getFloatValue();
        float newValue = Math.min(current + 0.5f, 10.0f); // max défini dans le HUD
        thirstHud.setFloatValue(newValue);

        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1f, 1f);

        stack.setDurability(durability - 1);
    }
}
