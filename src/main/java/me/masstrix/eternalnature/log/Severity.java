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

import java.util.logging.Level;

public class Severity {

    public static final Severity INFO = new Severity("INFO");
    public static final Severity DEBUG = new Severity("DEBUG");
    public static final Severity WARNING = new Severity("WARNING");
    public static final Severity ERROR = new Severity("ERROR");
    public static final Severity FATAL = new Severity("FATAL");

    private final String NAME;

    public Severity(String name) {
        this.NAME = name;
    }

    public String getName() {
        return NAME;
    }

    public static Severity fromLogLevel(Level level) {
        if (level == Level.SEVERE) return ERROR;
        if (level == Level.WARNING) return WARNING;
        return INFO;
    }
}
