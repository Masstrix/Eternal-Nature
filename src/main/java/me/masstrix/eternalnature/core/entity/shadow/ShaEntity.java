package me.masstrix.eternalnature.core.entity.shadow;

import com.mojang.datafixers.util.Pair;
import io.netty.util.internal.ConcurrentSet;
import me.masstrix.eternalnature.packet.WrappedOutEntityDestroyPacket;
import me.masstrix.eternalnature.packet.WrappedPacket;
import me.masstrix.eternalnature.reflection.ReflectionUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Set;

public abstract class ShaEntity <E extends Entity> {

    final E ENTITY;
    private Entity passenger;
    final Set<Player> VIS = new ConcurrentSet<>();
    private final ItemStack[] ITEMS = new ItemStack[ItemSlot.values().length];
    private Location location;

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

//        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(
//                ENTITY.getId(),
//                ENTITY.getDataWatcher(), true);
//        sendUpdates(metadata);
    }

    public final void sendMetaDataUpdate(Player player) {
        if (!VIS.contains(player)) return;
//        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(
//                ENTITY.getId(),
//                ENTITY.getDataWatcher(), true);
//        ReflectionUtil.sendPacket(player, metadata);
    }

    @SuppressWarnings("all")
    public final void sendUpdates(Packet<?>... packet) {
//        VIS.forEach(p->ReflectionUtil.sendPacket(p, packet));
    }

    public void sendTo(Player player) {
        if (VIS.contains(player)) return;
//        PacketPlayOutSpawnEntity spawn = new PacketPlayOutSpawnEntity(ENTITY);
//        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(
//                ENTITY.getId(),
//                ENTITY.getDataWatcher(), true);
//
//        // Equipment
//        List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> itemList = new ArrayList<>();
//        for (ItemSlot slot : ItemSlot.values()) {
//            itemList.add(new Pair<>(slot.nms(), ReflectionUtil.asNmsItem(ITEMS[slot.ordinal()])));
//        }
//        PacketPlayOutEntityEquipment equip = new PacketPlayOutEntityEquipment(ENTITY.getId(), itemList);
//
//        // Send data
//        ReflectionUtil.sendPacket(player, spawn, metadata, equip);
        VIS.add(player);
    }

    public void hideFrom(Player player) {
        if (!VIS.contains(player)) return;
//        WrappedPacket packet = new WrappedOutEntityDestroyPacket(ENTITY.getId());
//        ReflectionUtil.sendPacket(player, packet);

        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(ENTITY.getId());
        ServerPlayer p = ((CraftPlayer) player).getHandle();
        ServerGamePacketListenerImpl listener = p.connection;
        listener.send(packet);

        VIS.remove(player);
    }

    public void remove() {
        VIS.forEach(this::hideFrom);
    }

    public Location getLocation() {
        return location;
    }

    public void setCustomName(String name) {
//        ENTITY.setCustomName(new ChatComponentText(name));
//        sendMetaDataUpdate();
    }

    public String getCustomName() {
//        IChatBaseComponent txt = ENTITY.getCustomName();
//        return txt != null ? txt.getText() : "";
        return "";
    }

    public void setCustomNameVisible(boolean visible) {
//        ENTITY.setCustomNameVisible(visible);
//        sendMetaDataUpdate();
    }

    public void setSlot(ItemSlot slot, ItemStack item) {
//        ENTITY.setSlot(EnumItemSlot.values()[slot.ordinal()], ReflectionUtil.asNmsItem(item));
//        ITEMS[slot.ordinal()] = item;
//
//        List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> itemList = new ArrayList<>();
//        itemList.add(new Pair<>(slot.nms(), ReflectionUtil.asNmsItem(item)));
//
//        PacketPlayOutEntityEquipment equip = new PacketPlayOutEntityEquipment(
//                ENTITY.getId(),
//                itemList);
//        sendUpdates(equip);
    }

    public void move(double x, double y, double z) {
        setLocation(this.location.clone().add(x, y, z));
    }

    public void setLocation(Location loc) {
//        double x = loc.getX();
//        double y = loc.getY();
//        double z = loc.getZ();
//
//        this.location = loc;
//        ENTITY.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(),loc.getYaw(), loc.getPitch());
//
//        if (x < 8 && y < 8 && z < 8) {
//            short moveX = (short) ((x * 32 - this.location.getX() * 32) * 128);
//            short moveY = (short) ((x * 32 - this.location.getY() * 32) * 128);
//            short moveZ = (short) ((x * 32 - this.location.getZ() * 32) * 128);
//
//            PacketPlayOutEntity.PacketPlayOutRelEntityMove move = new PacketPlayOutEntity.PacketPlayOutRelEntityMove(
//                    ENTITY.getId(), moveX, moveY, moveZ, false);
//            sendUpdates(move);
//        } else {
//            PacketPlayOutEntityTeleport teleport = new PacketPlayOutEntityTeleport(ENTITY);
//            sendUpdates(teleport);
//        }
    }

    public void setSilent(boolean silent) {
//        ENTITY.setSilent(true);
        sendMetaDataUpdate();
    }

    public void setOnFire(boolean fire) {
//        ENTITY.setFireTicks(fire ? 20 : 0);
        sendMetaDataUpdate();
    }

    public void setSneaking(boolean sneaking) {
//        ENTITY.setSneaking(sneaking);
        sendMetaDataUpdate();
    }

    public void setSwimming(boolean swimming) {
//        ENTITY.setSwimming(swimming);
        sendMetaDataUpdate();
    }

    public void setFrozenTicks(int frozenTicks) {
//        ENTITY.setTicksFrozen(frozenTicks);
        sendMetaDataUpdate();
    }

    public void setInvisible(boolean invisible) {
//        ENTITY.setInvisible(invisible);
        sendMetaDataUpdate();
    }

    public boolean isFullyFrozen() {
        return ENTITY.isFullyFrozen();
    }
}
