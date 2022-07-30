/*
 * Copyright 2021 Matthew Denton
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

package me.masstrix.eternalnature.trigger;

import me.clip.placeholderapi.PlaceholderAPI;
import me.masstrix.eternalnature.player.UserData;
import me.masstrix.eternalnature.util.StringUtil;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stores the functions of an action that is executed when the conditions for a trigger is
 * met.
 */
// TODO add particle action
public abstract class TriggerAction {

    /**
     * Registration of all actions that can be parsed for a trigger. All actions that
     * a trigger can preform is registred in this map for parsing from
     * {@link #parse(ConfigurationSection)}.
     */
    private static final Map<String, Class<? extends TriggerAction>> KEYS;

    static {
        KEYS = new HashMap<>();
        KEYS.put("messages", Message.class);
        KEYS.put("play-sound", PlaySound.class);
    }

    /**
     * Parse the data from the configuration for the action.
     *
     * @param name    name of the action being parsed.
     * @param section the actions configuration section in the trigger.
     * @return an instance of this action.
     */
    abstract TriggerAction parse(String name, ConfigurationSection section);

    /**
     * Executes this action for the specified player.
     *
     * @param data   data related to the player.
     * @param player player to execute the action for.
     */
    abstract void execute(UserData data, Player player);

    /**
     * Parses and returns all the actions in a actions configuration section.
     *
     * @param section section of a configuration to parse all the actions from.
     * @return a set of all the parsed actions for the provided section. If none
     *         were found then an empty set is returned.
     */
    public static Set<TriggerAction> parse(ConfigurationSection section) {
        Set<TriggerAction> actions = new HashSet<>();
        for (Map.Entry<String, Class<? extends TriggerAction>> entry : KEYS.entrySet()) {
            if (!section.contains(entry.getKey())) continue;
            try {
                TriggerAction action = entry.getValue().getDeclaredConstructor().newInstance();
                action.parse(entry.getKey(), section);
                actions.add(action);
            } catch (InstantiationException | IllegalAccessException
                    | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return actions;
    }

    /**
     * Sends a message to a player.
     */
    public static class Message extends TriggerAction {

        private final List<String> MESSAGE = new ArrayList<>();

        @Override
        public Message parse(String name, ConfigurationSection section) {
            if (section.isString(name)) {
                MESSAGE.add(section.getString(name));
            } else {
                List<?> lines = section.getList(name);
                if (lines == null) return this;
                for (Object l : lines) {
                    MESSAGE.add((String) l);
                }
            }
            return this;
        }

        @Override
        public void execute(UserData data, Player player) {
            MESSAGE.forEach(m -> {
                String line = PlaceholderAPI.setPlaceholders(player, m);
                player.sendMessage(StringUtil.color(line));
            });
        }
    }

    /**
     * Plays sounds to a player.
     */
    public static class PlaySound extends TriggerAction {

        private Set<TriggerSound> sounds = new HashSet<>();

        @SuppressWarnings("unchecked")
        @Override
        public PlaySound parse(String name, ConfigurationSection section) {
            if (section.isList(name)) {
                List<?> soundList = section.getList(name);
                if (soundList == null) return this;
                for (Object sound : soundList) {
                    Map<String, ?> data = (Map<String, ?>) sound;
                    Object soundName = data.getOrDefault("sound", null);
                    if (!(soundName instanceof String)) continue;
                    double volume = parseGenericType(data.get("volume"), 1D);
                    double pitch = parseGenericType(data.get("pitch"), 1D);
                    sounds.add(new TriggerSound((String) soundName, (float) volume, (float) pitch));
                }
            } else {
                ConfigurationSection playSoundSec = section.getConfigurationSection(name);
                if (playSoundSec == null) return this;
                String soundName = playSoundSec.getString("sound", "null");
                if (soundName == null || soundName.equals("null")) return this;
                float volume = (float) playSoundSec.getDouble("volume", 1);
                float pitch = (float) playSoundSec.getDouble("pitch", 1);
                sounds.add(new TriggerSound(soundName, volume, pitch));
            }
            return this;
        }

        /**
         * Parses the data from an object to be the same as a generic type. Otherwise
         * returns the default value of def.
         *
         * @param data data to parse.
         * @param def  default value to fall back to. This needs to be the same type you expect
         *             <code>data</code> to be.
         * @param <T>  the type being returned and parsed.
         * @return the parsed object or the default value if it is not the same type as the default
         *         object.
         */
        private <T> T parseGenericType(Object data, T def) {
            if (data != null && data.getClass().isAssignableFrom(def.getClass())) {
                return (T) data;
            }
            return def;
        }

        @Override
        public void execute(UserData data, Player player) {
            for (TriggerSound s : sounds) {
                player.playSound(player.getLocation(), s.getSound(),
                        SoundCategory.MASTER, s.getVolume(), s.getPitch());
            }
        }
    }
}
