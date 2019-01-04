package com.kotori316.infchest.tiles;

import java.util.Collection;

public interface IRunUpdates {

    void addUpdate(Runnable runnable);

    Collection<Runnable> getUpdates();

    default void runUpdates() {
        getUpdates().forEach(Runnable::run);
    }
}
