/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine.task;

import org.jetbrains.annotations.NotNull;

public class CodingTaskFailed extends UnknownError {

    /**
     * Auto Generated
     */
    private static final long serialVersionUID = -9216406694103472730L;

    public final CodingTask _cTask;

    public CodingTaskFailed(CodingTask cTask) {
        _cTask = cTask;
    }

    @NotNull

    public String toString() {
        return "CodingTaskFailure:" + _cTask;
    }
}
