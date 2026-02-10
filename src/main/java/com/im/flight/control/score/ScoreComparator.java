/*
 * Created on 2.1.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.im.flight.control.score;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import javax.microedition.rms.RecordComparator;

/**
 * @author surpila
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ScoreComparator implements RecordComparator {

	public ScoreComparator() {
		super();
	}

	/* (non-Javadoc)
	 * @see javax.microedition.rms.RecordComparator#compare(byte[], byte[])
	 */
	public int compare(byte[] record1, byte[] record2) {

		ScoreEntry score1 = ScoreStore.createScoreEntry(record1);
		ScoreEntry score2 = ScoreStore.createScoreEntry(record2);

		if(score1.score<score2.score) {
			return FOLLOWS;
		}
		else if(score1.score>score2.score) {
			return PRECEDES;
		}
		else if(score1.time>score2.time){
			return FOLLOWS;
		}
		else if(score1.time<score2.time){
			return PRECEDES;
		}
		else {
			return EQUIVALENT;
		}
	}
}
