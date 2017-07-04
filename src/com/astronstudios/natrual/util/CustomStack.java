package com.astronstudios.natrual.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CustomStack extends ItemStack {

    private static Class<?> nmscraftitem;
    private static Class<?> nmsitemstack;
    private static Class<?> nbtTag;
    private static Class<?> nbtList;
    private static Class<?> nbtbase;

    static {
        nmscraftitem = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
        nmsitemstack = ReflectionUtil.getNMSClass("ItemStack");
        nbtTag = ReflectionUtil.getNMSClass("NBTTagCompound");
        nbtList = ReflectionUtil.getNMSClass("NBTTagList");
        nbtbase = ReflectionUtil.getNMSClass("NBTBase");
    }
    private interface NBTSetMethodsPropertys <T extends Enum> {

    }

    public CustomStack(Material material) {
        this.setType(material);
        this.setAmount(1);
    }

    public CustomStack(ItemStack itemStack) {
        this.setType(itemStack.getType());
        this.setItemMeta(itemStack.getItemMeta());
        this.setDurability(itemStack.getDurability());
        this.setAmount(itemStack.getAmount());
        this.setData(itemStack.getData());
        this.addUnsafeEnchantments(itemStack.getEnchantments());
    }

    public static ItemStack hideStats(CustomStack stack) {
        try {
            Method e = nmscraftitem.getDeclaredMethod("asNMSCopy", ItemStack.class);
            Object o = nmsitemstack.cast(e.invoke(nmsitemstack, stack.asItemStack()));
            Object t = o.getClass().getMethod("getTag", new Class[0]).invoke(o);
            if (t == null) {
                t = nbtTag.getConstructor(new Class[0]).newInstance();
            }

            Method m = t.getClass().getMethod("set", String.class, nbtbase);
            m.invoke(t, "AttributeModifiers", nbtList.getConstructor(new Class[0]).newInstance());

            m = t.getClass().getMethod("setInt", String.class, Integer.TYPE);
            m.invoke(t, "HideFlags", 63);

            Method setTag = o.getClass().getMethod("setTag", nbtTag);
            setTag.invoke(o, t);
            Method getItem = nmscraftitem.getDeclaredMethod("asBukkitCopy", nmsitemstack);
            return (ItemStack) getItem.invoke(o, o);
        } catch (Exception var10) {
            var10.printStackTrace();
        }
        return null;
    }

    public static <T> ItemStack setNBTTag(ItemStack stack, String function, Class<T> type, String tag, Object value) {
        try {
            Method e = nmscraftitem.getDeclaredMethod("asNMSCopy", ItemStack.class);
            Object o = nmsitemstack.cast(e.invoke(nmsitemstack, stack));
            Object t = o.getClass().getMethod("getTag", new Class[0]).invoke(o);
            if (t == null) {
                t = nbtTag.getConstructor(new Class[0]).newInstance();
            }

            Method m = t.getClass().getMethod(function, String.class, type);
            m.invoke(t, tag, value);

            Method setTag = o.getClass().getMethod("setTag", nbtTag);
            setTag.invoke(o, t);
            Method getItem = nmscraftitem.getDeclaredMethod("asBukkitCopy", nmsitemstack);
            return (ItemStack) getItem.invoke(o, o);
        } catch (Exception var10) {
            var10.printStackTrace();
        }
        return null;
    }

    private Object getTag() {
        try {
            Method e = nmscraftitem.getDeclaredMethod("asNMSCopy", ItemStack.class);
            Object o = nmsitemstack.cast(e.invoke(nmsitemstack, this));
            Object t = o.getClass().getMethod("getTag").invoke(o);
            return t != null ? t : nbtTag.getConstructor().newInstance();
        } catch (Exception var4) {
            var4.printStackTrace();
            return null;
        }
    }

    public String getNBTTagString(String tag) {
        try {
            return (String) getTag().getClass().getMethod("getString", String.class).invoke(getTag(), tag);
        } catch (Exception var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public static Object getNBTTag(ItemStack stack, String tag) {
        return getNBTTags(stack).get(tag);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, ?> getNBTTags(ItemStack stack) {
        try {
            Method e = nmscraftitem.getDeclaredMethod("asNMSCopy", ItemStack.class);
            Object o = nmsitemstack.cast(e.invoke(nmsitemstack, stack));
            Object t = o.getClass().getMethod("getTag", new Class[0]).invoke(o);
            if (t == null) {
                t = nbtTag.getConstructor(new Class[0]).newInstance();
            }

            Field f = t.getClass().getDeclaredField("map");
            f.setAccessible(true);

            return (Map<String, ?>) f.get(t);
        } catch (Exception var10) {
            var10.printStackTrace();
        }
        return new HashMap<>();
    }

    public CustomStack setDiplayname(String name) {
        ItemMeta meta = this.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        this.setItemMeta(meta);
        return this;
    }

    public CustomStack setDurability(int d) {
        setDurability((short) d);
        return this;
    }

    public CustomStack color(BlockColor color) {
        this.setDurability((short) color.id);
        return this;
    }

    public CustomStack loreAdd(String line) {
        ItemMeta meta = this.getItemMeta();
        List<String> l = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
        l.add(ChatColor.translateAlternateColorCodes('&', line));
        meta.setLore(l);
        this.setItemMeta(meta);
        return this;
    }

    public CustomStack loreAdd(String... line) {
        ItemMeta meta = this.getItemMeta();
        List<String> l = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
        for (String s : line) {
            l.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        meta.setLore(l);
        this.setItemMeta(meta);
        return this;
    }

    public CustomStack loreAdd(List<String> line) {
        ItemMeta meta = this.getItemMeta();
        List<String> l = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
        for (String s : line) {
            l.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        meta.setLore(l);
        this.setItemMeta(meta);
        return this;
    }

    public void loreClear() {
        getItemMeta().setLore(new ArrayList<>());
    }

    public ItemStack asItemStack() {
        return this;
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object object) {
        return (object instanceof CustomStack)
                && this.getType() == ((CustomStack) object).getType()
                && ((this.hasItemMeta() && ((CustomStack) object).hasItemMeta())
                && this.getItemMeta().getLore().toString()
                .equals(((CustomStack) object).getItemMeta().getLore().toString()))
                && this.getEnchantments() == ((CustomStack) object).getEnchantments();
    }
}
