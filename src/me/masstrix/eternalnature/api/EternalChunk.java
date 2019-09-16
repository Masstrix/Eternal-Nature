package me.masstrix.eternalnature.api;

import me.masstrix.eternalnature.core.world.WaterfallEmitter;
import me.masstrix.eternalnature.util.EVector;

import java.util.Collection;

public interface EternalChunk {

    int getX();

    int getZ();

    long getKey();

    float getTemperature(EVector point);

    Collection<WaterfallEmitter> getWaterfalls();
}
