/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.md_5.bungee.api.ChatColor
 *  net.md_5.bungee.api.chat.BaseComponent
 *  net.md_5.bungee.api.chat.ClickEvent
 *  net.md_5.bungee.api.chat.ClickEvent$Action
 *  net.md_5.bungee.api.chat.ComponentBuilder
 *  net.md_5.bungee.api.chat.HoverEvent
 *  net.md_5.bungee.api.chat.HoverEvent$Action
 *  net.md_5.bungee.api.chat.TextComponent
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package fr.rudy.newhorizon.dialogue;

import fr.rudy.newhorizon.Main;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DialogueManager {
    private static final Map<UUID, BukkitRunnable> pendingDialogs = new HashMap<UUID, BukkitRunnable>();

    public static void skipDialogue(Player player) {
        UUID uuid = player.getUniqueId();
        BukkitRunnable r = pendingDialogs.remove(uuid);
        if (r != null) {
            r.cancel();
        }
    }

    public static void startDialogue(Player player, String npc, String stepArg, boolean skip) {
        UUID uuid = player.getUniqueId();
        if (pendingDialogs.containsKey(uuid) && !skip) {
            DialogueManager.cancelRunnable(uuid);
            switch (npc.toLowerCase()) {
                case "npc_blue": {
                    if ("1".equals(stepArg)) {
                        if (Main.get().getDialogueProgressManager().getPlayerDialogueStep(uuid, npc) < 2) {
                            DialogueManager.runBlue1(player, uuid, npc);
                            break;
                        }
                        DialogueManager.runBlue10(player);
                        break;
                    }
                    if (!"10".equals(stepArg)) break;
                    DialogueManager.runBlue10(player);
                    break;
                }
                case "npc_alex": {
                    if (!"1".equals(stepArg)) break;
                    DialogueManager.runAlex(player);
                    break;
                }
                case "npc_fred": {
                    if (!"1".equals(stepArg)) break;
                    DialogueManager.runFred(player);
                    break;
                }
                case "npc_death": {
                    if (!"1".equals(stepArg)) break;
                    DialogueManager.runDeathMenu(player);
                }
            }
            return;
        }
        if (skip) {
            DialogueManager.cancelRunnable(uuid);
        }
        int lastStep = Main.get().getDialogueProgressManager().getPlayerDialogueStep(uuid, npc);
        String step = stepArg;
        switch (npc.toLowerCase()) {
            case "npc_blue": {
                if ("1".equals(step)) {
                    if (skip) {
                        DialogueManager.runBlue1(player, uuid, npc);
                        break;
                    }
                    if (lastStep >= 2) {
                        DialogueManager.startDialogue(player, npc, "10", false);
                        break;
                    }
                    DialogueManager.sendFormatted(player, ":portrait_blue:");
                    DialogueManager.sendFormatted(player, "              :speech_indicator:&f&l ?");
                    DialogueManager.sendFormatted(player, "              &7Bonjour et bienvenue sur &b&lNewHorizon&7,");
                    DialogueManager.sendFormatted(player, "              &7" + player.getName() + ", pr\u00eat pour une petite");
                    DialogueManager.sendClickableLine(player, "dialogue " + player.getName() + " npc_blue 1 true", "&7visite guid\u00e9e ? ", "Cin\u00e9matique \u2192", "Lancer la cin\u00e9matique");
                    DialogueManager.sendFormatted(player, "              ");
                    DialogueManager.scheduleBlue1(player, uuid, npc);
                    DialogueManager.startLocationTracking(player, uuid);
                    break;
                }
                if ("2".equals(step)) {
                    DialogueManager.sendFormatted(player, ":portrait_blue:");
                    DialogueManager.sendFormatted(player, "              :speech_indicator:&f&l Blue");
                    DialogueManager.sendFormatted(player, "              &7Pour commencer, je suis &b&lBlue");
                    DialogueManager.sendFormatted(player, "              &7Je suis là pour t'accompagner sur");
                    DialogueManager.sendFormatted(player, "              &b&lNewHorizon&f !");
                    DialogueManager.sendFormatted(player, "              ");
                    break;
                }
                if ("3".equals(step)) {
                    DialogueManager.sendFormatted(player, ":portrait_blue:");
                    DialogueManager.sendFormatted(player, "              :speech_indicator:&f&l Blue");
                    DialogueManager.sendFormatted(player, "              &7Je te présente le capitaine &b&lFred");
                    DialogueManager.sendFormatted(player, "              &7accompagné de &b&lGhaston&7. Ils te permettront");
                    DialogueManager.sendFormatted(player, "              &7de voyager entre les régions de NewHorizon.");
                    DialogueManager.sendFormatted(player, "              ");
                    break;
                }
                if (!"10".equals(step)) break;
                if (skip) {
                    DialogueManager.runBlue10(player);
                    break;
                }
                DialogueManager.sendFormatted(player, ":portrait_blue:");
                DialogueManager.sendFormatted(player, "              :speech_indicator:&f&l Blue");
                DialogueManager.sendFormatted(player, "              &7Bienvenue sur &b&lNewHorizon&7,");
                DialogueManager.sendFormatted(player, "              &7je suis votre guide ici.");
                DialogueManager.sendClickableLine(player, "dialogue " + player.getName() + " npc_blue 10 true", "&7Laissez-moi vous aider ", "Menu \u2192", "Ouvrir le menu");
                DialogueManager.sendFormatted(player, "              ");
                DialogueManager.scheduleBlue10(player);
                DialogueManager.startLocationTracking(player, uuid);
                break;
            }
            case "npc_alex": {
                if (!"1".equals(step)) break;
                if (skip) {
                    DialogueManager.runAlex(player);
                    break;
                }
                DialogueManager.sendFormatted(player, ":portrait_alex:");
                DialogueManager.sendFormatted(player, "              :speech_indicator:&f&l Alex");
                DialogueManager.sendFormatted(player, "              &7Bienvenue au Casino de &b&lNewHorizon&7 !");
                DialogueManager.sendFormatted(player, "              &7En plus c'est votre jour de chance.");
                DialogueManager.sendFormatted(player, "              &7Et surtout pariez gros pour gagner plus.");
                DialogueManager.sendClickableLine(player, "dialogue " + player.getName() + " npc_alex 1 true", "&7Que souhaitez vous faire ? ", "Menu \u2192", "Ouvrir le menu");
                DialogueManager.scheduleAlex(player);
                DialogueManager.startLocationTracking(player, uuid);
                break;
            }
            case "npc_fred": {
                if (!"1".equals(step)) break;
                if (skip) {
                    DialogueManager.runFred(player);
                    break;
                }
                DialogueManager.sendFormatted(player, ":portrait_fred:");
                DialogueManager.sendFormatted(player, "              :speech_indicator:&f&l Capitaine Fred");
                DialogueManager.sendFormatted(player, "              &7Hey matelot ! Moi et &b&lGhaston&7");
                DialogueManager.sendFormatted(player, "              &7pouvons t'emmener o\u00f9 tu le souhaites.");
                DialogueManager.sendClickableLine(player, "dialogue " + player.getName() + " npc_fred 1 true", "&7Je te d\u00e9pose o\u00f9 ? ", "Menu \u2192", "Ouvrir le menu");
                DialogueManager.sendFormatted(player, "              ");
                DialogueManager.scheduleFred(player);
                DialogueManager.startLocationTracking(player, uuid);
                break;
            }
            case "npc_death": {
                if (!"1".equals(step)) break;
                if (skip) {
                    DialogueManager.runDeathMenu(player);
                    break;
                }
                DialogueManager.sendFormatted(player, ":portrait_death:");
                DialogueManager.sendFormatted(player, "              :speech_indicator:&f&l La faucheuse");
                DialogueManager.sendFormatted(player, "              &7Te revoil\u00e0 parmis les vivants");
                DialogueManager.sendFormatted(player, "              &7Contre quelques pi\u00e8ces d'or,");
                DialogueManager.sendFormatted(player, "              &7je peux te ramener \u00e0 ta derni\u00e8re mort.");
                DialogueManager.sendClickableLine(player, "dialogue " + player.getName() + " npc_death 1 true", "&7Que choisis-tu ? ", "Menu \u2192", "Ouvrir le menu de r\u00e9surrection");
                DialogueManager.scheduleDeathMenu(player);
                DialogueManager.startLocationTracking(player, uuid);
                break;
            }
            case "npc_croupier": {
                if (!"1".equals(step)) break;
                DialogueManager.sendFormatted(player, ":portrait_croupier:");
                DialogueManager.sendFormatted(player, "              :speech_indicator:&f&l Croupier");
                DialogueManager.sendFormatted(player, "              &7Bienvenue \u00e0 la table de &a&lBlackjack&7.");
                DialogueManager.sendFormatted(player, "              &7Pour participer, cliquer sur la table.");
                DialogueManager.sendFormatted(player, "              &7Bonne chance, et que la chance soit");
                DialogueManager.sendFormatted(player, "              &7avec vous.");
            }
        }
    }

    private static void runBlue1(Player player, UUID uuid, String npc) {
        player.performCommand("tw cinematic start first_join " + player.getName());
        Bukkit.getScheduler().runTask((Plugin)Main.get(), () -> Main.get().getDialogueProgressManager().setPlayerDialogueStep(uuid, npc, 2));
    }

    private static void scheduleBlue1(final Player player, final UUID uuid, final String npc) {
        BukkitRunnable r = new BukkitRunnable(){

            public void run() {
                if (pendingDialogs.containsKey(uuid)) {
                    DialogueManager.runBlue1(player, uuid, npc);
                    pendingDialogs.remove(uuid);
                }
            }
        };
        pendingDialogs.put(uuid, r);
        r.runTaskLater((Plugin)Main.get(), 70L);
    }

    private static void runDeathMenu(Player player) {
        Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)("dmenu open death " + player.getName()));
    }

    private static void scheduleDeathMenu(final Player player) {
        final UUID uuid = player.getUniqueId();
        BukkitRunnable r = new BukkitRunnable(){

            public void run() {
                if (pendingDialogs.containsKey(uuid)) {
                    DialogueManager.runDeathMenu(player);
                    pendingDialogs.remove(uuid);
                }
            }
        };
        pendingDialogs.put(uuid, r);
        r.runTaskLater((Plugin)Main.get(), 70L);
    }

    private static void runBlue10(Player player) {
        Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)("dmenu open menu_blue " + player.getName()));
    }

    private static void scheduleBlue10(final Player player) {
        BukkitRunnable r = new BukkitRunnable(){

            public void run() {
                if (pendingDialogs.containsKey(player.getUniqueId())) {
                    DialogueManager.runBlue10(player);
                    pendingDialogs.remove(player.getUniqueId());
                }
            }
        };
        pendingDialogs.put(player.getUniqueId(), r);
        r.runTaskLater((Plugin)Main.get(), 70L);
    }

    private static void runAlex(Player player) {
        Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)("dmenu open casino_menu " + player.getName()));
    }

    private static void scheduleAlex(final Player player) {
        BukkitRunnable r = new BukkitRunnable(){

            public void run() {
                if (pendingDialogs.containsKey(player.getUniqueId())) {
                    DialogueManager.runAlex(player);
                    pendingDialogs.remove(player.getUniqueId());
                }
            }
        };
        pendingDialogs.put(player.getUniqueId(), r);
        r.runTaskLater((Plugin)Main.get(), 70L);
    }

    private static void runFred(Player player) {
        Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)("dmenu open navigation_build " + player.getName()));
    }

    private static void scheduleFred(final Player player) {
        BukkitRunnable r = new BukkitRunnable(){

            public void run() {
                if (pendingDialogs.containsKey(player.getUniqueId())) {
                    DialogueManager.runFred(player);
                    pendingDialogs.remove(player.getUniqueId());
                }
            }
        };
        pendingDialogs.put(player.getUniqueId(), r);
        r.runTaskLater((Plugin)Main.get(), 70L);
    }

    private static void cancelRunnable(UUID uuid) {
        BukkitRunnable r = pendingDialogs.remove(uuid);
        if (r != null) {
            try {
                r.cancel();
            }
            catch (IllegalStateException illegalStateException) {
                // empty catch block
            }
        }
    }

    private static void sendFormatted(Player player, String message) {
        player.sendMessage(DialogueManager.applyHexColor(message));
    }

    private static String applyHexColor(String message) {
        message = ChatColor.translateAlternateColorCodes((char)'&', (String)message);
        StringBuilder builder = new StringBuilder();
        char[] cs = message.toCharArray();
        for (int i = 0; i < cs.length; ++i) {
            if (cs[i] == '<' && i + 7 < cs.length && cs[i + 1] == '#' && cs[i + 8] == '>') {
                String hex = message.substring(i + 1, i + 8);
                try {
                    builder.append(ChatColor.of((String)hex));
                }
                catch (IllegalArgumentException e) {
                    builder.append(cs[i]);
                }
                i += 8;
                continue;
            }
            builder.append(cs[i]);
        }
        return builder.toString();
    }

    private static void sendClickableLine(Player player, String command, String before, String label, String hover) {
        TextComponent line = new TextComponent(DialogueManager.applyHexColor("              " + before));
        TextComponent button = new TextComponent(label);
        button.setColor(ChatColor.of((String)"#ffacd5"));
        button.setBold(Boolean.valueOf(true));
        button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(DialogueManager.applyHexColor("&f" + hover)).create()));
        line.addExtra((BaseComponent)button);
        player.spigot().sendMessage((BaseComponent)line);
    }

    private static void startLocationTracking(final Player player, final UUID uuid) {
        final Location start = player.getLocation().clone();
        new BukkitRunnable(){

            public void run() {
                if (!player.isOnline() || !pendingDialogs.containsKey(uuid)) {
                    this.cancel();
                    return;
                }
                if (player.getLocation().distanceSquared(start) > 9.0) {
                    player.sendMessage(DialogueManager.applyHexColor("&cDialogue annul\u00e9, vous vous \u00eates trop \u00e9loign\u00e9."));
                    DialogueManager.cancelRunnable(uuid);
                    this.cancel();
                }
            }
        }.runTaskTimer((Plugin)Main.get(), 0L, 10L);
    }
}

