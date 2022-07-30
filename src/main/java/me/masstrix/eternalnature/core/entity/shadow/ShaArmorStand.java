package me.masstrix.eternalnature.core.entity.shadow;

import net.minecraft.core.Rotations;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.WeakHashMap;

public class ShaArmorStand extends ShaEntity<ArmorStand> {

    private final Map<ArmorStandBodyPart, Vector> POSE = new WeakHashMap<>();

    public ShaArmorStand(Location loc) {
        super(loc);
    }

    @Override
    ArmorStand createEntity(ServerLevel level, Location loc) {
        return new ArmorStand(level, 0, 0, 0);
    }

    public void setSmall(boolean small) {
        ENTITY.setSmall(small);
        sendMetaDataUpdate();
    }

    public boolean isSmall() {
        return ENTITY.isSmall();
    }

    public void setArms(boolean arms) {
//        ENTITY.setArms(arms);
        sendMetaDataUpdate();
    }

    public void setMarker(boolean marker) {
        ENTITY.setMarker(marker);
        sendMetaDataUpdate();
    }

    public void setPose(ArmorStandBodyPart part, Vector vec) {
        float x = (float) vec.getX(); // Pitch
        float y = (float) vec.getY(); // Roll
        float z = (float) vec.getZ(); // Yaw

        Rotations rot = new Rotations(x, y, z);

        switch (part) {
            case HEAD -> ENTITY.setHeadPose(rot);
            case BODY -> ENTITY.setBodyPose(rot);
            case LEFT_ARM -> ENTITY.setLeftArmPose(rot);
            case RIGHT_ARM -> ENTITY.setRightArmPose(rot);
            case LEFT_LEG -> ENTITY.setLeftLegPose(rot);
            case RIGHT_LEG -> ENTITY.setRightLegPose(rot);
        }

        POSE.put(part, vec);
        sendMetaDataUpdate();
    }

    public Vector getPose(ArmorStandBodyPart part) {
        return POSE.getOrDefault(part, new Vector());
    }
}
