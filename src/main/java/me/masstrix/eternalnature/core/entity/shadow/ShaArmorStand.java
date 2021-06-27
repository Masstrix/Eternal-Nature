package me.masstrix.eternalnature.core.entity.shadow;

import net.minecraft.core.Vector3f;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.WeakHashMap;

public class ShaArmorStand extends ShaEntity<EntityArmorStand> {

    private final Map<ArmorStandBodyPart, Vector> POSE = new WeakHashMap<>();

    public ShaArmorStand(Location loc) {
        super(loc);
    }

    @Override
    EntityArmorStand createEntity(net.minecraft.world.level.World world, Location loc) {
        return new EntityArmorStand(world, 0, 0, 0);
    }

    public void setSmall(boolean small) {
        ENTITY.setSmall(small);
        sendMetaDataUpdate();
    }

    public boolean isSmall() {
        return ENTITY.isSmall();
    }

    public void setArms(boolean arms) {
        ENTITY.setArms(arms);
        sendMetaDataUpdate();
    }

    public void setMarker(boolean marker) {
        ENTITY.setMarker(marker);
        sendMetaDataUpdate();
    }

    public void setInvisible(boolean invisible) {
        ENTITY.setInvisible(invisible);
        sendMetaDataUpdate();
    }

    public void setPose(ArmorStandBodyPart part, Vector vec) {
        float x = (float) vec.getX(); // Pitch
        float y = (float) vec.getY(); // Roll
        float z = (float) vec.getZ(); // Yaw

        Vector3f vec3 = new Vector3f(x, y, z);

        switch (part) {
            case HEAD -> ENTITY.setHeadPose(vec3);
            case BODY -> ENTITY.setBodyPose(vec3);
            case LEFT_ARM -> ENTITY.setLeftArmPose(vec3);
            case RIGHT_ARM -> ENTITY.setRightArmPose(vec3);
            case LEFT_LEG -> ENTITY.setLeftLegPose(vec3);
            case RIGHT_LEG -> ENTITY.setRightLegPose(vec3);
        }

        POSE.put(part, vec);
        sendMetaDataUpdate();
    }

    public Vector getPose(ArmorStandBodyPart part) {
        return POSE.getOrDefault(part, new Vector());
    }
}
