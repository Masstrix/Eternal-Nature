package me.masstrix.eternalnature.config;

public enum ConfigOption {
    UPDATES_NOTIFY("general.notify-update-join", true),
    UPDATES_CHECK("general.check-for-updates", true),

    WATERFALLS("global.waterfalls", true),
    LEAF_EFFECT("global.falling-leaves", true),
    RANDOM_TREE_SPREAD("global.randomly-spread-trees", true),

    AUTO_PLANT("global.auto-plant.enabled", true),
    AUTO_REPLANT("global.auto-plant.replant-crops", true),
    AUTO_PLANT_SOUND("global.auto-plant.play-sound", true),
    AUTO_PLANT_SAPLING("global.auto-plant.saplings", 0.6),
    AUTO_PLANT_WHEAT("global.auto-plant.wheat", 1.0),
    AUTO_PLANT_CARROT("global.auto-plant.carrot", 1.0),
    AUTO_PLANT_POTATO("global.auto-plant.potato", 1.0),
    AUTO_PLANT_MELON("global.auto-plant.melon", 0.8),
    AUTO_PLANT_PUMPKIN("global.auto-plant.pumpkin", 0.8),
    AUTO_PLANT_BEETROOT("global.auto-plant.beetroot", 1.0),
    AUTO_PLANT_SWEET_BERRY("global.auto-plant.sweet_berry", 1.0),
    AUTO_PLANT_FLOWERS("global.auto-plant.flowers", 0.2),

    TEMP_ENABLED("global.temperature.enabled", true),
    TEMP_DAMAGE("global.temperature.cause-damage", true),
    TEMP_SPRINTING("global.temperature.sprinting", true),
    TEMP_FIRE("global.temperature.catch-fire", true),
    TEMP_SWEAT("global.temperature.sweat", true),
    TEMP_SAVE_DATA("global.temperature.save-data", true),
    TEMP_BAR_STYLE("render.temperature.style", StatusRenderMethod.BOSSBAR.name()),
    TEMP_BAR_FLASH("render.temperature.flash", true),

    HYDRATION_ENABLED("global.hydration.enabled", true),
    HYDRATION_WALKING("global.hydration.lose-from-walking", true),
    HYDRATION_DAMAGE("global.hydration.cause-damage", true),
    HYDRATION_BAR_FLASH("render.hydration.flash", true),
    HYDRATION_BAR_STYLE("render.hydration.style", StatusRenderMethod.BOSSBAR.name());

    String key;
    Object def;

    ConfigOption(String key, Object def) {
        this.key = key;
        this.def = def;
    }

    public String getKey() {
        return key;
    }

    public Object getDefault() {
        return def;
    }
}
