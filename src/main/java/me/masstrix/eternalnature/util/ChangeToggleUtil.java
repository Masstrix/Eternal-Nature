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

package me.masstrix.eternalnature.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChangeToggleUtil {

    private int cursor = 0;
    private List<ChanceSelection> selections = new ArrayList<>();

    /**
     * Adds a new selection value into the array. This will be sorted
     * by the chance once added.
     *
     * @param name name of selection.
     * @param val chance value associated with it.
     */
    public void add(String name, double val) {
        selections.add(new ChanceSelection(name, val));
        sort();
    }

    /**
     * Looks through all the added chance values and finds the closest
     * one to the given value and sets the cursor position there,
     *
     * @param value value to find closest to.
     */
    public void selectClosest(double value) {
        double closest = 0;
        int closestPosition = 0;
        for (int i = 0; i < selections.size(); i++) {
            ChanceSelection item = selections.get(i);
            double distance = MathUtil.diff(item.chance, value);
            if (i == 0) {
                closest = distance;
            }
            else if (distance < closest) {
                closest = distance;
                closestPosition = i;
            }
        }

        cursor = closestPosition;
    }

    public ChanceSelection getSelected() {
        return selections.get(cursor);
    }

    public ChanceSelection getNext() {
        if (cursor + 1 >= selections.size()) {
            return selections.get(0);
        }
        return selections.get(cursor + 1);
    }

    public void next() {
        cursor++;
        if (cursor >= selections.size()) cursor = 0;
    }

    private void sort() {
        selections.sort(Comparator.comparingDouble(o -> o.chance));
    }

    public static class ChanceSelection {
        private String name;
        private double chance;

        ChanceSelection(String name, double chance) {
            this.name = name;
            this.chance = chance;
        }

        public String getName() {
            return name;
        }

        public double getChance() {
            return chance;
        }
    }
}
