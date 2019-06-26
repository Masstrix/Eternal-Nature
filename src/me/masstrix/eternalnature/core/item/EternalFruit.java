package me.masstrix.eternalnature.core.item;

import org.bukkit.Location;

public abstract class EternalFruit {

    private int age;
    private Location loc;

    public EternalFruit(Location loc) {
        this.loc = loc;
        this.age = 0;
    }

    public Location getLocation() {
        return loc;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public abstract void update();
}
