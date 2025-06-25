package fr.rudy.newhorizon.chat;

import fr.rudy.newhorizon.Main;
import fr.rudy.newhorizon.utils.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class BlockedNamespacedCommandListener implements Listener {

    private final Main plugin = Main.get();

    private static final Set<String> BLOCKED_COMMANDS = new HashSet<>();
    static {
        BLOCKED_COMMANDS.add("plugins");
        BLOCKED_COMMANDS.add("pl");
        BLOCKED_COMMANDS.add("version");
        BLOCKED_COMMANDS.add("ver");
        BLOCKED_COMMANDS.add("about");
        BLOCKED_COMMANDS.add("bukkit:plugins");
        BLOCKED_COMMANDS.add("bukkit:ver");
        BLOCKED_COMMANDS.add("bukkit:about");
        BLOCKED_COMMANDS.add("bukkit:help");
        BLOCKED_COMMANDS.add("bukkit:reload");
        BLOCKED_COMMANDS.add("minecraft:me");
        BLOCKED_COMMANDS.add("minecraft:say");
        BLOCKED_COMMANDS.add("minecraft:kill");
        BLOCKED_COMMANDS.add("minecraft:reload");
        BLOCKED_COMMANDS.add("me");
        BLOCKED_COMMANDS.add("say");
        BLOCKED_COMMANDS.add("reload");
    }

    @EventHandler
    public void onBlockedCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("newhorizon.admin")) return;

        String msg = event.getMessage().toLowerCase(Locale.ROOT).trim();
        if (msg.startsWith("/")) msg = msg.substring(1);

        String base = msg.split(" ")[0];

        if (BLOCKED_COMMANDS.contains(base)) {
            event.setCancelled(true);
            MessageUtil.sendMessage(player, plugin.getPrefixError(), "Vous n'avez pas la permission d'exécuter cette commande.");
        }
    }
}
