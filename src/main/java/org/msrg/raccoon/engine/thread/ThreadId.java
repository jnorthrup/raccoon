/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.thread;

import org.jetbrains.annotations.Nullable;

public final class ThreadId {

    private static int _lastId;

    private final int _id;

    private ThreadId(int id) {
        _id = id;
    }

    public static synchronized ThreadId getNewThreadId() {
        return new ThreadId(ThreadId._lastId++);
    }


    public boolean equals(@Nullable Object obj) {
        if (obj == null)
            return false;
        if (!obj.getClass().isAssignableFrom(this.getClass()))
            return false;

        ThreadId idObj = (ThreadId) obj;
        return _id == idObj._id;
    }


    public int hashCode() {
        return _id;
    }


    public String toString() {
        return "TID[" + _id + "]";
    }

}
