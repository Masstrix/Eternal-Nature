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

public class Stopwatch {

    private boolean started = false;
    private long start = -1;
    private long end;
    private long runtime;

    public Stopwatch start() {
        this.start = System.currentTimeMillis();
        return this;
    }

    public Stopwatch startIfNew() {
        if (!started) {
            start();
            started = true;
        }
        return this;
    }

    public long stop() {
        this.end = System.currentTimeMillis();
        this.runtime = end - start;
        return runtime;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getCurrentTime() {
        return System.currentTimeMillis() - this.start;
    }

    public long getRuntime() {
        return runtime == 0 ? getCurrentTime() : runtime;
    }

    public boolean hasPassed(long mills) {
        return getCurrentTime() >= mills;
    }

    public void reset() {
        start = -1;
        end = 0;
        runtime = 0;
    }
}
