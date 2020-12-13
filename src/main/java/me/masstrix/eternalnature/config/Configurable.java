package me.masstrix.eternalnature.config;

import org.bukkit.configuration.ConfigurationSection;

import java.lang.annotation.*;


public interface Configurable {

    /**
     * @return the config path to the
     */
    default String getConfigPath() {
        return "";
    }

    /**
     * Updates the configurable with the provided section.
     *
     * @param section related section for the configuration.
     */
    void updateConfig(ConfigurationSection section);

    /**
     * Sets the section path in the config to use. This will override
     * {@link #getConfigPath()} if used.
     */
    @Inherited
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Path {
        String value();
    }


    @Inherited
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Before {
        String value();
    }
}
