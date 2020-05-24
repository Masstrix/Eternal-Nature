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

package me.masstrix.eternalnature.core.world;

public interface AgeItem {

    /**
     * Returns if the item being aged is still valid. Any items that are picked
     * up, removed etc become invalid and will be removed from the aging process.
     *
     * @return if the item is still valid.
     */
    boolean isValid();

    /**
     * Returns if the items aging process is complete.
     *
     * @return if complete.
     */
    boolean isDone();

    /**
     * Ticks the item in its aging process. Is the item is done aging the it should
     * return {@link AgeProcessState#COMPLETE}. If item is still in the process of
     * aging and is still valid then {@link AgeProcessState#AGING} should be returned.
     *
     * @return the items aging state.
     */
    AgeProcessState tick();

    enum AgeProcessState {
        AGING, COMPLETE, INVALID
    }
}
