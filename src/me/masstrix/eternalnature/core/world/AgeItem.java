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
