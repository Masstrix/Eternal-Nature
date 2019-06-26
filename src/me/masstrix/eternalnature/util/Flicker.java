package me.masstrix.eternalnature.util;

/**
 * An auto switching toggle class updated everytime {@link #update()} is called.
 */
public class Flicker {

    private boolean enabled = true;
    private long lastSwitch = System.currentTimeMillis();
    private long delay;

    /**
     * @param delay how long between switches.
     */
    public Flicker(long delay) {
        this.delay = delay;
    }

    public boolean update() {
        long now = System.currentTimeMillis();
        if (now - lastSwitch >= delay) {
            lastSwitch = now;
            enabled = !enabled;
        }
        return enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
