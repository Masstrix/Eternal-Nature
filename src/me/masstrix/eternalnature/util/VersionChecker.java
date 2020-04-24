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

package me.masstrix.eternalnature.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VersionChecker {

    private static final String SPIGET_URL = "https://api.spiget.org/v2/resources/";
    private static final String LATEST_VERSION = "/versions/latest";
    private static final Gson gson = new Gson();
    private static ExecutorService task = Executors.newSingleThreadExecutor(r -> new Thread(r, "VersionChecker"));
    private int id;
    private String current;

    public VersionChecker(int resource, String current) {
        id = resource;
        this.current = current;
    }

    /**
     * Connection to the spigot forums and
     *
     * @param callback callback method to be ran when the task is complete.
     */
    public void run(VersionCallback<VersionMeta> callback) {
        task.execute(() -> {
            try {
                URL url = new URL(SPIGET_URL + id + LATEST_VERSION + "?" + System.currentTimeMillis());
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setUseCaches(true);
                conn.addRequestProperty("User-Agent", "Eternal Systems");
                conn.setDoOutput(true);

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String input;
                StringBuilder content = new StringBuilder();
                while ((input = br.readLine()) != null) {
                    content.append(input);
                }
                br.close();
                JsonObject statistics;
                try {
                    statistics = gson.fromJson(content.toString(), JsonObject.class);
                    callback.done(new VersionMeta(current, statistics.get("name").getAsString()));
                } catch (JsonParseException e) {
                    e.printStackTrace();
                    callback.done(new VersionMeta(current, "unknown"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                callback.done(new VersionMeta(current, "unknown"));
            }
        });
    }

    /**
     * Callback value when checking the plugins version.
     *
     * @param <T> callback value.
     */
    public interface VersionCallback <T extends VersionMeta> {
        void done(T t);
    }

    /**
     * Meta for a plugins version returned in {@link VersionCallback#done(VersionMeta)}.
     */
    public static class VersionMeta {
        private String currentStr, latestStr;
        private byte[] current = null, latest = null;
        private VersionState state;

        public VersionMeta(String current, String latest) {
            if (latest == null || latest.equalsIgnoreCase("unknown")) {
                state = VersionState.UNKNOWN;
                return;
            }
            this.currentStr = current;
            this.latestStr = latest;
            try {
                this.current = getBytes(current);
                this.latest = getBytes(latest);
            } catch (Exception e) {
                state = VersionState.UNKNOWN;
                return;
            }
            this.state = VersionState.getState(this.current, this.latest);
        }

        /**
         * Returns the versions state. This state is an easy identifier for if
         * it is outdated, current, dev or unknown.
         *
         * @return the versions state.
         */
        public VersionState getState() {
            return state;
        }

        /**
         * @return the current version as a byte array.
         */
        public byte[] getCurrentBytes() {
            return current;
        }

        /**
         * @return the latest version as a byte array.
         */
        public byte[] getLatestBytes() {
            return latest;
        }

        /**
         * @return the current version as a formatted string.
         */
        public String getCurrentVersion() {
            return currentStr;
        }

        /**
         * @return the latest version as a string.
         */
        public String getLatestVersion() {
            return latestStr;
        }

        /**
         * Converts an array of bytes to an array.
         *
         * @param version version as a byte array (eg {@code [1, 0, 2]}.
         * @return the version as a formatted string.
         */
        private String bytesToVer(byte[] version) {
            StringBuilder builder = new StringBuilder();
            for (byte build : version) {
                if (builder.length() > 0)
                    builder.append(".");
                builder.append(build);
            }
            return builder.substring(0, builder.length() - 1);
        }

        /**
         * Converts a version string to an array of bytes.
         *
         * @param version string of version to convert.
         * @return a version byte array.
         */
        private byte[] getBytes(String version) {
            String[] s = version.split("\\.");
            byte[] data = new byte[s.length];
            for (int i = 0; i < s.length; i++)
                data[i] = Byte.parseByte(s[i]);
            return data;
        }
    }

    public enum VersionState {
        DEV_BUILD, LATEST, BEHIND, UNKNOWN;

        /**
         * Gets the state of a version and returns a {@code VersionState}.
         *
         * @param current current version bytes.
         * @param latest  latest version bytes.
         * @return the versions state.
         */
        public static VersionState getState(byte[] current, byte[] latest) {
            if (Arrays.equals(current, latest)) return LATEST;
            if (isBehind(current, latest)) return BEHIND;
            return DEV_BUILD;
        }

        /**
         * Compares the bytes of two versions and returns if the current version
         * is behind the latest.
         *
         * @param current current version bytes.
         * @param latest  latest version bytes.
         * @return true if the current version is behind the latest version.
         */
        private static boolean isBehind(byte[] current, byte[] latest) {
            int ln = Math.min(current.length, latest.length);
            for (int i = 0; i < ln; i++) {
                if (current[i] < latest[i]) return true;
            }
            return false;
        }
    }
}