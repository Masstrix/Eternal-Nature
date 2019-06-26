package me.masstrix.eternalnature.util;

import org.bukkit.World;

public class MinecraftTimeConverter {

    private static final long TICKS_PER_DAY = 24000;
    private static final long MIDNIGHT = 18000;
    private static final long MIDDAY = 6000;
    private boolean midnightReset;

    public MinecraftTimeConverter(boolean midnightReset) {
        this.midnightReset = midnightReset;
    }

    public long convert(World world) {
        long time = world.getTime();
        if (midnightReset) {
            time += MIDNIGHT;

        }
        return time;
    }

    public String ampm(World world) {
        return convert(world) < 12000 ? "am" : "pm";
    }
}
