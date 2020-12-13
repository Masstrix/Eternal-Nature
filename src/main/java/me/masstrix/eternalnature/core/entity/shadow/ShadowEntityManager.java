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

package me.masstrix.eternalnature.core.entity.shadow;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.core.EternalWorker;

import java.util.HashSet;
import java.util.Set;

public class ShadowEntityManager implements EternalWorker {

    private static Set<ShadowEntity> entities = new HashSet<>();
    private EternalNature plugin;

    protected ShadowEntityManager() {

    }

    public ShadowEntityManager(EternalNature plugin) {
        this.plugin = plugin;
    }

    /**
     * Adds an entity to the cache. Once added it will be known to the plugin
     * for if the plugin is disabled for any reason it will remove the entity
     * for clients.
     *
     * @param entity entity to register.
     */
    public void register(ShadowEntity entity) {
        entities.add(entity);
    }

    /**
     * Removes an entity from the cache. Once removed the entity can not be removed
     * if the server is reloaded or something happens and may cause entities
     * to become there without being properly removed.
     *
     * @param entity entity being removed.
     */
    public void unregister(ShadowEntity entity) {
        entities.remove(entity);
    }

    /**
     * Removes all currently loaded and registered shadow entities from the server. This
     * will call the {@link ShadowEntity#remove()} method removing it from any players the
     * entity is currently visible to.
     */
    public void removeAll() {
        entities.forEach(ShadowEntity::remove);
        entities.clear();
    }

    /**
     * Does nothing in the case of this class.
     *
     * @see EternalWorker#start()
     */
    @Override
    public void start() {
    }

    /**
     * Clears all currently loaded and registered shadow entities from the server.
     *
     * @see #removeAll()
     */
    @Override
    public void end() {
        removeAll();
    }
}
