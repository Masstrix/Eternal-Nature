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

import java.util.logging.LogRecord;

public class DebugRecord {

    private Severity severity;
    private String message;
    private Throwable thrown;

    public DebugRecord(Severity severity, String message) {
        this.severity = severity;
        this.message = message;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Throwable getThrown() {
        return thrown;
    }

    public DebugRecord setThrown(Throwable thrown) {
        this.thrown = thrown;
        return this;
    }

    public static DebugRecord fromLog(LogRecord record) {
        return new DebugRecord(Severity.fromLogLevel(record.getLevel()), record.getMessage())
                .setThrown(record.getThrown());
    }
}
