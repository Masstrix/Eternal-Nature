package com.astronstudios.natrual.recipes;

import java.util.List;

public class EnchantmentHelper {

    private String[] valid;
    private int rarity;

    public EnchantmentHelper(int rarity, EnchantmentAllowance validItems) {
        this.rarity = rarity > 100 ? 100 : rarity < 1 ? 1 : rarity;
        String s = "";
        if (validItems == EnchantmentAllowance.ALL) {
            for (EnchantmentAllowance e : EnchantmentAllowance.values()) s += (s.length() == 0 ? "" : ",") + e.toString();
        } else {
            s += validItems.toString();
        }
        this.valid = s.split(",");
    }

    public EnchantmentHelper(int rarity, EnchantmentAllowance... validItems) {
        this.rarity = rarity > 100 ? 100 : rarity < 1 ? 1 : rarity;
        String s = "";
        for (EnchantmentAllowance e : validItems) s += (s.length() == 0 ? "" : ",") + e.toString();
        this.valid = s.split(",");
    }

    public EnchantmentHelper(int rarity, List<EnchantmentAllowance> validItems) {
        this.rarity = rarity > 100 ? 100 : rarity < 1 ? 1 : rarity;
        String s = "";
        for (EnchantmentAllowance e : validItems) s += (s.length() == 0 ? "" : ",") + e.toString();
        this.valid = s.split(",");
    }

    public String[] getValid() {
        return valid;
    }

    public int getRarity() {
        return rarity;
    }
}
