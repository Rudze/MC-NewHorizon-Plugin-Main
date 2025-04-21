package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.itemscustom.CustomItems;
import fr.rudy.newhorizon.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CustomItemsCommand implements CommandExecutor {

    private final Main plugin;

    public CustomItemsCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 2) {
            MessageUtil.sendMessage(sender, plugin.getPrefixError(), "Utilisation: /customitems <item> <joueur>");
            return true;
        }

        String itemName = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);

        if (target == null || !target.isOnline()) {
            MessageUtil.sendMessage(sender, plugin.getPrefixError(), "Joueur introuvable ou hors ligne.");
            return true;
        }

        CustomItems customItems = plugin.getCustomItems();
        ItemStack item = null;

        if (itemName.equals("potionvol")) {
            item = customItems.getFlightPotion();
        } else {
            MessageUtil.sendMessage(sender, plugin.getPrefixError(), "Item inconnu: " + itemName);
            return true;
        }

        target.getInventory().addItem(item);
        return true;
    }
}
