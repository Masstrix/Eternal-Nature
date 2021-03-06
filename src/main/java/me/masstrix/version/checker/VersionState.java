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

package me.masstrix.version.checker;

public enum VersionState {

    BEHIND, CURRENT, AHEAD, UNKNOWN;

    public static VersionState get(int val) {
        switch (val) {
            case 1: return AHEAD;
            case 0: return CURRENT;
            case -1: return BEHIND;
        }
        return UNKNOWN;
    }
}
