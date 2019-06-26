package me.masstrix.eternalnature.command;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.data.UserData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class HydrateCommand extends EternalCommand {

    private EternalNature plugin;

    public HydrateCommand(EternalNature plugin) {
        super("hydrate");
        this.plugin = plugin;
    }

    @Override
    public void execute(String[] args) {
        Player player;
        if (args.length == 0) {
            if (wasPlayer())
                player = (Player) getSender();
            else {
                msg("&cPlease define who to hydrate.");
                return;
            }
        } else {
            player = Bukkit.getPlayer(args[0]);
        }
        hydrateUser(player);
    }

    private void hydrateUser(Player player) {
        if (player == null) {
            msg("&cNo online player found with that name.");
            return;
        }
        UserData user = plugin.getEngine().getUserData(player.getUniqueId());
        if (user != null) {
            user.setHydration(20);
            if (wasPlayer())
                msg("&aYou have been fully hydrated!");
            else msg("&aHydrated " + player.getName());
        }
    }
}
