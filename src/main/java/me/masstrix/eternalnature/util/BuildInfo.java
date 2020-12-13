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

package me.masstrix.eternalnature.util;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BuildInfo {

    private static int build = -1;
    private static String version = "Unknown";
    private static boolean snapshot;

    /**
     * Loads the build.properties file and sets the build constants. This
     * should be run on initialization.
     */
    public static void load(JavaPlugin plugin) {
        new Thread(() -> {
            try {
                InputStream in = BuildInfo.class.getClassLoader().getResourceAsStream("build.properties");

                // Don't load if file was not found.
                if (in == null) {
                    System.out.println("Failed to load build.properties");
                    return;
                }

                Properties properties = new Properties();
                properties.load(in);

                // Set constant values
                build = Integer.parseInt(properties.getProperty("build", "-1"));
                version = properties.getProperty("version", "Unknown");
                snapshot = Boolean.parseBoolean(properties.getProperty("snapshot", "true"));
            } catch (IOException e) {
                Logger logger = plugin.getLogger();
                logger.log(Level.SEVERE, "Failed to load plugin properties, make sure you restart the " +
                        "server when changing the jar and not reload.");
            }
        }, "PluginPropertyLoader").start();
    }

    /**
     * @return if this build is a snapshot.
     */
    public static boolean isSnapshot() {
        return snapshot;
    }

    /**
     * @return the build version.
     */
    public static String getVersion() {
        return version;
    }

    /**
     * @return the unique builder number.
     */
    public static int getBuild() {
        return build;
    }
}
