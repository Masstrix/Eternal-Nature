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

package me.masstrix.version.checker;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import me.masstrix.version.Version;
import org.bukkit.scheduler.BukkitRunnable;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VersionChecker {
    private static final String SPIGET_URL = "https://api.spiget.org/v2/resources/";
    private static final String LATEST_VERSION = "/versions/latest";
    private static final Gson gson = new Gson();
    private final int ID;
    private final String CURRENT;

    public VersionChecker(int resource, String current) {
        ID = resource;
        CURRENT = current;
    }

    /**
     * Connection to the spigot forums and
     *
     * @param callback callback method to be ran when the task is complete.
     */
    public void run(VersionCallback callback) {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(SPIGET_URL + ID + LATEST_VERSION + "?" + System.currentTimeMillis());
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
                    callback.done(new VersionCheckInfo(CURRENT, statistics.get("name").getAsString()));
                } catch (JsonParseException e) {
                    e.printStackTrace();
                    callback.done(new VersionCheckInfo(CURRENT, Version.UNKNOWN));
                }
            } catch (IOException e) {
                e.printStackTrace();
                callback.done(new VersionCheckInfo(CURRENT, Version.UNKNOWN));
            }
        });
        thread.start();
    }
}
