/*
 * Copyright 2019 Matthew Denton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.masstrix.eternalnature;

import me.masstrix.eternalnature.api.EternalUser;
import me.masstrix.eternalnature.api.EternalWorld;
import me.masstrix.eternalnature.core.temperature.TemperatureData;
import me.masstrix.eternalnature.config.SystemConfig;
import me.masstrix.eternalnature.menus.Menus;
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
        plugin.getEngine().getMenuManager().getMenu(Menus.SETTINGS).open(player);
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
