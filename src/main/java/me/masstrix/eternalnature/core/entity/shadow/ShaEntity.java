package me.masstrix.eternalnature.core.entity.shadow;

import com.mojang.datafixers.util.Pair;
import io.netty.util.internal.ConcurrentSet;
import me.masstrix.eternalnature.reflection.ReflectionUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class ShaEntity <E extends Entity> {

    final E ENTITY;
    private Entity passenger;
    final Set<Player> VIS = new ConcurrentSet<>();
    private final ItemStack[] ITEMS = new ItemStack[ItemSlot.values().length];
    private Location location;
    private String customName = "";

    public ShaEntity(Location loc) {
        ENTITY = createEntity(((CraftWorld) loc.getWorld()).getHandle(), loc);
        setLocation(loc);
        new ShadowEntityManager().register(this);
    }

    /**
     * @return an instance of the entity that gets created.
     */
    abstract E createEntity(ServerLevel level, Location loc);

    /**
     * Sends an update of any metadata for the entity. Any changes that are done
     * to the entities metadata will need to have this called for them to be visible
     * to the players.
     */
    public final void sendMetaDataUpdate() {
        ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(
                ENTITY.getId(),
                ENTITY.getEntityData(),
                true
        );
        sendUpdates(packet);
    }

    /**
     * Sends an entity data packet to the player to update the entity data for this shadow
     * entity.
     *
     * @see #sendMetaDataUpdate()
     *
     * @param player who to send the packet to.
     */
    public final void sendMetaDataUpdate(Player player) {
        if (!VIS.contains(player)) return;
        ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(
                ENTITY.getId(),
                ENTITY.getEntityData(),
                true
        );
        ReflectionUtil.sendPacket(player, packet);
    }

    /**
     * Sends an update packet to all players this entity is visible to.
     *
     * @param packet packets to send.
     */
    @SuppressWarnings("all")
    public final void sendUpdates(Packet<?>... packet) {
        VIS.forEach(p->ReflectionUtil.sendPacket(p, packet));
    }

    public void sendTo(Player player) {
        if (VIS.contains(player)) return;
        // Create initial packets
        ClientboundAddEntityPacket spawnPacket = new ClientboundAddEntityPacket(ENTITY);
        ClientboundSetEntityDataPacket metadataPacket = new ClientboundSetEntityDataPacket(
                ENTITY.getId(),
                ENTITY.getEntityData(),
                true
        );

        // Create equipment packet
        List<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> itemList = new ArrayList<>();
        for (ItemSlot slot : ItemSlot.values()) {
            itemList.add(new Pair<>(slot.nms(), ReflectionUtil.asNmsItem(ITEMS[slot.ordinal()])));
        }

        ClientboundSetEquipmentPacket equipmentPacket = new ClientboundSetEquipmentPacket(
                ENTITY.getId(),
                itemList
        );

        // Send packets
        ReflectionUtil.sendPacket(player, spawnPacket, metadataPacket, equipmentPacket);
        VIS.add(player);
    }

    public void hideFrom(Player player) {
        if (!VIS.contains(player)) return;
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(ENTITY.getId());
        ReflectionUtil.sendPacket(player, packet);
        VIS.remove(player);
    }

    public void remove() {
        VIS.forEach(this::hideFrom);
    }

    public Location getLocation() {
        return location;
    }

    public void setCustomName(String name) {
        this.customName = name;
        ENTITY.setCustomName(CraftChatMessage.fromString(name)[0]);
        sendMetaDataUpdate();
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomNameVisible(boolean visible) {
        ENTITY.setCustomNameVisible(visible);
        sendMetaDataUpdate();
    }

    public void setSlot(ItemSlot slot, ItemStack item) {
        ENTITY.setItemSlot(slot.nms(), ReflectionUtil.asNmsItem(item));
        ITEMS[slot.ordinal()] = item;

        List<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> itemList = new ArrayList<>();
        itemList.add(new Pair<>(slot.nms(), ReflectionUtil.asNmsItem(item)));

        sendUpdates(new ClientboundSetEquipmentPacket(ENTITY.getId(), itemList));
    }

    public void move(double x, double y, double z) {
        setLocation(this.location.clone().add(x, y, z));
    }

    public void setLocation(Location loc) {

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        this.location = loc;
        ENTITY.setPos(x, y, z);
        ENTITY.setXRot(loc.getPitch());
        ENTITY.setYRot(loc.getYaw());

        Packet<?> packet;

        if (x < 8 && y < 8 && z < 8) {
            short moveX = (short) ((x * 32 - this.location.getX() * 32) * 128);
            short moveY = (short) ((x * 32 - this.location.getY() * 32) * 128);
            short moveZ = (short) ((x * 32 - this.location.getZ() * 32) * 128);

            packet = new ClientboundMoveEntityPacket.Pos(
                    ENTITY.getId(), moveX, moveY, moveZ, false
            );
        } else {
            packet = new ClientboundTeleportEntityPacket(ENTITY);
        }

        // Send teleport packet to all players
        sendUpdates(packet);
    }

    public void setSilent(boolean silent) {
        ENTITY.setSilent(true);
        sendMetaDataUpdate();
    }

    public void setOnFire(boolean fire) {
        ENTITY.setRemainingFireTicks(fire ? 20 : 0);
        sendMetaDataUpdate();
    }

    public void setSneaking(boolean sneaking) {
        ENTITY.setShiftKeyDown(sneaking);
        sendMetaDataUpdate();
    }

    public void setSwimming(boolean swimming) {
        ENTITY.setSwimming(swimming);
        sendMetaDataUpdate();
    }

    public void setFrozenTicks(int frozenTicks) {
        ENTITY.setTicksFrozen(frozenTicks);
        sendMetaDataUpdate();
    }

    public void setInvisible(boolean invisible) {
        ENTITY.setInvisible(invisible);
        sendMetaDataUpdate();
    }

    public boolean isFullyFrozen() {
        return ENTITY.isFullyFrozen();
    }
}
