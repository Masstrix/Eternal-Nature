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
import com.google.gson.JsonPrimitive;
import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.core.render.LeafParticle;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class EntityStorage {

    public final static UUID SESSION_ID = UUID.randomUUID();
    private HashSet<CachedEntity> entities = new HashSet<>();
    private EternalNature plugin;
    private File dataFile;
    private File dataFolder;

    public EntityStorage(EternalNature plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        this.dataFile = new File(dataFolder, "entities.dat");
    }

    void store(CachedEntity entity) {
        entities.add(entity);
        try {
            write();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void remove(CachedEntity entity) {
        entities.remove(entity);
        try {
            write();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void restartSystem() throws IOException {
        plugin.getLogger().log(Level.INFO, "Reading entity storage data...");
        List<String> data = Files.readAllLines(dataFile.toPath());
        int line = 0;
        int removedEntities = 0;
        for (String e : data) {
            if (line++ == 0) {
                continue;
            }
            JsonObject json = CachedEntity.readData(e);
            if (removeEntityCheck(json))
                removedEntities++;
        }
        if (removedEntities > 0)
            plugin.getLogger().log(Level.INFO, "Removed " + removedEntities + " expired entities.");
    }

    private void write() throws IOException {
        if (!dataFile.exists()) {
            dataFolder.mkdirs();
            dataFile.createNewFile();
            plugin.getLogger().log(Level.INFO, "Created new file " + dataFile.getAbsolutePath());
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile));
        writer.write("version=" + plugin.getDescription().getVersion());
        writer.newLine();
        for (CachedEntity entity : entities) {
            writer.write(entity.serialize());
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    private boolean removeEntityCheck(JsonObject json) {
        JsonPrimitive removeKey = new JsonPrimitive(EntityOption.REMOVE_ON_RESTART.name());
        if (json.has("options")) {
            JsonArray array = json.getAsJsonArray("options");
            if (array.contains(removeKey) && json.has("entities")) {
                String list = json.get("entities").getAsString();
                String[] ids = list.substring(1, list.length() - 1).split(",");
                boolean removed = false;
                for (String s : ids) {
                    Entity entity = Bukkit.getEntity(UUID.fromString(s));
                    if (entity != null) {
                        entity.remove();
                        removed = true;
                    }
                }
                return removed;
            }
        }
        return false;
    }
}
