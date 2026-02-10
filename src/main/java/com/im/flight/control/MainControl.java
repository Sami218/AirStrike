/* This file was created by Nokia Developer's Suite for J2ME(TM) */

package com.im.flight.control;

import java.io.IOException;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;

import com.im.flight.control.score.ScoreEntry;
import com.im.flight.control.score.ScoreStore;
import com.im.flight.view.game.GameCanvas;
import com.im.flight.view.game.GameObject;
import com.im.flight.view.menu.FullScreenInfo;
import com.im.flight.view.menu.MenuScreen;
import com.im.util.ObjectProperties;
import com.im.util.SoundEffects;
import com.im.util.TextResources;
import com.nokia.mid.sound.Sound;
import com.nokia.mid.ui.DeviceControl;

public class MainControl implements Runnable, CommandListener {

	private static final byte START_LEVEL = 1;
	private static final byte LEVEL_COUNT = 4;

    private static final byte STATE_WELCOME 		= 1;
    private static final byte STATE_MENU    		= 2;
//    private static final byte STATE_PLAYING 		= 3;
//    private static final byte STATE_LOADING 		= 4;
//    private static final byte STATE_PAUSED  		= 5;
    private static final byte STATE_SCORE 			= 6;
    private static final byte STATE_SCORE_HISCORE 	= 7;
    private static final byte STATE_HISCORE 		= 8;
    private static final byte STATE_SETTINGS 		= 9;
//    private static final byte STATE_HELP			= 10;
    private static final byte STATE_COMPLETED		= 11;
    private static final byte STATE_LOADER			= 12;
    private static final byte STATE_LICENSE			= 13;

//    public static ApplicationProperties appConfiguration;
//    public Settings settings;
    public MIDlet midlet;
    private Command yesCommand;
    private Command noCommand;
    private Command exitCommand;
    private Command quitGameCommand;
    private Command backToMenuCommand;
    private Command continueCommand;
    private Command selectCommand;
    private Command toggleCommand;
    private Command restartLevelCommand;
 //   private Displayable currentScreen = null;
    private MenuScreen currentScreen = null;
    private GameControl gameControl = null;
//    private GameLoader loader = null;
    private Thread gameThread = null;
    private Thread loaderThread = null;
    public ScoreStore scoreStore;
    public int level = START_LEVEL;
    private int shipsDestroyed = 0;
    public static MainControl instance;
    private int scoreOnlevelStart = 0;

    private int applicationState = 0;


    // Settings
    public byte skillLevel = 1;
    public boolean useVibration = true;
    public boolean useBacklight = true;
    public boolean useSounds = false;
    public boolean cheatMode = false;


    public MainControl(MIDlet starter) {

        try {
        	midlet = starter;

        	instance = this;

        	backToMenuCommand = new Command(TextResources.COMMAND_BACKTOMENU,Command.EXIT,1);
        	yesCommand = new Command("Yes",Command.OK,1);
        	noCommand = new Command("No",Command.CANCEL,1);

        	// Skip the license/evaluation screen and go straight to loader
        	showScreen(MenuScreen.MENU_LOADER);
            loaderThread = new Thread(this);
            Display.getDisplay(midlet).callSerially(loaderThread);
    	} catch(Throwable t) {
        	t.printStackTrace();
        	showErrorDialog(TextResources.ERROR,t,null);
        	try {
        		Thread.sleep(10000);
        	}
        	catch(InterruptedException e) {}
        	// In browser port, System.exit is a no-op
        	System.out.println("MainControl init error - would have called System.exit(0)");
        }
    }

    private void initApp() {
    	try {

//	    	GameObject.preLoadImages();

//	    	Thread.sleep(3000);

	    } catch(Throwable t) {
	    	t.printStackTrace();
	    	showErrorDialog(TextResources.ERROR,t,null);
	    	try {
	    		Thread.sleep(10000);
	    	}
	    	catch(InterruptedException e) {}
	    	System.out.println("initApp error - would have called System.exit(0)");
	    }

    }

    private void progressLoading() {
    	currentScreen.loadingProgress++;
    	currentScreen.repaint();
    }

    public void commandAction( Command p1, Displayable p2 ) {
    	try {
    		if(p1 == yesCommand) {
    			showScreen(MenuScreen.MENU_LOADER);
    			loaderThread = new Thread(this);
    			Display.getDisplay(midlet).callSerially(loaderThread);
    		}
    		else if( p1 == noCommand) {
    			// In browser port, notifyDestroyed is a no-op
    			if (midlet != null) {
    				midlet.notifyDestroyed();
    			} else {
    				System.out.println("Game exit requested (no MIDlet to destroy)");
    			}
    		}
    		else if(p1 == continueCommand) {
            	if(applicationState == STATE_COMPLETED) {
            		showScreen(MenuScreen.MENU_SCORE);
            	}
            	else {
/*
            		currentScreen.freeImages();
            		currentScreen = null;
            		System.gc();
*/
            		Displayable gameCanvas = gameControl.getCanvas();
            		Display.getDisplay(midlet).setCurrent(gameCanvas);
            		gameThread.start();
            	}
            }
            else if(p1 == quitGameCommand) {
                if(applicationState == STATE_SCORE_HISCORE) {
 	                try {
 	                	level = 1;
 	                	addHighScore(scoreStore.getScore(),currentScreen.getInitials());
 	                	showScreen(MenuScreen.MENU_HISCORE);
 	                }
 	                catch(RecordStoreException re) {
 	                	showScreen(MenuScreen.MENU_MENU);
 	                	showErrorDialog(TextResources.ERROR_HIGHSCORE,re,currentScreen);
 	                }
                }
                else {
                	showScreen(MenuScreen.MENU_MENU);
                }
/*
                else if(applicationState == STATE_HISCORE) {
                	showMenuScreen();
                }
*/
            }
            else if(p1 == restartLevelCommand) {
            	if(applicationState == STATE_SCORE_HISCORE) {
            		int score = scoreStore.getScore();
            		restartLevel();
            		try {
            			addHighScore(score,currentScreen.getInitials());
            		}
            		catch(RecordStoreException re) {
            			showErrorDialog(TextResources.ERROR_HIGHSCORE,re,currentScreen);
            		}
            	}
            	else {
            		restartLevel();
            	}
            	scoreStore.setScore(0);
            }
            else if(p1 == exitCommand) {
            	// In browser port, notifyDestroyed is a no-op
            	if (midlet != null) {
            		midlet.notifyDestroyed();
            	} else {
            		System.out.println("Game exit requested (no MIDlet to destroy)");
            	}
            }
            else if(p1 == backToMenuCommand) {
            	if(applicationState == STATE_SETTINGS) {
            		useSettings();
            		saveSettings();
            		showScreen(MenuScreen.MENU_MENU);
            	}
            	else {
            		showScreen(MenuScreen.MENU_MENU);
            	}
            }
            else if(p1 == selectCommand) {
            	MenuScreen menu = (MenuScreen)currentScreen;
                if(menu.selectedIndex == 0) {
                	scoreStore.setScore(0);
                	loadGame(START_LEVEL);
                }
                else if(menu.selectedIndex == 1) {
                	showScreen(MenuScreen.MENU_SETTINGS);
                }
                else if(menu.selectedIndex == 2) {
                	showScreen(MenuScreen.MENU_HISCORE);
                }
                else if(menu.selectedIndex == 3) {
                	showScreen(MenuScreen.MENU_HELP);
                }
            }
            else if(p1 == toggleCommand) {
            	int skillLevel = currentScreen.skillLevel;
               	currentScreen.skillLevel++;
               	if(currentScreen.skillLevel == 4) {
               		currentScreen.skillLevel = 1;
               	}
               	currentScreen.scoreEntries = scoreStore.getHighScores(currentScreen.skillLevel);
            	currentScreen.repaint();
            }

            SoundEffects.stopSound();
        }
        catch(Throwable t) {
        	showErrorDialog(null, t,null);
            t.printStackTrace();
        }
    }

    private void showScreen(byte screenType) {
    	int originalState = applicationState;
    	try {
	    	currentScreen = new MenuScreen(screenType);
	    	switch(screenType) {
	    		case MenuScreen.MENU_LICENSE:
	    			currentScreen.addCommand(yesCommand);
	    			currentScreen.addCommand(noCommand);
	    			applicationState = STATE_LICENSE;
	    			break;
	    		case MenuScreen.MENU_LOADER:
//	    			currentScreen.addCommand(backToMenuCommand);
	    			applicationState = STATE_LOADER;
	    			break;
	    		case MenuScreen.MENU_SPLASH:
	    			currentScreen.addCommand(backToMenuCommand);
	    			applicationState = STATE_WELCOME;
	    			break;
	    		case MenuScreen.MENU_MENU:
	    			currentScreen.addCommand(selectCommand);
	    			currentScreen.addCommand(exitCommand);
	    			applicationState = STATE_MENU;
	    			break;
	    		case MenuScreen.MENU_SETTINGS:
	    			currentScreen.addCommand(selectCommand);
	    			currentScreen.addCommand(backToMenuCommand);
	    			applicationState = STATE_SETTINGS;
	    			break;
	    		case MenuScreen.MENU_HISCORE:
	    			currentScreen.addCommand(backToMenuCommand);
	    			currentScreen.addCommand(toggleCommand);
	    			applicationState = STATE_HISCORE;
	    			break;
	    		case MenuScreen.MENU_SCORE:
	    			int score = scoreStore.getScore();
	    			boolean isHighScore = false;
	    			try {
	    				isHighScore = scoreStore.isHighScore(score,skillLevel);
	    			}
	    			catch(RecordStoreException e) {
	    				// Do nothing, the high score test is not done
	    			}
	    			if(isHighScore) {
	    				applicationState = STATE_SCORE_HISCORE;
	    				SoundEffects.playEffect(SoundEffects.HIGH_SCORE);
	    			}
	    			else {
	    				applicationState = STATE_SCORE;
	    			}
	    			currentScreen.score = score;
	    			currentScreen.highScore = isHighScore;
	    			currentScreen.addCommand(restartLevelCommand);
	    			currentScreen.addCommand(quitGameCommand);
	    			break;
	    		case MenuScreen.MENU_HELP:
	    		case MenuScreen.MENU_ABOUT:
	    			currentScreen.addCommand(backToMenuCommand);
	    			break;
	    	}

	    	setToScreen(currentScreen);
    	}
    	catch(RecordStoreException e) {
    		applicationState = originalState;
    		showErrorDialog(TextResources.ERROR_HIGHSCORE,e,null);
    	}
    }


    private void useSettings() {
    	if(useBacklight) {
    		DeviceControl.setLights(0,100);
    	}
    	else {
    		DeviceControl.setLights(0,0);
    	}
    }


    private void loadGame(int level) {
    	freeMemory();
    	showScreen(MenuScreen.MENU_LEVEL);

    	currentScreen.repaint();
    	currentScreen.serviceRepaints();

    	if (loaderThread == null) {
    		loaderThread = new Thread(this);
    	}
    	Display.getDisplay(midlet).callSerially(loaderThread);
    }



    private void freeMemory() {
    	if(currentScreen != null) {
    		currentScreen.freeImages();
    		currentScreen = null;
    	}

    	if(gameControl != null) {
    		gameControl.freeImages();
    		gameControl = null;
    		gameThread = null;
    	}
    	System.gc();
    }

    public void showErrorDialog(String message, Throwable t, Displayable d) {
    	t.printStackTrace();
    	if(message==null) {
    		message = t.toString();
    	}
    	if(d == null) {
    		d = Display.getDisplay(midlet).getCurrent();
    	}

    	Alert alert = new Alert(TextResources.ERROR,message,null,AlertType.ERROR);
    	Display.getDisplay(midlet).setCurrent(alert,d);
    }

    private void setToScreen(Displayable screen) {
    	screen.setCommandListener(this);
    	Display.getDisplay(midlet).setCurrent(screen);
    	System.gc();
    }

    public void nextLevel() {
    	freeMemory();
    	level++;
    	scoreOnlevelStart = scoreStore.getScore();
    	if(level>LEVEL_COUNT) {
    		freeMemory();

    		String targetInfo = TextResources.TARGET_INFO+shipsDestroyed+"/4";
    		showScreen(MenuScreen.MENU_LEVEL);
    		currentScreen.addCommand(continueCommand);

    		applicationState = STATE_COMPLETED;

    		setToScreen(currentScreen);

    		SoundEffects.playEffect(SoundEffects.HIGH_SCORE);
    	}
    	else {
    		loadGame(level);
    	}
    }

    private void restartLevel() {
    	if(level>LEVEL_COUNT) {
    		resetGame();
    	}

    	freeMemory();
    	shipsDestroyed = 0;
    	loadGame(level);
    }

    private void resetGame() {
    	level = 1;
    	scoreOnlevelStart = 0;
    	scoreStore.setScore(0);
    	shipsDestroyed = 0;
    }

    public void gameOver() {
    	freeMemory();
    	showScreen(MenuScreen.MENU_SCORE);
    }

    public void interruptGame() {
    	freeMemory();
    	resetGame();
    	showScreen(MenuScreen.MENU_MENU);
    }

    private void addHighScore(int points,String initials) throws RecordStoreException {
	    ScoreEntry scoreEntry = new ScoreEntry(initials,points,System.currentTimeMillis());
	    scoreStore.addToHighScores(scoreEntry,skillLevel);
    }

    /**
     *
     * @param points Points to add
     * @returns the currents score
     */
    public int addToScore(int points) {
    	scoreStore.addToScore(points);
    	return scoreStore.getScore();
//    	Logger.logTrace("Added to score "+points);
    }

    public void shipDestroyed() {
    	shipsDestroyed++;
    	addToScore(500);
    	gameControl.gameCanvas.updateScore(scoreStore.getScore());
    }

    public void run() {
    	try {
	    	if(applicationState == STATE_WELCOME || applicationState == STATE_LOADER) {
	    		// Preload images
	    		switch(MenuScreen.loadingProgress) {
	    			case 0:
	    				exitCommand = new Command(TextResources.COMMAND_EXIT,Command.EXIT,1);
			    		quitGameCommand = new Command(TextResources.COMMAND_QUIT,Command.EXIT,1);
			    		continueCommand = new Command(TextResources.COMMAND_CONTINUE,Command.OK,1);
			    		restartLevelCommand= new Command(TextResources.COMMAND_RESTARTLEVEL,Command.OK,1);
			    		toggleCommand = new Command(TextResources.COMMAND_TOGGLE,Command.OK,1);
			    		selectCommand = new Command(TextResources.COMMAND_SELECT,Command.OK,1);
			    		break;
			    	case 1:
			    		scoreStore = new ScoreStore();
			    		break;
			    	case 2:
			    		loadSettings();
			    		break;
			    	case 3:
			    		useSettings();
			    		break;
			    	case 4:
			    		SoundEffects.initEffects();
			    		break;
			    	default:
			    		if(MenuScreen.loadingProgress<GameObject.imagesToPreload.length+5) {
			    			GameObject.getImage(GameObject.imagesToPreload[MenuScreen.loadingProgress-5]);
			    		}
			    		else {
			    			showScreen(MenuScreen.MENU_SPLASH);
			    			SoundEffects.playEffect(SoundEffects.INTRO);
//			    			System.out.println("Loaded "+MenuScreen.loadingProgress+" steps");
			    		}
	    		}
	    		progressLoading();
	    		if(applicationState == STATE_LOADER) {
	    			Display.getDisplay(midlet).callSerially(loaderThread);
	    		}
	    	}
	    	else {
	    		// Load level
	    		gameControl = new GameControl(this,level);
		    	gameThread = new Thread(gameControl);

		    	currentScreen.gameLoaded = true;
		    	currentScreen.addCommand(continueCommand);
	    	}
	    	currentScreen.repaint();
	    } catch(Throwable t) {
	    	t.printStackTrace();
	    	showErrorDialog(TextResources.ERROR,t,null);
	    	try {
	    		Thread.sleep(10000);
	    	}
	    	catch(InterruptedException e) {}
	    	System.out.println("Run error - would have called System.exit(0)");
	    }

    }

    public void loadSettings() {
    	try {
//			Logger.logTrace("loading settings");

    		RecordStore recordStore = null;
    		try {
    			//RecordStore.deleteRecordStore("settings");
    			recordStore = RecordStore.openRecordStore("settings",false);
//				Logger.logTrace("Found record store");
    			RecordEnumeration recordEnum = recordStore.enumerateRecords(null,null,false);
    			byte[] record = recordEnum.nextRecord();
//				Logger.logTrace("Found record: "+new String(record));
    			if(record.length>3) {
    				skillLevel = record[0];
    				useVibration = record[1]>0;
    				useBacklight = record[2]>0;
    				useSounds = record[3]>0;
    			}
    		}
    		catch(RecordStoreNotFoundException ne) {
//				Logger.logTrace("Creating a new record store");
    			recordStore = RecordStore.openRecordStore("settings",true);
    			recordStore.addRecord(createRecordData(),0,4);
//				Logger.logTrace("Createad a new record");
    		}
    		catch(RecordStoreException e) {
    			e.printStackTrace();
    		}

//			Logger.logTrace("Closing record store");
    		recordStore.closeRecordStore();
    	}
    	catch(RecordStoreException e) {
    		e.printStackTrace();
    	}
    }

    public void saveSettings() {
    	try {
//			Logger.logTrace("Saving settings");
    		RecordStore recordStore = null;
    		try {
    			recordStore = RecordStore.openRecordStore("settings",false);
    			RecordEnumeration recordEnum = recordStore.enumerateRecords(null,null,false);
    			int recordId = recordEnum.nextRecordId();
//				Logger.logTrace("Found record: "+recordId);

    			byte[] record = createRecordData();

    			recordStore.setRecord(recordId,record,0,4);
//				Logger.logTrace("Updated record: "+recordId);

    		}
    		catch(RecordStoreException e) {
    			e.printStackTrace();
    		}

    		recordStore.closeRecordStore();
    	}
    	catch(RecordStoreException e) {
    		e.printStackTrace();
    	}
    }

    private byte[] createRecordData() {
    	byte[] record = new byte[4];
    	record[0] = (byte)skillLevel;
    	if(useVibration) {
    		record[1] = 1;
    	}
    	else {
    		record[1] = 0;
    	}

    	if(useBacklight) {
    		record[2] = 1;
    	}
    	else {
    		record[2] = 0;
    	}

    	if(useSounds) {
    		record[3] = 1;
    	}
    	else {
    		record[3] = 0;
    	}

    	return record;
    }

}
