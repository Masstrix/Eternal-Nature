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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BuildInfo {

    private static int build = -1;
    private static String version = "Unknown";
    private static boolean snapshot;

    /**
     * Loads the build.properties file and sets the build constants. This
     * should be run on initialization.
     */
    public static void init() {
        try {
            InputStream in = BuildInfo.class.getClassLoader().getResourceAsStream("build.properties");

            // Load the properties file.
            if (in != null) {
                Properties properties = new Properties();
                properties.load(in);

                // Set constant values
                build = Integer.parseInt(properties.getProperty("build", "-1"));
                version = properties.getProperty("version", "Unknown");
                snapshot = Boolean.parseBoolean(properties.getProperty("snapshot", "true"));
            } else {
                System.out.println("Failed to load build.properties");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
