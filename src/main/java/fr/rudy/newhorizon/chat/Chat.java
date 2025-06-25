package fr.rudy.newhorizon.chat;

import fr.rudy.newhorizon.Main;
import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Chat implements Listener {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private final Main plugin;
    private final LuckPerms luckPerms;

    public Chat(Main plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(final AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        final Player player = event.getPlayer();
        final String message = event.getMessage();

        final CachedMetaData metaData = this.luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
        final String group = metaData.getPrimaryGroup();

        String format = plugin.getConfig().getString(plugin.getConfig().getString("group-formats." + group) != null ? "group-formats." + group : "chat-format")
                .replace("{prefix}", metaData.getPrefix() != null ? metaData.getPrefix() : "")
                .replace("{suffix}", metaData.getSuffix() != null ? metaData.getSuffix() : "")
                .replace("{prefixes}", metaData.getPrefixes().keySet().stream().map(metaData.getPrefixes()::get).collect(Collectors.joining()))
                .replace("{suffixes}", metaData.getSuffixes().keySet().stream().map(metaData.getSuffixes()::get).collect(Collectors.joining()))
                .replace("{world}", player.getWorld().getName())
                .replace("{name}", player.getName())
                .replace("{displayname}", player.getDisplayName())
                .replace("{username-color}", metaData.getMetaValue("username-color") != null ? metaData.getMetaValue("username-color") : "")
                .replace("{message-color}", metaData.getMetaValue("message-color") != null ? metaData.getMetaValue("message-color") : "");

        format = colorize(translateHexColorCodes(plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")
                ? PlaceholderAPI.setPlaceholders(player, format)
                : format));

        // Rendu cliquable du pseudo
        String usernameColor = metaData.getMetaValue("username-color") != null ? metaData.getMetaValue("username-color") : "";
        String messageColor = metaData.getMetaValue("message-color") != null ? metaData.getMetaValue("message-color") : "";
        String prefix = metaData.getPrefix() != null ? metaData.getPrefix() : "";
        String suffix = metaData.getSuffix() != null ? metaData.getSuffix() : "";

        TextComponent nameComponent = new TextComponent(colorize(usernameColor + prefix + player.getName() + suffix));
        nameComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/profile " + player.getName()));

        TextComponent messageComponent = new TextComponent(" : " +
                (player.hasPermission("lpc.colorcodes") && player.hasPermission("lpc.rgbcodes")
                        ? colorize(translateHexColorCodes(message))
                        : player.hasPermission("lpc.colorcodes")
                        ? colorize(message)
                        : player.hasPermission("lpc.rgbcodes")
                        ? translateHexColorCodes(message)
                        : message));

        nameComponent.addExtra(messageComponent);

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.spigot().sendMessage(nameComponent);
        }

        Bukkit.getConsoleSender().sendMessage(ChatColor.stripColor(prefix + player.getName() + suffix + ": " + message));
    }

    private String colorize(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private String translateHexColorCodes(final String message) {
        final char colorChar = ChatColor.COLOR_CHAR;

        final Matcher matcher = HEX_PATTERN.matcher(message);
        final StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);

        while (matcher.find()) {
            final String group = matcher.group(1);

            matcher.appendReplacement(buffer, colorChar + "x"
                    + colorChar + group.charAt(0) + colorChar + group.charAt(1)
                    + colorChar + group.charAt(2) + colorChar + group.charAt(3)
                    + colorChar + group.charAt(4) + colorChar + group.charAt(5));
        }

        return matcher.appendTail(buffer).toString();
    }
}
