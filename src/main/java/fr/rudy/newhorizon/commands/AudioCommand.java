package fr.rudy.newhorizon.commands;

import fr.rudy.newhorizon.Main;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AudioCommand implements CommandExecutor {

    private final Main plugin;

    public AudioCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = plugin.getPrefixInfo();

        TextComponent message = new TextComponent("&f\uE01F&b Pour utiliser le chat vocal, installe le mod en cliquant ");
        TextComponent ici = new TextComponent("§n§lici");
        ici.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                "https://www.curseforge.com/minecraft/mc-mods/plasmo-voice/files/all?page=1&pageSize=20"));
        message.addExtra(ici);

        if (sender instanceof Player player) {
            player.spigot().sendMessage(message);
        } else {
            sender.sendMessage(prefix + "&f\uE01F&b Pour utiliser le chat vocal : https://www.curseforge.com/minecraft/mc-mods/plasmo-voice/files");
        }

        return true;
    }
}
