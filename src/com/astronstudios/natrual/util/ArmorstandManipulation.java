package com.astronstudios.natrual.util;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class ArmorstandManipulation {

    private ArmorStand stand;

    public static final int FALL = 360;
    public static final int HALF = 180;
    public static final int QUAETER = FALL / 4;

    public ArmorstandManipulation(ArmorStand stand) {
        this.stand = stand;
    }

    public void setLeftArm(double pitch, double yaw, double roll) {
        stand.setLeftArmPose(new EulerAngle(angle(pitch), angle(yaw), angle(roll)));
    }

    public void setRightArm(double pitch, double yaw, double roll) {
        stand.setRightArmPose(new EulerAngle(angle(pitch), angle(yaw), angle(roll)));
    }

    public void setLeftLeg(double pitch, double yaw, double roll) {
        stand.setLeftLegPose(new EulerAngle(angle(pitch), angle(yaw), angle(roll)));
    }

    public void setRightLeg(double pitch, double yaw, double roll) {
        stand.setRightLegPose(new EulerAngle(angle(pitch), angle(yaw), angle(roll)));
    }

    public void setBody(double pitch, double yaw, double roll) {
        stand.setBodyPose(new EulerAngle(angle(pitch), angle(yaw), angle(roll)));
    }

    public void setHead(double pitch, double yaw, double roll) {
        stand.setHeadPose(new EulerAngle(angle(pitch), angle(yaw), angle(roll)));
    }

    private double angle(double d) {
        return (d / 180) * Math.PI;
    }

    public void setRotation(int v) {

    }
}
