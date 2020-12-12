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

package me.masstrix.eternalnature;

import me.masstrix.eternalnature.core.EternalWorker;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class EternalHeartbeat implements EternalWorker {

    private List<Ticking> pubsub = new ArrayList<>();
    private BukkitTask task;
    private final int RATE;
    private EternalNature plugin;

    public EternalHeartbeat(EternalNature plugin, int tickRate) {
        RATE = tickRate;
        this.plugin = plugin;
    }

    public void subscribe(Ticking ticking) {
        pubsub.add(ticking);
    }

    public void unsubscribe(Ticking ticking) {
        pubsub.remove(ticking);
    }

    @Override
    public void start() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                pubsub.forEach(Ticking::tick);
            }
        }.runTaskTimer(plugin, 0, RATE);
    }

    @Override
    public void end() {
        task.cancel();
    }
}
