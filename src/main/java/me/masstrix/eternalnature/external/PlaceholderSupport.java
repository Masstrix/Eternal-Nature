/*
 * Copyright 2020 Matthew Denton
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

package me.masstrix.eternalnature.external;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.core.temperature.TemperatureIcon;
import me.masstrix.eternalnature.core.temperature.Temperatures;
import me.masstrix.eternalnature.core.world.WorldData;
import me.masstrix.eternalnature.core.world.WorldProvider;
import me.masstrix.eternalnature.data.UserData;
import me.masstrix.eternalnature.util.BuildInfo;
import me.masstrix.eternalnature.util.SecondsFormat;
import org.bukkit.entity.Player;

public class PlaceholderSupport extends PlaceholderExpansion {

    private final EternalNature PLUGIN;

    public PlaceholderSupport(EternalNature plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public String getIdentifier() {
        return "eternalnature";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getAuthor() {
        return PLUGIN.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return BuildInfo.getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return "";

        UserData data = PLUGIN.getEngine().getUserData(player.getUniqueId());
        if (data == null) return "";

        if (identifier.equals("temperature")) {
            return String.valueOf(data.getTemperature());
        }

        if (identifier.equals("hydration")) {
            return String.valueOf(data.getHydration());
        }

        if (identifier.equals("temperature_icon")) {
            TemperatureIcon icon = getTemperatureIcon(player, data);
            return icon.getColor() + icon.getIcon();
        }

        if (identifier.equals("temperature_name")) {
            TemperatureIcon icon = getTemperatureIcon(player, data);
            return icon.getColor() + icon.getName();
        }

        if (identifier.equals("thirst_effect_timer")) {
            if (!data.isThirsty()) return "";
            return new SecondsFormat().format(data.getThirstTime());
        }

        return null;
    }

    /**
     * Gets the temperature icon for a player.
     *
     * @param player player to get temperature of.
     * @param data data for that player.
     * @return the temperature icon for that players current temperature.
     */
    private TemperatureIcon getTemperatureIcon(Player player, UserData data) {
        WorldProvider provider = PLUGIN.getEngine().getWorldProvider();
        WorldData worldData = provider.getWorld(player.getWorld());
        Temperatures temps = worldData.getTemperatures();
        return TemperatureIcon.getClosest(data.getTemperature(), temps);
    }
}
