package me.masstrix.lang.langEngine;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Handles language files for multi language support in applications.
 */
public class LanguageEngine {

    private File dir;
    private String selected = "en";
    private final String FALLBACK;
    private final Map<String, Lang> LOADED = new HashMap<>();
    private Set<String> persistent = new HashSet<>();

    /**
     * Defines a new language engine.
     *
     * @param folder folder to load .lang files from.
     */
    public LanguageEngine(File folder) {
        this(folder, null);
    }

    /**
     * Defines a new language engine.
     *
     * @param folder folder to load .lang files from.
     * @param fallback nullable. sets the fallback language. If null no fallback
     *                 will be used and return the keys when a text is not found
     *                 from the selected language. If the fallback is set and is
     *                 is loaded, any missing text will attempt to be retrieved from
     *                 the fallback language.
     */
    public LanguageEngine(File folder, String fallback) {
        loadFrom(folder);
        this.FALLBACK = stringNulled(fallback);
        setPersistent(fallback);
    }

    /**
     * Sets the file to load language files from.
     *
     * @param folder folder to load .lang files from.
     */
    public void loadFrom(File folder) {
        this.dir = folder;
        this.dir.mkdirs();
    }

    /**
     * Sets what languages should always be loaded in memory. Once run it will
     * set all defined languages to be persistent. This means when the text values
     * are loaded for that language they will not be removed when the language is
     * changed.
     *
     * @param languages language to keep persistent.
     *
     * @return instance of this engine.
     */
    public LanguageEngine setPersistent(String... languages) {
        for (String l : languages) {
            persistent.add(l);
            if (LOADED.containsKey(l)) {
                Lang lang = LOADED.get(l);
                lang.setPersistent(true);
                lang.loadText();
            }
        }
        return this;
    }

    /**
     * Copies all .lang files from a folder to the working folder for this
     * language engine instane.
     *
     * @param folder folder to copy .lang files from.
     */
    public void copyLanguagesFrom(File folder) {
        if (!folder.isDirectory()) return;

        FileFilter filter = pathname -> pathname.getName().endsWith(".lang");
        for (File file : Objects.requireNonNull(folder.listFiles(filter))) {
            File target = new File(dir, file.getName());
            if (target.exists()) continue;
            try {
                Files.copy(file.toPath(), target.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads all .lang files in the language folder. If a language has already been loaded
     * then it will update the values of that language.
     */
    public void loadLanguages() {
        if (dir == null || !dir.exists()) return;

        FileFilter filter = pathname -> pathname.getName().endsWith(".lang");
        for (File file : Objects.requireNonNull(dir.listFiles(filter))) {
            loadSingle(file);
        }
    }

    /**
     * Loads a single .lang file.
     *
     * @param file file to load, the file must have a .lang extension to be read.
     */
    public void loadSingle(File file) {
        int nl = file.getName().length();
        String fn = file.getName().substring(0, nl - ".lang".length());
        if (LOADED.containsKey(fn)) {
            if (selected.equals(fn))
                LOADED.get(fn).loadText();
        } else {
            Lang lang = new Lang(file, persistent.contains(fn));
            LOADED.put(lang.getLocale(), lang);
        }
    }

    /**
     * Lists all loaded languages in a collection.
     *
     * @return all loaded languages.
     */
    public Collection<Lang> list() {
        return LOADED.values();
    }

    /**
     * @return the selected language.
     */
    public String getSelected() {
        return selected;
    }

    /**
     * Returns if the selected language is the same.
     *
     * @param lang language to check.
     * @return if this language is the selected one.
     */
    public boolean isActive(Lang lang) {
        return lang != null && lang.getLocale().equals(selected);
    }

    /**
     * Sets the language to use.
     *
     * @param lang language to change to.
     */
    public void setLanguage(Lang lang) {
        setLanguage(lang.getLocale());
    }

    /**
     * Sets the language to use.
     *
     * @param name name of locale.
     */
    public void setLanguage(String name) {
        if (LOADED.containsKey(name)) {
            // Unload currently selected language
            Lang lang = LOADED.get(selected);
            lang.unload();

            // Load new language
            this.selected = name;
            lang = LOADED.get(name);
            lang.loadText();
            System.out.println("Changed language to " + lang.getNiceName());
        }
    }

    /**
     * Returns a text value from the selected language. If the language
     * was not loaded and a fallback was set it will attempt to get the
     * text value from the fallback language. If there is no fallback set
     * and the key does not exists in the selected language or the fallback
     * if enabled the key will be returned.
     *
     * @param key key of the text.
     * @return the text if it exists otherwise a error will be returned.
     */
    public String getText(String key) {
        Lang lang = LOADED.get(selected);
        if (useFallback()) {
            Lang fallback = LOADED.get(this.FALLBACK);
            if (lang == null)
                return fallback == null ? key : fallback.getText(key);
            return lang.getText(key, fallback.getText(key));
        } else {
            if (lang == null) return key;
            return lang.getText(key);
        }
    }

    /**
     * @return if the fallback language has been set and should be used.
     */
    private boolean useFallback() {
        return FALLBACK != null;
    }

    /**
     * Returns the passed string or null if that string is null or the strings
     * length is 0.
     *
     * @param s string to validate.
     * @return returns null if string length is 0 otherwise returns s.
     */
    private String stringNulled(String s) {
        return s == null || s.length() == 0 ? null : s;
    }
}
