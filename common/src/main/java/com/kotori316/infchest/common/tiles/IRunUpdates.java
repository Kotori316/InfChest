package com.kotori316.infchest.common.tiles;

import java.util.Collection;

public interface IRunUpdates {

    void addUpdate(Runnable runnable);

    /**
     * Read only.
     * @return the list of actions done.
     */
    Collection<Runnable> getUpdates();

    /**
     * Called in markDirty().
     */
    default void runUpdates() {
        getUpdates().forEach(Runnable::run);
    }
}
