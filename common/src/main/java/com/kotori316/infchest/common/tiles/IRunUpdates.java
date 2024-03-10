package com.kotori316.infchest.common.tiles;

import java.util.Collection;
import java.util.function.Predicate;

public interface IRunUpdates {

    void addUpdate(Runnable runnable);

    /**
     * Read only.
     * @return the list of actions done.
     */
    Collection<Runnable> getUpdates();

    void runUpdateRemoveIf(Predicate<Runnable> predicate);

    /**
     * Called in markDirty().
     */
    default void runUpdates() {
        getUpdates().forEach(Runnable::run);
    }
}
