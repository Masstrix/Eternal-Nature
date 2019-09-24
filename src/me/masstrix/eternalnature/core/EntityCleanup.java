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

import me.masstrix.eternalnature.EternalNature;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Handles the removal of entities that were summoned by the plugin. This should remove
 * any entities that are no longer being tracked/used to stop nulled floating entities
 * in the world(s).
 */
public class EntityCleanup {

    private static final String META_NAME = "EternalEntity";

    public EntityCleanup(EternalNature plugin, CleanableEntity entity) {
        for (Entity e : entity.getEntities()) {
            e.setMetadata(META_NAME, new FixedMetadataValue(plugin, true));
        }
    }

    EntityCleanup() {

    }

    void run() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntities()) {
                if (e.hasMetadata(META_NAME))
                    e.remove();
            }
        }
    }
}
