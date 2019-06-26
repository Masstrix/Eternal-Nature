package me.masstrix.eternalnature.command;

import me.masstrix.eternalnature.core.block.Pineapple;
import org.bukkit.entity.Player;

@EternalCommand.PlayerOnly
public class TestCommand extends EternalCommand {

    public TestCommand() {
        super("test");
    }

    private Pineapple pineapple;

    @Override
    public void execute(String[] args) {
        Player player = (Player) getSender();

        if (pineapple == null) {
            pineapple = new Pineapple(player.getLocation());
        }

        if (args.length > 0) {
            pineapple.setAge(Integer.parseInt(args[0]));
        }
    }
}
