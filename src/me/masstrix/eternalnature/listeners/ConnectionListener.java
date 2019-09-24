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

package me.masstrix.eternalnature.listeners;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.PluginData;
import me.masstrix.eternalnature.config.ConfigOption;
import me.masstrix.eternalnature.util.StringUtil;
import me.masstrix.eternalnature.util.VersionChecker;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {

    private EternalNature plugin;

    public ConnectionListener(EternalNature plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getEngine().loadPlayerData(event.getPlayer().getUniqueId());

        Player player = event.getPlayer();
        if (player.hasPermission("eternalnature.admin") || player.isOp()
                && plugin.getSystemConfig().isEnabled(ConfigOption.UPDATES_NOTIFY)) {
            VersionChecker.VersionMeta meta = plugin.getVersionMeta();
            if (meta == null) return;
            switch (meta.getState()) {
                case BEHIND: {
                    ComponentBuilder builder = new ComponentBuilder(StringUtil.color(PluginData.PREFIX));
                    builder.append("There is a newer version aviliable ").color(ChatColor.WHITE)
                            .append("(" + meta.getLatestVersion() + ")   ").color(ChatColor.GOLD);
                    builder.append("\nVIEW DOWNLOAD", ComponentBuilder.FormatRetention.NONE).color(ChatColor.AQUA).bold(true)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("\u00A7eClick to view download page")))
                            .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/natural-environment.43290/"));
                    player.spigot().sendMessage(builder.create());
                    break;
                }
                case DEV_BUILD: {
                    ComponentBuilder builder = new ComponentBuilder(StringUtil.color(PluginData.PREFIX));
                    builder.append("You are running a dev build. Please report bugs ").color(ChatColor.RED);
                    builder.append("HERE", ComponentBuilder.FormatRetention.NONE).color(ChatColor.AQUA).bold(true)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("\u00A7eClick to report a bug.")))
                            .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Masstrix/Eternal-Nature/issues"));
                    player.spigot().sendMessage(builder.create());
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getEngine().unloadUserData(event.getPlayer().getUniqueId());
    }
}
