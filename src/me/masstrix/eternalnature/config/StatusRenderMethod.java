package me.masstrix.eternalnature.config;

public enum StatusRenderMethod {
    ACTIONBAR, BOSSBAR;

    static StatusRenderMethod getOr(String val, StatusRenderMethod def) {
        for (StatusRenderMethod m : StatusRenderMethod.values()) {
            if (m.name().equalsIgnoreCase(val)) {
                return m;
            }
        }
        return def;
    }

    public StatusRenderMethod opisite() {
        return this == ACTIONBAR ? BOSSBAR : ACTIONBAR;
    }
}