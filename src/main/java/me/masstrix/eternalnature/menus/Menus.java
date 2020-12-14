/*
 * Copyright 2020 Matthew Denton
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

package me.masstrix.eternalnature.menus;

public enum Menus {

    SETTINGS("settings_main"),
    LEAF_PARTICLE_SETTINGS("leaf_effect_settings"),
    HYDRATION_SETTINGS("hydration_settings"),
    OTHER_SETTINGS("other_settings"),
    TEMP_SETTINGS("temp_settings"),
    TEMP_SCANNING_SETTINGS("temp_scanning_settings"),
    LANG_SETTINGS("lang_settings");

    private final String id;

    Menus(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
