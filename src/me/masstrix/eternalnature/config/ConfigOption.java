/*
 * Copyright 2019 Matthew Denton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.masstrix.eternalnature.config;

public enum ConfigOption {
    UPDATES_NOTIFY("general.notify-update-join", true),
    UPDATES_CHECK("general.check-for-updates", true),
    LANGUAGE("general.language", "en"),

    LEAF_EFFECT("global.falling-leaves.enabled", true),
    LEAF_EFFECT_RANGE("global.falling-leaves.range", 6),
    LEAF_EFFECT_FIDELITY("global.falling-leaves.fidelity", 3),
    LEAF_EFFECT_MAX_PARTICLES("global.falling-leaves.max-particles", 200),
    LEAF_EFFECT_SCAN_DELAY("global.falling-leaves.scan-delay", 100),
    LEAF_EFFECT_CHANCE("global.falling-leaves.spawn-chance", 0.005),

    WATERFALLS("global.waterfalls", true),
    RANDOM_TREE_SPREAD("global.randomly-spread-trees.enabled", true),
    RANDOM_TREE_SPREAD_RANGE("global.randomly-spread-trees.range", 20),
    RANDOM_TREE_SPREAD_SCANS("global.randomly-spread-trees.scans", 2),
    AGE_ITEMS("global.age-items", true),

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

    TEMPERATURE_ENABLED("temperature.enabled", true),
    TEMPERATURE_DAMAGE("temperature.cause-damage", true),
    TEMPERATURE_SPRINTING("temperature.increase-from-sprinting", true),
    TEMPERATURE_SWEAT("temperature.sweat", true),
    TEMPERATURE_BURN("temperature.burn", true),
    TEMPERATURE_BURN_THR("temperature.damage-threshold-heat", 50.0),
    TEMPERATURE_FREEZE("temperature.freeze", true),
    TEMPERATURE_FREEZE_THR("temperature.damage-threshold-cold", -5.0),
    TEMPERATURE_MAX_DELTA("temperature.max-delta-change", 15),
    TEMPERATURE_TEXT("temperature.display.text", "%temperature% %temp_icon%"),
    TEMPERATURE_BAR_STYLE("temperature.display.style", StatusRenderMethod.BOSSBAR.name()),
    TEMPERATURE_BAR_FLASH("temperature.display.warning-flash", true),
    TEMPERATURE_USE_BLOCKS("temperature.advanced.use-blocks", true),
    TEMPERATURE_USE_BIOMES("temperature.advanced.use-biomes", true),
    TEMPERATURE_USE_ENVIRO("temperature.advanced.use-environment", true),
    TEMPERATURE_THREADS("temperature.advanced.thread-limit", 20),
    TEMPERATURE_POOL_SIZE("temperature.advanced.chunk-pool-size", 20),
    TEMPERATURE_SAVE_DATA("temperature.advanced.save-chunk-data", true),

    HYDRATION_ENABLED("hydration.enabled", true),
    HYDRATION_WALKING("hydration.increase-from-activity", true),
    HYDRATION_DAMAGE("hydration.damage-when-empty", true),
    HYDRATION_BAR_STYLE("hydration.display.style", StatusRenderMethod.BOSSBAR.name()),
    HYDRATION_BAR_FLASH("hydration.display.warning-flash", true);

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
