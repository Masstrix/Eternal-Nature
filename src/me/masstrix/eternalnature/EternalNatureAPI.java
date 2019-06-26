package me.masstrix.eternalnature;

import me.masstrix.eternalnature.api.EternalUser;
import me.masstrix.eternalnature.api.EternalWorld;
import me.masstrix.eternalnature.core.TemperatureData;
import me.masstrix.eternalnature.data.SystemConfig;

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

    public SystemConfig getSystemConfig() {
        return plugin.getSystemConfig();
    }

    /**
     * @param userId uuid of player.
     * @return a players data or null if there is no data found for that uuid.
     */
    public EternalUser getUserData(UUID userId) {
        return plugin.getEngine().getUserData(userId);
    }

    public EternalWorld getWorlData(UUID worldId) {
        return null;
    }

    /**
     * @return the temperature data.
     */
    public TemperatureData getTemperdatureData() {
        return plugin.getEngine().getTemperatureData();
    }
}
