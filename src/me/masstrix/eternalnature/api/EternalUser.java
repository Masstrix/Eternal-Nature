package me.masstrix.eternalnature.api;

public interface EternalUser {

    /**
     * @return the players current temperature,
     */
    double getTemperature();

    /**
     * @return the players current hydration. Ranges between 0 and 20.
     */
    double getHydration();

    /**
     * Hydrates the player.
     *
     * @param amount how much to hydrate the player by.
     */
    void hydrate(float amount);
}
