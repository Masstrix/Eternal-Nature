package me.masstrix.lang.langEngine;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class Lang {

    private static final String DATA_HEADER = "header";
    private static final String DATA_TEXT = "text";
    private static final Pattern PATTERN = Pattern.compile(".*=");

    private boolean persistent;
    private File file;
    private String locale = "";
    private Map<String, String> header = new HashMap<>();
    private Map<String, String> text = new HashMap<>();

    public Lang(File file) {
        this(file, false);
    }

    public Lang(File file, boolean persistent) {
        this.persistent = persistent;
        this.file = file;

        // Set the locale of this language.
        int nl = file.getName().length();
        locale = file.getName().substring(0, nl - ".lang".length());

        read(true, persistent);
    }

    /**
     * @return the loale for this language.
     */
    public String getLocale() {
        return locale;
    }

    /**
     * @return the nice name for this language.
     */
    public String getNiceName() {
        return header.getOrDefault("name", header.getOrDefault("locale", "unknown"));
    }

    /**
     * Returns a text attribute.
     *
     * @param key key for the text value.
     * @return the text value for this key or the key if it does not exist.
     */
    public String getText(String key) {
        return getText(key, key);
    }

    /**
     * Returns a text attribute.
     *
     * @param key key for the text value.
     * @param def default value if key is missing.
     * @return the text value for this key or default if it oes not exist.
     */
    public String getText(String key, String def) {
        return text.getOrDefault(key, def);
    }

    /**
     * Sets a text value.
     *
     * @param key  key to set.
     * @param text value to assign with key.
     */
    public void setText(String key, String text) {
        this.text.put(key, text);
    }

    /**
     * @param key key to check if exists.
     * @return if the kanguage contains the key.
     */
    public boolean containsText(String key) {
        return text.containsKey(key);
    }

    /**
     * Returns a header attribute.
     *
     * @param key key to get from header attributes.
     * @return the value.
     */
    public String getHeader(String key) {
        return header.get(key);
    }

    /**
     * Sets a header value.
     *
     * @param key key to set.
     * @param val value to assign with key.
     */
    public void setHeaderValue(String key, String val) {
        header.put(key, val);
    }

    /**
     * @return if this language is set to be persistent.
     */
    public boolean isPersistent() {
        return persistent;
    }

    /**
     * Attempts to save the language to file.
     *
     * @throws IOException
     */
    public void save() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write("# header");
        writer.newLine();
        for (Map.Entry<String, String> head : header.entrySet()) {
            writer.write(head.getKey() + "=" + head.getValue());
            writer.newLine();
        }
        writer.write("#");
        writer.newLine();
        for (Map.Entry<String, String> text : text.entrySet()) {
            writer.write(text.getKey() + "=" + text.getValue());
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    /**
     * Loads the languages text into cache. If the text has already been loaded
     * then it will be refreshed.
     */
    public void loadText() {
        read(false, true);
    }

    /**
     * Reads the language files content.
     *
     * @param readHeader should the file header be read.
     * @param readText should the files text content be loaded.
     */
    public void read(boolean readHeader, boolean readText) {
        if (!file.exists()) {
            return;
        }
        if (!readHeader && !readText) return;
        int nl = file.getName().length();
        String locale = file.getName().substring(0, nl - ".lang".length());
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));

            String section = "";
            Iterator<String> iterator = reader.lines().iterator();

            // Read file content
            while (iterator.hasNext()) {
                String line = iterator.next();
                if (line.length() == 0) continue;

                // Ignore comment lines
                if (line.startsWith("//")) {
                    continue;
                }

                // Check if section block tag is used.
                if (line.startsWith("# ")) {
                    section = line.substring(2).toLowerCase();
                    continue;
                } else if (line.equals("#")){
                    if (readHeader && !readText && section.equals(DATA_HEADER)) {
                        break;
                    }
                    section = section.equals(DATA_HEADER) ? DATA_TEXT : "";
                    continue;
                }

                // Ignore mal-formatted lines
                if (!PATTERN.matcher(line).lookingAt()) continue;

                // Split key and value line data
                String[] data = getLineInfo(line);

                // Handle reading just the file readHeader
                if (readHeader && section.equals(DATA_HEADER)) {
                    this.header.put(data[0], data[1]);
                }

                if (readText && section.equals(DATA_TEXT)) {
                    text.put(data[0], data[1]);
                }
            }

            if (readText) {
                System.out.print("Loaded Language " + getNiceName());
            }
        } catch (IOException e) {
            System.out.println("Failed to language load file " + file.getName());
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Unloads the text for this language from memory. If the language is set to
     * be persistent
     */
    public void unload() {
        if (persistent) return;
        text.clear();
        System.out.println("Unloaded language " + getNiceName() + " (" + getLocale() + ")");
    }

    /**
     * sets if this language should stay persistent once loaded.
     *
     * @param b should lang be persistent.
     */
    protected void setPersistent(boolean b) {
        this.persistent = b;
    }

    /**
     * Splits a text line into the key and text components.
     *
     * @param line line to split.
     * @return an array with a length of 2. First element is the key,
     *         second string is the text.
     */
    private String[] getLineInfo(String line) {
        return line.split("=", 2);
    }
}
