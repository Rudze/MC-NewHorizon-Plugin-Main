package fr.rudy.newhorizon.archaeology;

import dev.lone.itemsadder.api.CustomStack;
import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.utils.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ArchaeologistListener implements Listener {

    Main plugin = Main.get();

    private static final Map<String, Double> dnaChances = new HashMap<>();
    static {
        dnaChances.put("newhorizon:tyrannosaurus_dna", 5.0);
        dnaChances.put("newhorizon:ankylosaurus_dna", 15.0);
        dnaChances.put("newhorizon:brachiosaurus_dna", 10.0);
        dnaChances.put("newhorizon:dilophosaurus_dna", 12.0);
        dnaChances.put("newhorizon:parasaurolophus_dna", 14.0);
        dnaChances.put("newhorizon:triceratops_dna", 8.0);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(ArchaeologistGUI.GUI_TITLE)) return;

        int rawSlot = event.getRawSlot();

        // Click dans le GUI uniquement
        if (rawSlot < 27) {

            // Slot 4 autorisé (dépôt fossil)
            if (rawSlot == 4) return;

            // Slot 8 = bouton Wiki
            if (rawSlot == 8) {
                event.setCancelled(true);
                player.closeInventory();
                player.performCommand("wiki");
                return;
            }

            // Slots 21, 22, 23 = analyse
            if (rawSlot == 21 || rawSlot == 22 || rawSlot == 23) {
                event.setCancelled(true);

                ItemStack fossil = event.getInventory().getItem(4);
                if (fossil == null) {
                    MessageUtil.sendMessage(player, plugin.getPrefixError(), "§cAucun fossile à analyser !");
                    return;
                }

                CustomStack stack = CustomStack.byItemStack(fossil);
                if (stack == null || !stack.getNamespacedID().equalsIgnoreCase("newhorizon:fossil")) {
                    MessageUtil.sendMessage(player, plugin.getPrefixError(), "§cCe n'est pas un fossile valide !");
                    return;
                }

                // Consommer 1 fossile
                fossil.setAmount(fossil.getAmount() - 1);
                if (fossil.getAmount() <= 0) event.getInventory().setItem(4, null);

                // Récompense
                Random rand = new Random();
                if (rand.nextDouble() <= 0.70) {
                    player.getInventory().addItem(new ItemStack(Material.SAND));
                    MessageUtil.sendMessage(player, plugin.getPrefixInfo(), "Ce fossile ne contient pas d'ADN");
                } else {
                    String dnaID = getRandomDNA();
                    if (dnaID != null) {
                        CustomStack dnaStack = CustomStack.getInstance(dnaID);
                        if (dnaStack != null) {
                            player.getInventory().addItem(dnaStack.getItemStack());
                            MessageUtil.sendMessage(player, plugin.getPrefixInfo(), "Analyse terminée ! Vous avez trouvé de l’ADN : §d" + dnaID.replace("newhorizon:", ""));
                        } else {
                            MessageUtil.sendMessage(player, plugin.getPrefixError(), "§cErreur lors de la génération de l'ADN (" + dnaID + ")");
                        }
                    }
                }
                return;
            }

            // Tous les autres slots du GUI sont bloqués
            event.setCancelled(true);
        }
    }

    private String getRandomDNA() {
        double totalWeight = dnaChances.values().stream().mapToDouble(Double::doubleValue).sum();
        double r = new Random().nextDouble() * totalWeight;
        double cumulative = 0.0;

        for (Map.Entry<String, Double> entry : dnaChances.entrySet()) {
            cumulative += entry.getValue();
            if (r <= cumulative) return entry.getKey();
        }
        return null;
    }
}
