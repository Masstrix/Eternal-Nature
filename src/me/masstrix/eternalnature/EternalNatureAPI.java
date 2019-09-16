package me.masstrix.eternalnature;

import me.masstrix.eternalnature.api.EternalUser;
import me.masstrix.eternalnature.api.EternalWorld;
import me.masstrix.eternalnature.core.TemperatureData;
import me.masstrix.eternalnature.config.SystemConfig;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EternalNatureAPI {

    private static boolean enabled = false;
    private static EternalNature plugin;

    EternalNatureAPI(EternalNature instance) {
        if (enabled) return;
        enabled = true;
        plugin = instance;
    }

    public EternalNatureAPI() {
    }

    /**
     * Returns the plugins config. You can edit any aspect of the config from
     * {@link SystemConfig}.
     *
     * @return the system config.
     */
    public SystemConfig getSystemConfig() {
        return plugin.getSystemConfig();
    }

    /**
     * Opens the settings menu for the given player. This does not check for
     * permissions, anyone who has the menu open can edit the settings accessible
     * through it. Only open the menu for players who have valid access.
     *
     * @param player who to open the settings menu for.
     */
    public void openSettingsMenu(Player player) {
        plugin.getSettingsMenu().open(player);
    }

    /**
     * @param userId uuid of player.
     * @return a players data or null if there is no data found for that uuid.
     */
    public EternalUser getUserData(UUID userId) {
        return plugin.getEngine().getUserData(userId);
    }

    public EternalWorld getWorldData(UUID worldId) {
        return null;
    }

    /**
     * @return the temperature data.
     */
    public TemperatureData getTemperatureData() {
        return plugin.getEngine().getTemperatureData();
    }
}
