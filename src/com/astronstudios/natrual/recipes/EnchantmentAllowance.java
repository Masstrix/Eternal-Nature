package com.astronstudios.natrual.recipes;

public enum EnchantmentAllowance {
    HELMET, CHESTPLATE, LEGGINGS, BOOTS,
    BOOK,
    BOW, ARROW, SWORD,
    PICKAXE, AXE, SHOVEL, HOE, ROD, SHIELD,
    ALL;

    public static String[] toStringArray() {
        String s = "";
        for (EnchantmentAllowance e : EnchantmentAllowance.values())
            s += (s.length() == 0 ? "" : ",") + e.toString();
        return s.split(",");
    }
}
