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

import com.google.common.collect.Sets;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.LogRecord;
import java.util.stream.Stream;

/**
 * Handles the logging of debug messages to file. The logger will create a new file when it's
 * created to store all the logs.
 */
@SuppressWarnings("unused")
public class DebugLogger {

    private final static WeakHashMap<String, DebugLogger> LOGGERS = new WeakHashMap<>();
    private static PrintWriter printWriter;
    private final File LOGS_FOLDER;
    private final String NAME;
    private Set<DebugDelayEntry> delays = Sets.newConcurrentHashSet();

    public static DebugLogger get(Plugin plugin) {
        return LOGGERS.get(plugin.getName());
    }

    public static DebugLogger get(String name) {
        return LOGGERS.get(name);
    }

    /**
     * Creates a new logger. This logger will log everything to file.
     *
     * @param plugin plugin the logger is for.
     */
    public DebugLogger(Plugin plugin) {
        if (LOGGERS.containsKey(plugin.getName())) {
            throw new IllegalStateException("A debug logger has already been created for plugin " + plugin.getName());
        }
        NAME = plugin.getName();
        LOGS_FOLDER = new File(plugin.getDataFolder(), "logs");
        LOGS_FOLDER.mkdirs();

        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh-mm-ss");
        Date date = new Date();
        String fileName = "Log " + format.format(date) + ".log";
        File file = new File(LOGS_FOLDER, fileName);
        try {
            printWriter = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cleanes up and removes all log files that are older than x days. This will ensure
     * that the logs don't take up to much space.
     *
     * @param days how many days a log file can exist before being deleted.
     */
    public final void cleanOldLogs(int days) {
        try {
            info("Checking old log files");
            AtomicInteger deleted = new AtomicInteger();
            Stream<Path> files = Files.list(LOGS_FOLDER.toPath());
            files.forEach(path -> {
                if (!path.getFileName().toString().endsWith(".log")) {
                    return;
                }
                BasicFileAttributes attr;
                try {
                    attr = Files.readAttributes(path, BasicFileAttributes.class);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                long created = attr.creationTime().to(TimeUnit.MILLISECONDS);
                if (System.currentTimeMillis() - created > TimeUnit.DAYS.toMillis(days)) {
                    try {
                        Files.delete(path);
                        deleted.incrementAndGet();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            if (deleted.get() > 0) {
                info("Deleted " + deleted.get() + " old logs.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the logger.
     */
    public void close() {
        if (printWriter != null) {
            info("Closed logger");
            printWriter.close();
        }
        LOGGERS.remove(NAME);
        delays.clear();
    }

    public void addDelayEntry(DebugDelayEntry entry) {
        delays.add(entry);
    }

    public boolean isDelayed(Class<?> clazz, String key) {
        return delays.contains(new DebugDelayEntry(clazz, key, 0));
    }

    public void log(Severity level, String message) {
        log(level, message, null);
    }

    public void log(Severity level, String message, Throwable thrown) {
        log(new DebugRecord(level, message).setThrown(thrown));
    }

    public void log(Severity level, Object message) {
        log(level, message, null);
    }

    public void log(Severity level, Object message, Throwable thrown) {
        log(level, message.toString(), thrown);
    }

    public void log(LogRecord log) {
        log(DebugRecord.fromLog(log));
    }

    public void log(DebugRecord log) {
        printWriter.write(format(log));
        printWriter.flush();
        cleanDelayEntries();
    }

    public void info(String message) {
        log(Severity.INFO, message);
    }

    public void info(Object message) {
        log(Severity.INFO, message);
    }

    public void debug(String message) {
        log(Severity.DEBUG, message);
    }

    public void debug(String message, Throwable thrown) {
        log(Severity.DEBUG, message, thrown);
    }

    public void warning(String message) {
        log(Severity.WARNING, message);
    }

    public void error(String message) {
        log(Severity.ERROR, message);
    }

    public void error(String message, Throwable thrown) {
        log(Severity.ERROR, message, thrown);
    }

    public void fatal(String message) {
        log(Severity.FATAL, message);
    }

    public void fatal(String message, Throwable thrown) {
        log(Severity.FATAL, message, thrown);
    }

    /**
     * Removes all delay entries that have expired.
     */
    private void cleanDelayEntries() {
        delays.removeIf(DebugDelayEntry::isDone);
    }

    private String format(DebugRecord record) {
        ZonedDateTime date = ZonedDateTime.now();

        String throwable = "";
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
        }

        return String.format("%n[%1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS] [%2$-7s]: %3$s%4$s",
                date,
                record.getSeverity().getName(),
                record.getMessage(), throwable);
    }
}
