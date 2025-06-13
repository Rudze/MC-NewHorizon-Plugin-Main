package fr.rudy.newhorizon.dialogue;

import fr.rudy.newhorizon.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DialogueManager {

    private static final Map<UUID, BukkitRunnable> pendingDialogs = new HashMap<>();

    public static void startDialogue(Player player, String npc, String step) {
        UUID uuid = player.getUniqueId();

        if (pendingDialogs.containsKey(uuid)) {
            cancelRunnable(uuid);
            openMenuImmediately(player, npc);
            return;
        }

        if (npc.equalsIgnoreCase("npc_emily") && step.equals("1")) {
            sendFormatted(player, ":portrait_emily:");
            sendFormatted(player, "              :speech_indicator:&f&l Emily");
            sendFormatted(player, "              &7Bienvenue sur &b&lNewHorizon&7, ");
            sendFormatted(player, "              &7Je suis votre guide ici.");
            sendClickableLine(player, "menu_emily", "&7Laissez-moi vous aider ");
            sendFormatted(player, "        ");

            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dmenu open menu_emily " + player.getName());
                    pendingDialogs.remove(uuid);
                }
            };
            pendingDialogs.put(uuid, runnable);
            runnable.runTaskLater(Main.get(), 70L);

            Location startLoc = player.getLocation().clone();
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || !pendingDialogs.containsKey(uuid)) {
                        cancel();
                        return;
                    }

                    if (player.getLocation().distanceSquared(startLoc) > 4) {
                        player.sendMessage("§f\uE01D§c Dialogue annulé, vous vous êtes éloigné.");
                        cancelRunnable(uuid);
                        cancel();
                    }
                }
            }.runTaskTimer(Main.get(), 0L, 5L);
        }

        if (npc.equalsIgnoreCase("npc_alex") && step.equals("1")) {
            sendFormatted(player, ":portrait_alex:");
            sendFormatted(player, "              :speech_indicator:&f&l Alex");
            sendFormatted(player, "              &7Bienvenue au Casino de &b&lNewHorizon&7 !");
            sendFormatted(player, "              &7En plus c'est votre jour de chance.");
            sendFormatted(player, "              &7Une seule règle : &cpariez gros &7pour gagner plus.");
            sendClickableLine(player, "casino_menu", "&7Que souhaitez vous faire ? ");
            sendFormatted(player, "        ");

            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dmenu open casino_menu " + player.getName());
                    pendingDialogs.remove(uuid);
                }
            };
            pendingDialogs.put(uuid, runnable);
            runnable.runTaskLater(Main.get(), 70L);

            Location startLoc = player.getLocation().clone();
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || !pendingDialogs.containsKey(uuid)) {
                        cancel();
                        return;
                    }

                    if (player.getLocation().distanceSquared(startLoc) > 4) {
                        player.sendMessage("§f\uE01D§c Dialogue annulé, vous vous êtes éloigné.");
                        cancelRunnable(uuid);
                        cancel();
                    }
                }
            }.runTaskTimer(Main.get(), 0L, 5L);
        }

        if (npc.equalsIgnoreCase("npc_croupier") && step.equals("1")) {
            sendFormatted(player, ":portrait_croupier:");
            sendFormatted(player, "              :speech_indicator:&f&l Croupier");
            sendFormatted(player, "              &7Bienvenue à la table de &a&lBlackjack&7.");
            sendFormatted(player, "              &7Pour participer, cliquer sur la table.");
            sendFormatted(player, "              &7Bonne chance, et que la chance soit avec vous.");
            sendFormatted(player, "        ");
        }
    }

    private static void cancelRunnable(UUID uuid) {
        BukkitRunnable r = pendingDialogs.remove(uuid);
        if (r != null) r.cancel();
    }

    private static void openMenuImmediately(Player player, String npc) {
        String menuName = null;
        if (npc.equalsIgnoreCase("npc_emily")) {
            menuName = "menu_emily";
        } else if (npc.equalsIgnoreCase("npc_alex")) {
            menuName = "casino_menu";
        }

        if (menuName != null) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dmenu open " + menuName + " " + player.getName());
        }
    }

    private static void sendFormatted(Player player, String message) {
        player.sendMessage(applyHexColor(message));
    }

    private static String applyHexColor(String message) {
        StringBuilder builder = new StringBuilder();
        char[] chars = message.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '<' && i + 7 < chars.length && chars[i + 1] == '#' && chars[i + 8] == '>') {
                String hex = message.substring(i + 1, i + 8);
                builder.append(ChatColor.of(hex));
                i += 8;
            } else if (chars[i] == '&' && i + 1 < chars.length) {
                builder.append(ChatColor.translateAlternateColorCodes('&', "&" + chars[i + 1]));
                i++;
            } else {
                builder.append(chars[i]);
            }
        }

        return builder.toString();
    }

    private static void sendClickableLine(Player player, String menuName, String messageBeforeButton) {
        TextComponent fullLine = new TextComponent("              " + applyHexColor(messageBeforeButton));
        TextComponent menuPart = new TextComponent("Menu →");

        menuPart.setColor(ChatColor.of("#ffacd5"));
        menuPart.setBold(true);
        menuPart.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dmenu open " + menuName + " " + player.getName()));
        menuPart.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Ouvrir le menu").color(ChatColor.of("#ffacd5")).create()));

        fullLine.addExtra(menuPart);
        player.spigot().sendMessage(fullLine);
    }
}
