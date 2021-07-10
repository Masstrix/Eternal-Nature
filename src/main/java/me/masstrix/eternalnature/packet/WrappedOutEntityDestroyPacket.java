package me.masstrix.eternalnature.packet;

import me.masstrix.eternalnature.log.DebugLogger;
import me.masstrix.version.MinecraftRelease;
import me.masstrix.version.MinecraftVersion;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Wraps the PacketPlayOutEntityDestroy packet class to accept the changes between
 * 1.17 and 1.17.1.
 */
public class WrappedOutEntityDestroyPacket implements WrappedPacket {

    private static boolean acceptsList = true;
    private static Constructor<?> packetClass;

    static {
        Class<?> pc = PacketPlayOutEntityDestroy.class;
        try {
            if (MinecraftRelease.getServerVersion().equals(new MinecraftVersion("1.17"))) {
                acceptsList = false;
                packetClass = pc.getConstructor(int.class);
            } else {
                packetClass = pc.getConstructor(int[].class);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private Object packet;

    /**
     * Creates a new instance of the packet. In 1.17 the packet only accepts a single id and
     * so will always take the first entry. In versions before and after 1.17 it accepts an
     * array of ints.
     *
     * @param entityId array of entity id's to remove. In 1.17 only the first entry will every be used.
     */
    public WrappedOutEntityDestroyPacket(int... entityId) {
        try {
            if (!acceptsList) {
                packet = packetClass.newInstance(entityId[0]);
                if (entityId.length > 1) {
                    DebugLogger.get("EternalNature").warning(
                            "WrappedEntityDestroyPacket was called on 1.17 with a " +
                            "list of entries and is not supported. Only the first entity was removed."
                    );
                }
            } else {
                packet = packetClass.newInstance(entityId);
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getPacket() {
        return packet;
    }
}
