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

package me.masstrix.eternalnature.log;

import java.util.Objects;

public class DebugDelayEntry {

    private final String KEY;
    private final long EXIT_TIME;

    public DebugDelayEntry(Class<?> clazz, String key, long time) {
        this.KEY = clazz.getName() + ":" + key;
        this.EXIT_TIME = System.currentTimeMillis() + time;
    }

    public String getKey() {
        return KEY;
    }

    public boolean isDone() {
        return System.currentTimeMillis() > EXIT_TIME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DebugDelayEntry)) return false;
        DebugDelayEntry that = (DebugDelayEntry) o;
        return Objects.equals(KEY, that.KEY);
    }

    @Override
    public int hashCode() {
        return KEY.hashCode();
    }
}
