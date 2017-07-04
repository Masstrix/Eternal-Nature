package com.astronstudios.natrual.items;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

public class AdvancedClock {

    public AdvancedClock() {

    }

    public void sapwn(Location location) {

    }

    public void desapwn(ArmorStand stand) {

    }

    public boolean isValid(ArmorStand stand) {
        return stand.getCustomName().startsWith("CLOCK-") && stand.getCustomName().length() > 10;
    }
}
