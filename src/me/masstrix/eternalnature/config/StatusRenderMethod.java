package me.masstrix.eternalnature.config;

public enum StatusRenderMethod {
    ACTIONBAR("Actionbar", "Displays above the players hotbar."),
    BOSSBAR("Bossbar", "Shown at the top of the players screen.");

    private String simple;
    private String description;

    StatusRenderMethod(String simple, String description) {
        this.simple = simple;
        this.description = description;
    }

    public String getSimple() {
        return simple;
    }

    public String getDescription() {
        return description;
    }

    public StatusRenderMethod opposite() {
        return this == ACTIONBAR ? BOSSBAR : ACTIONBAR;
    }

    static StatusRenderMethod getOr(String val, StatusRenderMethod def) {
        for (StatusRenderMethod m : StatusRenderMethod.values()) {
            if (m.name().equalsIgnoreCase(val)) {
                return m;
            }
        }
        return def;
    }
}