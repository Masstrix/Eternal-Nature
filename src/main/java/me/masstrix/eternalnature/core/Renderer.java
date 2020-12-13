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

package me.masstrix.eternalnature.core;

import me.masstrix.eternalnature.EternalEngine;
import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.config.Configurable;
import me.masstrix.eternalnature.player.UserData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

@Configurable.Path("global")
public class Renderer implements EternalWorker, Configurable {

    private EternalNature plugin;
    private EternalEngine engine;
    private BukkitTask renderTask;
    private int renderDelay = 20;

    public Renderer(EternalNature plugin, EternalEngine engine) {
        this.plugin = plugin;
        this.engine = engine;
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        renderDelay = section.getInt("render-delay-ticks", 20);
        start();
    }

    @Override
    public void start() {
        end(); // Make sure any previous tasks are first stopped
        renderTask = new BukkitRunnable() {
            @Override
            public void run() {
                render();
            }
        }.runTaskTimer(plugin, 0, renderDelay);
    }

    /**
     * Render all components in the plugin.
     */
    private void render() {
        for (UserData user : engine.getCashedUsers()) {
            if (!user.isOnline()) continue;
            user.render();
        }
    }

    @Override
    public void end() {
        if (renderTask != null)
            renderTask.cancel();
    }
}
