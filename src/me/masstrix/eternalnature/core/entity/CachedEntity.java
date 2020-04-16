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

package me.masstrix.eternalnature.core.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.*;

public abstract class CachedEntity {

    private final int HASH_CODE = UUID.randomUUID().hashCode();
    private UUID[] entityIdList = new UUID[1];
    private UUID world;
    private HashSet<EntityOption> options = new HashSet<>();
    private EntityStorage storage;

    public CachedEntity(EntityStorage storage) {
        this.storage = storage;
    }

    public void cache() {
        storage.store(this);
    }

    public void options(EntityOption... option) {
        options.clear();
        options.addAll(Arrays.asList(option));
    }

    public void setWorld(UUID world) {
        this.world = world;
    }

    public UUID getWorld() {
        return world;
    }

    public void setEntityId(UUID id) {
        this.entityIdList = new UUID[] {id};
    }

    public void setEntityIdList(UUID[] id) {
        this.entityIdList = id;
    }

    public UUID getEntityId() {
        return entityIdList[0];
    }

    public UUID[] getEntityIdList() {
        return entityIdList;
    }

    public EntityStorage getStorage() {
        return storage;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CachedEntity && o.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode() {
        return HASH_CODE;
    }

    public String serialize() {
        JsonObject object = new JsonObject();
        JsonArray optionsArray = new JsonArray();
        options.forEach(o -> optionsArray.add(o.name()));
        object.addProperty("world", world.toString());
        object.addProperty("entities", Arrays.toString(entityIdList));
        object.add("options", optionsArray);
        return object.toString();
    }

    public static JsonObject readData(String serializedData) {
        return new JsonParser().parse(serializedData).getAsJsonObject();
    }
}
