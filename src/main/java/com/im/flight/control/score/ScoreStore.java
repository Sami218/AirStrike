/*
 * Created on 31.12.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.im.flight.control.score;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

import com.im.flight.control.score.*;


/**
 * @author surpila
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ScoreStore {

	private static final int NUMBER_OF_RECORDS = 5;

	private int currentScore = 0;

	public ScoreStore() throws IOException, RecordStoreException, RecordStoreFullException, RecordStoreNotFoundException {
		super();
//		resetHighScores();
	}

	private RecordStore openStore(int skillLevel) throws RecordStoreException {
		try {
			RecordStore recordStore = RecordStore.openRecordStore("scores"+skillLevel,false);
			return recordStore;
		}
		catch(RecordStoreNotFoundException e) {
			RecordStore recordStore = RecordStore.openRecordStore("scores"+skillLevel,true);
			ScoreEntry nullScore = new ScoreEntry("   ",0,0);
			for(int i=0;i<NUMBER_OF_RECORDS;i++) {
				createRecord(nullScore,recordStore);
			}

			return recordStore;
		}
	}

	private void closeStore(RecordStore store) {
		try {
			store.closeRecordStore();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private byte[] createRecordData(ScoreEntry scoreEntry) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream outputStream = new DataOutputStream(baos);

			outputStream.writeUTF(scoreEntry.name);
			outputStream.writeInt(scoreEntry.score);
			outputStream.writeLong(scoreEntry.time);

			return  baos.toByteArray();
		}
		catch(IOException e) {
			return null;
		}
	}

	public static ScoreEntry createScoreEntry(byte[] recordData) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(recordData);
			DataInputStream inputStream = new DataInputStream(bais);

			return new ScoreEntry(inputStream.readUTF(),inputStream.readInt(),inputStream.readLong());
		}
		catch(IOException e) {
			e.printStackTrace();
			return new ScoreEntry("   ",0,0);
		}
	}

	private int createRecord(ScoreEntry scoreEntry,RecordStore recordStore) throws RecordStoreException {

		byte[] b = createRecordData(scoreEntry);
		return recordStore.addRecord(b, 0, b.length);
	}

	private void updateRecord(int recordId, ScoreEntry scoreEntry, RecordStore recordStore) throws  RecordStoreException {

		byte[] recordData = createRecordData(scoreEntry);
		recordStore.setRecord(recordId,recordData, 0, recordData.length);

		throw new RecordStoreException();
	}

	public void addToHighScores(ScoreEntry scoreEntry,int skillLevel) throws RecordStoreException{
//		System.out.println("Adding high score:"+scoreEntry.name+","+scoreEntry.score+","+scoreEntry.time);

		RecordStore recordStore = null;
		try {
			Vector highScores = getHighScores(skillLevel);
			ScoreEntry worstScore = (ScoreEntry)highScores.elementAt(highScores.size()-1);

			recordStore = openStore(skillLevel);
			recordStore.deleteRecord(worstScore.recordId);
			createRecord(scoreEntry,recordStore);

			recordStore.closeRecordStore();
		}
		catch(RecordStoreException rse) {
			recordStore.closeRecordStore();
			throw rse;
		}
	}

	public void addToScore(int points) {
		currentScore = currentScore+points;
	}

	public int getScore() {
		return currentScore;
	}

	public void setScore(int points) {
		currentScore = points;
	}

	public Vector getHighScores(int skillLevel) throws RecordStoreException {
		RecordStore recordStore = null;
		try {
			recordStore = openStore(skillLevel);
			// PORTED: renamed 'enum' to 'recordEnum' (enum is a reserved word in modern Java)
			RecordEnumeration recordEnum = recordStore.enumerateRecords(null,new ScoreComparator(),false);
			Vector highScores = new Vector();
//			Logger.logTrace("Get high scores");
			while(recordEnum.hasNextElement()) {
				int recordId = recordEnum.nextRecordId();
				byte[] record = recordStore.getRecord(recordId);
//				Logger.logTrace("Record: "+new String(record));
				ScoreEntry entry = createScoreEntry(record);
				entry.recordId = recordId;
				highScores.addElement(entry);
			}

			recordStore.closeRecordStore();

			return highScores;
		}
		catch(RecordStoreException rse) {
			recordStore.closeRecordStore();
			throw rse;
		}
	}

	public boolean isHighScore(int score,int skillLevel) throws RecordStoreException {
		Vector highScores = getHighScores(skillLevel);

		for(int i=0;i<highScores.size();i++) {
			ScoreEntry entry = (ScoreEntry)highScores.elementAt(i);
			if(score>entry.score) {
				return true;
			}
		}

		return false;
	}
/*
	public void resetHighScores() throws RecordStoreException {
		RecordStore.deleteRecordStore("scores1");
		RecordStore.deleteRecordStore("scores2");
		RecordStore.deleteRecordStore("scores3");
	}
*/
}
