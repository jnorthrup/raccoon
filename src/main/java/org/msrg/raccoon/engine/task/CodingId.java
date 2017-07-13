/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.task;

import org.jetbrains.annotations.Nullable;

public final class CodingId {

    private static int _lastId;

    private final int _id;

    private CodingId(int id) {
        _id = id;
    }

    public static synchronized CodingId getNewCodingId() {
        return new CodingId(CodingId._lastId++);
    }


    public boolean equals(@Nullable Object obj) {
        if (obj == null)
            return false;
//        if (!obj.getClass().isAssignableFrom(this.getClass()))
//            return false;

        CodingId idObj = (CodingId) obj;
        return _id == idObj._id;
    }


    public int hashCode() {
        return _id;
    }


    public String toString() {
        return "ID[" + _id + "]";
    }
}
