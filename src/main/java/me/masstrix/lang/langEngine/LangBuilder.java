package me.masstrix.lang.langEngine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LangBuilder {

    private boolean overrde;
    private File folder;
    private String locale;
    private String name;
    private Map<String, String> text = new HashMap<>();

    /**
     * creates a new language builder instance.
     *
     * @param folder   folder to save the language file to.
     * @param locale   locale of the language (eg. en, us, fr).
     * @param name     name of the language.
     */
    public LangBuilder(File folder, String locale, String name) {
        this(folder, locale, name, false);
    }

    /**
     * creates a new language builder instance.
     *
     * @param folder   folder to save the language file to.
     * @param locale   locale of the language (eg. en, us, fr).
     * @param name     name of the language.
     * @param override should it override an existing .lang file.
     */
    public LangBuilder(File folder, String locale, String name, boolean override) {
        this.folder = folder;
        this.overrde = override;
        this.locale = locale;
        this.name = name;
    }

    /**
     * Sets a text value in the language.
     *
     * @param key   key of value to set.
     * @param value value assigned to this ket.
     * @return an instance of this builder.
     */
    public LangBuilder setText(String key, String value) {
        text.put(key, value);
        return this;
    }

    /**
     * Attempts to create the .lang file.
     *
     * @return if the file was created.
     */
    public boolean create() {
        File langFile = new File(folder, locale + ".lang");
        if (langFile.exists() && !overrde) {
            return false;
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(langFile));
            writer.write("# header");
            writer.newLine();
            writer.write("name=" + name);
            writer.newLine();
            writer.write("# header");
            writer.newLine();
            writer.write("#");
            writer.newLine();

            for (Map.Entry<String, String> text : text.entrySet()) {
                writer.write(text.getKey() + "=" + text.getValue());
                writer.newLine();
            }

            writer.flush();
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
