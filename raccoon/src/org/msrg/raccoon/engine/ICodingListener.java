/*
 * Raccoon Network Coding Engine
 * @author Reza Sherafat (reza.sherafat@gmail.com)
 * Copyright (c) 2012, MSRG, University of Toronto. All rights reserved.
 */

package org.msrg.raccoon.engine;

import org.msrg.raccoon.engine.task.result.CodingResult;

public interface ICodingListener {

	public void codingStarted(CodingResult result);
	public void codingPreliminaryStageCompleted(CodingResult result);
	public void codingFailed(CodingResult result);
	public void codingFinished(CodingResult result);
	
}
