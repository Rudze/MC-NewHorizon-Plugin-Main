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
            // Open menu immediately if a dialogue is already active for this player
            // This is a safety measure to prevent multiple dialogues overlapping
            openMenuImmediately(player, npc);
            return;
        }

        // Dialogue for Emily
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

            startLocationTracking(player, uuid);
        }

        // Dialogue for Alex
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

            startLocationTracking(player, uuid);
        }

        // Dialogue for Croupier
        if (npc.equalsIgnoreCase("npc_croupier") && step.equals("1")) {
            sendFormatted(player, ":portrait_croupier:");
            sendFormatted(player, "              :speech_indicator:&f&l Croupier");
            sendFormatted(player, "              &7Bienvenue à la table de &a&lBlackjack&7.");
            sendFormatted(player, "              &7Pour participer, cliquer sur la table.");
            sendFormatted(player, "              &7Bonne chance, et que la chance soit avec vous.");
            sendFormatted(player, "        ");
            // No menu for Croupier based on current code, so no runnable for menu opening
            // But we still want to track location
            startLocationTracking(player, uuid);
        }

        // Dialogue for Fred - NEW NPC
        if (npc.equalsIgnoreCase("npc_fred") && step.equals("1")) {
            sendFormatted(player, ":portrait_fred:"); // Assuming you have a portrait for Fred
            sendFormatted(player, "              :speech_indicator:&f&l Capitaine Fred");
            sendFormatted(player, "              &7Hey matelot ! Moi et &b&lGhaston&7 pouvons");
            sendFormatted(player, "              &7t'emmener où tu le souhaites.");
            sendClickableLine(player, "navigation_build", "&7Je te dépose où ? "); // New menu for travel
            sendFormatted(player, "        ");

            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dmenu open navigation_build " + player.getName());
                    pendingDialogs.remove(uuid);
                }
            };
            pendingDialogs.put(uuid, runnable);
            runnable.runTaskLater(Main.get(), 70L);

            startLocationTracking(player, uuid);
        }
    }

    /**
     * Cancels any pending dialogue for a given player UUID.
     * @param uuid The UUID of the player.
     */
    private static void cancelRunnable(UUID uuid) {
        BukkitRunnable r = pendingDialogs.remove(uuid);
        if (r != null) r.cancel();
    }

    /**
     * Opens a specific dialogue menu for a player immediately.
     * @param player The player to open the menu for.
     * @param npc The NPC associated with the dialogue.
     */
    private static void openMenuImmediately(Player player, String npc) {
        String menuName = null;
        if (npc.equalsIgnoreCase("npc_emily")) {
            menuName = "menu_emily";
        } else if (npc.equalsIgnoreCase("npc_alex")) {
            menuName = "casino_menu";
        } else if (npc.equalsIgnoreCase("npc_fred")) {
            menuName = "travel_menu"; // Corresponding menu for Fred
        }

        if (menuName != null) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dmenu open " + menuName + " " + player.getName());
        }
    }

    /**
     * Sends a formatted message to the player, applying custom hex colors and Bukkit color codes.
     * @param player The player to send the message to.
     * @param message The message string to format and send.
     */
    private static void sendFormatted(Player player, String message) {
        player.sendMessage(applyHexColor(message));
    }

    /**
     * Applies custom hex color codes (e.g., <#RRGGBB>) and standard Bukkit color codes (&) to a message string.
     * @param message The message string to apply colors to.
     * @return The formatted message string.
     */
    private static String applyHexColor(String message) {
        StringBuilder builder = new StringBuilder();
        char[] chars = message.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '<' && i + 7 < chars.length && chars[i + 1] == '#' && chars[i + 8] == '>') {
                // Handle hex color codes <#RRGGBB>
                String hex = message.substring(i + 1, i + 8);
                try {
                    builder.append(ChatColor.of(hex));
                } catch (IllegalArgumentException e) {
                    // Fallback if hex color is invalid, append as is
                    builder.append(chars[i]);
                }
                i += 8;
            } else if (chars[i] == '&' && i + 1 < chars.length) {
                // Handle standard Bukkit color codes &X
                builder.append(ChatColor.translateAlternateColorCodes('&', "&" + chars[i + 1]));
                i++;
            } else {
                builder.append(chars[i]);
            }
        }
        return builder.toString();
    }

    /**
     * Sends a line of text to the player with a clickable button that opens a dialogue menu.
     * @param player The player to send the message to.
     * @param menuName The name of the menu to open when the button is clicked.
     * @param messageBeforeButton The message to display before the clickable button.
     */
    private static void sendClickableLine(Player player, String menuName, String messageBeforeButton) {
        TextComponent fullLine = new TextComponent("              " + applyHexColor(messageBeforeButton));
        TextComponent menuPart = new TextComponent("Menu →");

        // Set color and bold for the clickable part, matching existing theme
        menuPart.setColor(ChatColor.of("#ffacd5"));
        menuPart.setBold(true);
        // Set click event to run a command to open the specified menu
        menuPart.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dmenu open " + menuName + " " + player.getName()));
        // Set hover event to show a tooltip when hovering over the button
        menuPart.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Ouvrir le menu").color(ChatColor.of("#ffacd5")).create()));

        fullLine.addExtra(menuPart); // Add the clickable part to the full line
        player.spigot().sendMessage(fullLine); // Send the message to the player
    }

    /**
     * Starts a repeating task to track the player's location and cancel dialogue if they move too far.
     * @param player The player to track.
     * @param uuid The UUID of the player.
     */
    private static void startLocationTracking(Player player, UUID uuid) {
        Location startLoc = player.getLocation().clone();
        new BukkitRunnable() {
            @Override
            public void run() {
                // Cancel if player is offline or dialogue is no longer pending
                if (!player.isOnline() || !pendingDialogs.containsKey(uuid)) {
                    cancel();
                    return;
                }

                // If player moves more than 4 blocks, cancel dialogue
                if (player.getLocation().distanceSquared(startLoc) > 4) {
                    player.sendMessage("§f\uE01D§c Dialogue annulé, vous vous êtes éloigné.");
                    cancelRunnable(uuid);
                    cancel();
                }
            }
        }.runTaskTimer(Main.get(), 0L, 5L); // Check every 5 ticks (0.25 seconds)
    }
}