package com.astronstudios.natrual.items;

import com.astronstudios.natrual.util.CustomStack;
import com.astronstudios.natrual.util.InventoryStringDeSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Backpack {

    private ItemStack stack = null;
    private Inventory inventory;

    public Backpack(ItemStack stack) {
        this.stack = stack;
        Inventory inventory = Bukkit.createInventory(null, getLevel() * 9,
                stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()
                        ? stack.getItemMeta().getDisplayName() : getOwner() + "'s Backpack");
        this.stack = CustomStack.setNBTTag(stack, "setString", String.class, "backpack.inventory",
                InventoryStringDeSerializer.InventoryToString(inventory));
        this.inventory = getInventory();
    }

    public boolean isValid() {
        return stack != null && CustomStack.getNBTTag(stack, "backpack.owner") != null
                && CustomStack.getNBTTag(stack, "backpack.level") != null;
    }

    public String getOwner() {
        return new CustomStack(stack).getNBTTagString("backpack.owner");//(String) CustomStack.getNBTTag(stack, "backpack.owner");
    }

    public int getLevel() {
        return 3; //CustomStack.getNBTTag(stack, "backpack.level") == null ? 1 : (int) CustomStack.getNBTTag(stack, "backpack.level");
    }

    public ItemStack getBackpack() {
        return stack;
    }

    public void setContents(ItemStack[] stacks) {
        inventory.setContents(stacks);
    }

    public void setItem(int slot, ItemStack stack) {
        inventory.setItem(slot, stack);
    }

    public void save() {
        this.stack = CustomStack.setNBTTag(stack, "setString", String.class, "backpack.inventory",
                InventoryStringDeSerializer.InventoryToString(inventory));
    }

    public Inventory getInventory() {
        Inventory inv = inventory != null ? inventory : Bukkit.createInventory(null, getLevel() * 9,
                stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()
                        ? ChatColor.stripColor(stack.getItemMeta().getDisplayName()) : getOwner() + "'s Backpack");

        Inventory content = InventoryStringDeSerializer.StringToInventory(new CustomStack(stack).getNBTTagString("backpack.inventory"));
        inv.setContents(content.getContents());


        /*for (int i = 0; i < inv.getSize() - 1; i++) {
            Object item = CustomStack.getNBTTag(stack, "backpack.items.slot" + i);
            if (item == null) continue;

            String str = (String) item;

            Properties props = new Properties();
            try {
                props.load(new StringReader(str.substring(1, str.length() - 1).replace(", ", "\n")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Map<String, Object> map2 = new HashMap<>();
            for (Map.Entry<Object, Object> e : props.entrySet()) {
                map2.put((String)e.getKey(), e.getValue());
            }

            inv.setItem(i, ItemStack.deserialize(map2));
        }*/
        return inv;
    }
}
