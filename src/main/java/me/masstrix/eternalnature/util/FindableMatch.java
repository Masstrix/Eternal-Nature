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


import java.util.*;
import java.util.stream.Collectors;

/**
 * Allows for simple matching and finding.
 *
 * @param <K> some kind of number that is being matched.
 */
public interface FindableMatch<K extends Number> {

    K getMatchingValue();

    /**
     * Different methods used to match and find {@link FindableMatch} objects by.
     */
    enum MatchMethod {
        /**
         * Finds the closest match in the collection and returns it. This will return the item
         * with the smallest differance to the current <i>find</i> parameter.
         */
        CLOSEST {
            @Override
            public <K extends Number> FindableMatch<K> find(Collection<? extends FindableMatch<K>> array, K find) {
                FindableMatch<K> closest = null;
                AbstractNumber selectedDiff = null;
                for (FindableMatch<K> i : array) {
                    if (i.getMatchingValue().equals(find)) return i;
                    AbstractNumber diff = new AbstractNumber(i.getMatchingValue()).subtract(find).abs();
                    if (closest == null || selectedDiff.greaterThan(diff.get())) {
                        closest = i;
                        selectedDiff = diff;
                    }
                }
                return closest;
            }
        },
        /**
         * Finds the closest match in a linear up way. This works in a similar way to the floor method
         * as it will always default to the lowest value it can.
         * <p>
         * Example: If we have [1, 10, 30, 50] and are looking for 9. The item with the {@link #getMatchingValue()}
         * of 1 will be returned, but soon as <i>find</i> is equal to 10, it will return the item with 10 up until it
         * reaches 29. The same will happen if <i>find</i> is equal to 49, the item with 30 will be returned.
         */
        LINEAR {
            @Override
            public <K extends Number> FindableMatch<K> find(Collection<? extends FindableMatch<K>> array, K find) {
                FindableMatch<K> last = null;

                List<? extends FindableMatch<K>> sorted = array.stream().sorted((o1, o2)
                        -> AbstractNumber.compare(o1.getMatchingValue(), o2.getMatchingValue()))
                        .collect(Collectors.toList());

                for (FindableMatch<K> i : sorted) {
                    if (i.getMatchingValue().equals(find)) return i;

                    if (last == null) {
                        last = i;
                        continue;
                    }

                    AbstractNumber in = new AbstractNumber(i.getMatchingValue());

                    if (in.greaterThan(find)) {
                        break;
                    }
                    last = i;
                }
                return last;
            }
        };

        /**
         * Finds a matching method by a string. If no method matches the given string
         * then the linear method will be returned.
         *
         * @param str string to find a method for,
         * @return the matched method or linear if none is found.
         */
        public static MatchMethod fromString(String str) {
            return fromString(str, LINEAR);
        }

        /**
         * Finds a matching method by a string. If no method matches the given string
         * then the default method will be returned.
         *
         * @param str string to find a method for,
         * @param def default method to return if there was no matching method found.
         * @return the matched method or linear if none is found.
         */
        public static MatchMethod fromString(String str, MatchMethod def) {
            for (MatchMethod m : MatchMethod.values()) {
                if (m.name().equalsIgnoreCase(str)) return m;
            }
            return def;
        }

        /**
         * Finds a value in the array in accordance to <i>find</i>.
         *
         * @param array array to look through to find and match against.
         * @param find  value to find in the array or nearest to.
         * @param <K>   type of number that is being used.
         * @return the closest matching value in the collection or null if there were none.
         */
        public <K extends Number> FindableMatch<K> find(Collection<? extends FindableMatch<K>> array, K find) {
            return array.size() == 0 ? null : array.iterator().next();
        }
    }
}
