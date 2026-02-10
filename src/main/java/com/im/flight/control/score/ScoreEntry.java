/*
 * Created on 31.12.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.im.flight.control.score;

/**
 * @author surpila
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ScoreEntry {

	public int score;
	public String name;
	public long time;
	public int recordId = -1;

	public ScoreEntry(String name, int score, long time) {
		this.name = name;
		this.score = score;
		this.time = time;
	}

	public ScoreEntry(String name, int score, long time, int recordId) {
		this(name,score,time);
		this.recordId = recordId;
	}

}
