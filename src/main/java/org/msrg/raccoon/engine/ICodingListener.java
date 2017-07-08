/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine;

import org.msrg.raccoon.engine.task.result.CodingResult;

public interface ICodingListener {
    void codingStarted(CodingResult result);

    void codingPreliminaryStageCompleted(CodingResult result);

    void codingFailed(CodingResult result);

    void codingFinished(CodingResult result);
}
