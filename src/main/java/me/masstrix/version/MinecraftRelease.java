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

package me.masstrix.version;

import me.masstrix.eternalnature.reflection.ReflectionUtil;

import java.util.HashMap;
import java.util.Map;

public class MinecraftRelease {

    private final static MinecraftVersion serverVersion;
    private final static Map<Integer, String> VERSIONS = new HashMap<>();

    static {
        // Add all minecraft versions.
        VERSIONS.put(0, "1.7");
        VERSIONS.put(5, "1.7.10");
        VERSIONS.put(47, "1.8");
        VERSIONS.put(107, "1.9");
        VERSIONS.put(110, "1.9.4");
        VERSIONS.put(210, "1.10");
        VERSIONS.put(315, "1.11");
        VERSIONS.put(316, "1.11.2");
        VERSIONS.put(335, "1.12");
        VERSIONS.put(338, "1.12.1");
        VERSIONS.put(340, "1.12.2");
        VERSIONS.put(393, "1.13");
        VERSIONS.put(401, "1.13.1");
        VERSIONS.put(404, "1.13.2");
        VERSIONS.put(477, "1.14");
        VERSIONS.put(480, "1.14.1");
        VERSIONS.put(485, "1.14.2");
        VERSIONS.put(490, "1.14.3");
        VERSIONS.put(498, "1.14.4");
        VERSIONS.put(573, "1.15");
        VERSIONS.put(575, "1.15.1");
        VERSIONS.put(578, "1.15.2");
        VERSIONS.put(735, "1.16");
        VERSIONS.put(736, "1.16.1");
        VERSIONS.put(751, "1.16.2");
        VERSIONS.put(753, "1.16.3");
        VERSIONS.put(754, "1.16.4");

        // Find and set the servers version.
        byte[] version = ReflectionUtil.getVersionUnsafe();
        StringBuilder s = new StringBuilder();
        for (byte b : version) {
            if (s.length() > 0)
                s.append(".");
            s.append(b);
        }
        String ver = s.toString();
        if (new MinecraftVersion().isValid(ver))
            serverVersion = new MinecraftVersion(ver);
        else serverVersion = new MinecraftVersion("1.0");
    }

    public static MinecraftVersion getServerVersion() {
        return serverVersion;
    }

    public static boolean isKnown(MinecraftVersion version) {
        return VERSIONS.containsValue(version.version);
    }

    public static int getProtocol(String version) {
        for (Map.Entry<Integer, String> entry : VERSIONS.entrySet()) {
            if (entry.getValue().equals(version))
                return entry.getKey();
        }
        return -1;
    }

    public String getVersion(int protocol) {
        return VERSIONS.getOrDefault(protocol, Version.UNKNOWN);
    }
}
