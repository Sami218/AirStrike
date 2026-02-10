/* This file was created by Nokia Developer's Suite for J2ME(TM) */

package com.im.flight.control;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import com.im.flight.view.game.Cannon;
import com.im.flight.view.game.GameCanvas;
import com.im.flight.view.game.GameObject;
import com.im.flight.view.game.Rectangle;
import com.im.util.ObjectProperties;
import com.im.util.SoundEffects;
import com.nokia.mid.ui.DeviceControl;
import com.nokia.mid.ui.DirectUtils;
import com.nokia.mid.ui.FullCanvas;

public class GameControl  implements Runnable
{
	private static final int PLANE_DEFAULT_SPEED = 1;
	public static final int STATE_PLAYING = 0;
	public static final int STATE_PAUSED = 1;
	public static final int STATE_INTERRUPTED = 2;
	public static final int STATE_GAMEOVER = 3;
	public static final int STATE_ENDING= 4;
	private static final long GAMETHREAD_DELAY = 50;

	public static byte COLLISION_GROUND = 0;
	public static byte COLLISION_SEA = 1;
	public static byte COLLISION_PLANE = 2;
	public static byte COLLISION_ACTOBJ = 3;
	public static byte COLLISION_PASSOBJ = 4;
	public static byte COLLISION_BULLET = 5;

	private static final byte TICKS_BETWEEN_SHOTS = 10;

	private static final int PARACHUTE_MIN_ALTITUDE = 110;

	public static GameControl instance;
	public int gameState = STATE_PLAYING;
	public boolean gameEnding = false;
	private MainControl parent;
    private Vector activeObjects = new Vector();
    private Vector passiveObjects = new Vector();
    private Vector ownBombs = new Vector();
    private Vector bullets = new Vector();
    public GameObject thePlane;
    private GameObject jumpingPilot = null;
    private byte seaHeight;
    private byte[] landHighLimit;
    public int levelLength;
    public int level = 1;
    public GameCanvas gameCanvas;
//    private Hashtable objectsPropertyTable = new Hashtable();
    private long lastAdvance = System.currentTimeMillis();
    private int ticksSinceLastShot = 0;

    public GameControl(MainControl starter,int currentLevel) {
    	try {
	    	parent = starter;
	    	level = currentLevel;
	    	instance = this;
//	    	preLoadObjects();
	    	readLandscape();
	        readObjects();
	        gameCanvas = new GameCanvas(seaHeight,landHighLimit,passiveObjects,activeObjects,ownBombs,bullets,thePlane);
	        gameCanvas.setKeyboardActionListener(this);
	        gameCanvas.updateScore(starter.scoreStore.getScore());
//	        cleanUpObjectProperties();
    	}
    	catch(Throwable t) {
    		MainControl.instance.showErrorDialog(null,t,null);
    	}
    }

    private void readLandscape() {
        try {
            String landscapeResourceName = "/res/"+level+".l";
//            Logger.logTrace("Opening "+landscapeResourceName);
            // In browser port, use Image's resource cache (preloaded via XHR)
            InputStream inputStream = javax.microedition.lcdui.Image.getResourceAsStream(landscapeResourceName);
            if (inputStream == null) {
                // Fallback to classpath (won't work in browser, but preserves structure)
                inputStream = getClass().getResourceAsStream(landscapeResourceName);
            }
            DataInputStream dataInput = new DataInputStream(inputStream);
            int hiByte = dataInput.readUnsignedByte();
            int loByte = dataInput.readUnsignedByte();
            levelLength = 2 * (((hiByte&0xFFFF)<<8) + (loByte&0xFFFF));
/*
            ApplicationProperties appProps = getObjectProperties("flight");
            int maxLevelLength = Integer.parseInt(appProps.getProperty("landscape.maxlength"));
            if(levelLength>maxLevelLength) {
            	levelLength = maxLevelLength;
            }
*/
            seaHeight = dataInput.readByte();
            landHighLimit = new byte[levelLength];

            byte previousLandHighLimit = 127;
            for(int i=0;i<levelLength;i=i+2) {
            	byte landHigh = dataInput.readByte();
            	landHighLimit[i] = (byte)((previousLandHighLimit+landHigh)/2);
            	landHighLimit[i+1] = landHigh;
            	previousLandHighLimit = landHigh;
            }
        }
        catch(Exception e) {
//            System.out.println("Error reading landscape");
//            e.printStackTrace();
        }
    }

    private void readObjects() {
    	try {
    		String objectResourceName = "/res/"+level+".o";
    		// In browser port, use Image's resource cache (preloaded via XHR)
    		InputStream inputStream = javax.microedition.lcdui.Image.getResourceAsStream(objectResourceName);
    		if (inputStream == null) {
    		    // Fallback to classpath
    		    inputStream = getClass().getResourceAsStream(objectResourceName);
    		}
    		DataInputStream dataInput = new DataInputStream(inputStream);
    		byte readByte = 0;
    		while(true) {
    			byte levelType = dataInput.readByte();
    			int objectX = dataInput.readShort();
    			byte objectY = dataInput.readByte();

    			byte objectType = (byte)(0xF&levelType);
    			int skillLevel = (levelType>>4);

//    			System.out.println("Skill:"+skillLevel+", Type:"+objectType+", x:"+objectX+", y"+objectY);

    			if((objectX<levelLength)&&(skillLevel<=parent.skillLevel)) {
    				ObjectProperties objectProperties = ObjectProperties.getObjectProperties(objectType);

    				GameObject newObject = instantiateGameObject(objectX,objectY,objectType);

    				if(objectProperties.objectMode == GameObject.MODE_ACTIVE) {
    					activeObjects.addElement(newObject);
    				}
    				else {
    					passiveObjects.addElement(newObject);
    				}
    			}
    		}
    	}
    	catch(EOFException eof) {
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    	thePlane = new GameObject(GameObject.PLANE_STARTPOS_X,GameObject.PLANE_STARTPOS_Y,(byte)1,(byte)0,GameObject.TYPE_PLANE);
    }

    private GameObject instantiateGameObject(int x,byte y,byte type) {
    	ObjectProperties props = ObjectProperties.getObjectProperties(type);
    	switch(type) {
    		case GameObject.TYPE_CANNON:
    		case GameObject.TYPE_TANK:
    		case GameObject.TYPE_BOAT:
    		case GameObject.TYPE_SHIP:
    		case GameObject.TYPE_HELICOPTER:
    		case GameObject.TYPE_MOVINGHELICOPTER:
    		case GameObject.TYPE_SUBMARINE:
    			return new Cannon(x,y,type);
    		default:
    			return new GameObject(x,y,(byte)0,(byte)0,type);
    	}
    }

    public Displayable getCanvas() {
        return gameCanvas;
    }

    /**
     * Main loop of the game
     */
    public void run() {
        try {
            while(((thePlane.bounds.left)<levelLength)&&(gameState!=STATE_INTERRUPTED)) {

            	if(gameCanvas.visibleArea.right>=levelLength-1)  {
					gameState = STATE_ENDING;
				}

            	checkForCollisions();


            	if(jumpingPilot != null) {
            		jumpingPilot.advance();
            	}

            	if(gameState == STATE_PLAYING) {
            		// Check is the plane moving out of the visible area
            		checkPlanePosition();
            		gameCanvas.advance();
            	}

            	thePlane.advance();

            	Rectangle visibleArea = gameCanvas.visibleArea;
            	Vector activeActiveObjects = gameCanvas.activeActiveObjects;
                for(int i=0;i<activeActiveObjects.size();i++) {
                	GameObject actObj = (GameObject)activeActiveObjects.elementAt(i);
                	actObj.advance();
                }
                // Advance own objects
                for(int i=ownBombs.size()-1;i>=0;i--) {
                	GameObject tmpObj = (GameObject)ownBombs.elementAt(i);
                	tmpObj.advance();
                	if((tmpObj.markedForDeleting)||(!tmpObj.collidesWith(visibleArea))) {
                		ownBombs.removeElementAt(i);
                	}
                }
                // Advance the bullets shot by enemy objects
                for(int i=bullets.size()-1;i>=0;i--) {
                	GameObject bullet = (GameObject)bullets.elementAt(i);
//                	Logger.logTrace("Advancing bullet #"+i);
                	bullet.advance();
                	// Delete the bullet if it is not on screen, not on active area or is marked for deletion
                	if(bullet.markedForDeleting||((!bullet.collidesWith(visibleArea))&&(!visibleArea.isInside(bullet.xCoord-GameCanvas.OBJECT_ACTIVATION_DISTANCE,bullet.yCoord)))) {
//                		Logger.logTrace("Deleting bullet #"+i);
                		bullets.removeElementAt(i);
                	}
                }

                if(gameState == STATE_GAMEOVER && jumpingPilot.yCoord > PARACHUTE_MIN_ALTITUDE && jumpingPilot.vertSpeed > 0) {
                	break;
                }

                gameCanvas.repaint();

                long sleepTime = GAMETHREAD_DELAY+lastAdvance-System.currentTimeMillis();
                //Logger.logTrace("Thread sleep = "+sleepTime);
                if(sleepTime<0) {
                	sleepTime = 0;
                }

                if(gameCanvas.visibleArea.right > levelLength-20) {
                	gameEnding = true;
                }

                ticksSinceLastShot++;
                Thread.sleep(sleepTime);
                lastAdvance = System.currentTimeMillis();

                while(gameState == STATE_PAUSED) {
                	Thread.yield();
                }
            }

            if(gameState == STATE_INTERRUPTED) {
            	parent.interruptGame();
            }
            else if(gameState == STATE_GAMEOVER) {
            	gameCanvas.setKeyboardActionListener(null);
            	parent.gameOver();
            }
            else {
            	gameCanvas.setKeyboardActionListener(null);
            	parent.nextLevel();
            }
        }
        catch(Throwable e) {
            e.printStackTrace();
            parent.showErrorDialog("Error",e,null);
        }
    }

    public void keyRepeated(int keyCode,int gameActionCode) {
     	keyPressed(keyCode,gameActionCode);
    }

    public void keyPressed(int keyCode,int gameActionCode) {
    	try {
    		if(!thePlane.hasCollided()) {
//    			int ticksSinceLastShot = System.currentTimeMillis()-lastShot;
	    		Rectangle planeBounds = thePlane.bounds;
		    	Rectangle visibleArea = gameCanvas.visibleArea;

		    	switch(keyCode) {
		    		case FullCanvas.KEY_SOFTKEY1:
		    			if(gameState == STATE_PLAYING) {
		    				pauseGame();
		    			}
		    			else if(gameState == STATE_PAUSED) {
		    				continueGame();
		    			}

		    			return;
		    		case FullCanvas.KEY_SOFTKEY2:
		    			if(gameState == STATE_PLAYING) {
		    				pauseGame();
		    			}
		    			else if(gameState == STATE_PAUSED) {
		    				endGame();
		    			}
		    			return;
		    		case FullCanvas.KEY_SOFTKEY3:
		    			if(gameState == STATE_PLAYING) {
		    				pauseGame();
		    			}
		    			return;
		    	}

		    	if(gameState == STATE_PLAYING) {
			    	switch(gameActionCode) {
			    		case FullCanvas.UP:
			    			if(planeBounds.bottom>visibleArea.bottom) {
			    				thePlane.vertSpeed = -2*GameObject.SPEED_SCALE_FACTOR;
			    			}
			    			return;
			    		case FullCanvas.DOWN:
			    			if(planeBounds.top<visibleArea.top) {
			    				thePlane.vertSpeed = 2*GameObject.SPEED_SCALE_FACTOR;
			    			}
			    			return;
			    		case FullCanvas.RIGHT:
			    			if(planeBounds.right<visibleArea.right) {
			    				thePlane.horizSpeed = 2*GameObject.SPEED_SCALE_FACTOR;
			    			}
			    			return;
			    		case FullCanvas.LEFT:
			    			if(planeBounds.left>visibleArea.left) {
			    				thePlane.horizSpeed = 0;
			    			}
			    			return;
			    		case FullCanvas.FIRE:
			    			if(ticksSinceLastShot>TICKS_BETWEEN_SHOTS) {
			    				GameObject newBomb = new GameObject(thePlane.xCoord,(thePlane.bounds.top+3),(byte)1,(byte)1,GameObject.TYPE_BOMB);
			    				//ApplicationProperties bompProps = getObjectProperties("bomb");
			    				//newBomb.setObjectProperties(bompProps);
			    				//newBomb.initImages();
	//		    				newBomb.horizSpeed = thePlane.horizSpeed;
			    				ownBombs.addElement(newBomb);
			    				ticksSinceLastShot = 0;
			    			}
			    			return;
			    		case FullCanvas.GAME_A:
			    			if(ticksSinceLastShot>TICKS_BETWEEN_SHOTS) {
			    				//Logger.logTrace("Missile added");
			    				GameObject newMissile = new GameObject(thePlane.bounds.right-5,(byte)(thePlane.bounds.top+2),(byte)1,(byte)0,GameObject.TYPE_MISSILE);
			    				newMissile.missileDirection = GameObject.MISSILE_RIGHT;

			    				ownBombs.addElement(newMissile);
			    				ticksSinceLastShot = 0;

			    			}
			    			return;
			    		}
		    	}
	    	}
    	}
    	catch(Throwable t) {
    		t.printStackTrace();
    		parent.showErrorDialog("Error",t,null);
    	}
    }

    public void keyReleased(int keyCode,int gameActionCode) {
//    	Logger.logTrace("KeyReleased"+keyCode+", "+gameActionCode);
    	try {
	    	switch(gameActionCode) {
	    		case FullCanvas.UP:
	    		case FullCanvas.DOWN:
	    			thePlane.vertSpeed = 0;
	    			break;
	    		case FullCanvas.LEFT:
	    		case FullCanvas.RIGHT:
	    			thePlane.horizSpeed = 1*GameObject.SPEED_SCALE_FACTOR;
	    			break;
	    		}
	    }
    	catch(Throwable t) {
    		t.printStackTrace();
    		parent.showErrorDialog("Error",t,null);
    	}
	}

    public void pauseGame() {
    	gameState = STATE_PAUSED;
    	gameCanvas.setGamePaused(true);
    	gameCanvas.repaint();
    }

    public void continueGame() {
    	gameState = STATE_PLAYING;
    	gameCanvas.setGamePaused(false);
    }

    private void endGame() {
    	gameState = STATE_INTERRUPTED;
    }

    public void addBullet(GameObject bullet) {
    	bullets.addElement(bullet);
//    	SoundEffects.playEffect(SoundEffects.ENEMY_SHOOT);
    }

    public void shipDestroyed() {
    	updateScore(GameObject.TYPE_SHIP);
    	parent.shipDestroyed();
    }

    public void freeImages() {
    	GameObject.emptyImageCache();

    	gameCanvas.freeImages();
    	gameCanvas = null;
    	instance = null;


    	activeObjects.removeAllElements();
    	passiveObjects.removeAllElements();
    	ownBombs.removeAllElements();
    	bullets.removeAllElements();

    	thePlane = null;
  /*
    	objectsPropertyTable.clear();
    	objectsPropertyTable = null;
    */
    }

    /**
     * Check collisions between the objects
     * This method is called frequently, so keep it effective
     */
    private void checkForCollisions() {
    	Rectangle planeBounds = thePlane.bounds;
    	//Logger.logTrace("Plane bounds: top="+planeBounds.top+", bottom="+planeBounds.bottom+", left="+planeBounds.left+" ,right="+planeBounds.right);

    	Vector visibleActiveObject = gameCanvas.visibleActiveObjects;
    	Vector visiblePassiveObject = gameCanvas.visiblePassiveObjects;

    	// Check the temporary objects
//    	Logger.logTrace("Number of bombs and missiles: "+ownBombs.size());
    	for(int i=0;i<ownBombs.size();i++) {
    		GameObject actObj = (GameObject)ownBombs.elementAt(i);
    		if((!actObj.hasCollided())&&(actObj.xCoord<levelLength)) {
	    		int xCoord = actObj.xCoord;
	    		// Collisionw with sea or ground
	    		//Logger.logTrace("Check the Missile collisions, x="+xCoord);
	    		if((actObj.currentState==GameObject.INITIAL_STATE)&&(actObj.yCoord>=landHighLimit[xCoord])) {
	    			actObj.onCollision(COLLISION_GROUND);
//	    			SoundEffects.playEffect(SoundEffects.OWN_BOMB);
	    		}
	    		if((actObj.currentState==GameObject.INITIAL_STATE)&&(actObj.yCoord>=seaHeight)) {
	    			actObj.onCollision(COLLISION_SEA);
	    		}
	    		// Collisions with cannons etc.
	    		for(int j=0;j<visibleActiveObject.size();j++) {
	    			GameObject visibleObj = (GameObject)visibleActiveObject.elementAt(j);
	    			if((!visibleObj.hasCollided())&&(visibleObj.bounds.collidesWith(actObj.bounds))) {
	    				visibleObj.onCollision(COLLISION_BULLET);
	    				actObj.onCollision(COLLISION_ACTOBJ);
	    				updateScore(visibleObj.objType);
	    				SoundEffects.playEffect(SoundEffects.ENEMY_DESTRUCT);
	    				vibrate(50,100);
	    			}
	    		}
	    		// Collisions with objects that do not explode
	    		for(int j=0;j<visiblePassiveObject.size();j++) {
	    			GameObject visibleObj = (GameObject)visiblePassiveObject.elementAt(j);
	    			if(visibleObj.bounds.collidesWith(actObj.bounds)) {
	    				actObj.onCollision(COLLISION_PASSOBJ);
//	    				SoundEffects.playEffect(SoundEffects.OWN_BOMB);
	    			}
	    		}
    		}
    	}

    	if((!parent.cheatMode)&&(gameState==STATE_PLAYING)&&(!thePlane.hasCollided())) {
    		// Plane collisions with ground
    		for(int p=planeBounds.left;p<=planeBounds.right;p++) {
	    		if((planeBounds.top>landHighLimit[p])||(planeBounds.top>seaHeight)) {
	    			thePlane.onCollision(COLLISION_GROUND);
	    			planeDestroyed();
	    		}
	    	}
	    	// Plane collisions with active objects
	    	for(int j=0;j<visibleActiveObject.size();j++) {
	    		GameObject visibleObj = (GameObject)visibleActiveObject.elementAt(j);
	    		//Logger.logTrace("Active obejct bounds: top="+visibleObj.getBounds().top+", bottom="+visibleObj.getBounds().bottom+", left="+visibleObj.getBounds().left+" ,right="+visibleObj.getBounds().right);
	    		if((!visibleObj.hasCollided())&&(visibleObj.bounds.collidesWith(planeBounds))) {
	    			visibleObj.onCollision(COLLISION_ACTOBJ);
	    			thePlane.onCollision(COLLISION_ACTOBJ);
	    			planeDestroyed();
	    		}
	    	}
	    	// Plane collisions with passive objects
	    	for(int j=0;j<visiblePassiveObject.size();j++) {
	    		GameObject visibleObj = (GameObject)visiblePassiveObject.elementAt(j);
	    		//Logger.logTrace("Passive obejct bounds: top="+visibleObj.getBounds().top+", bottom="+visibleObj.getBounds().bottom+", left="+visibleObj.getBounds().left+" ,right="+visibleObj.getBounds().right);
	    		if(visibleObj.bounds.collidesWith(planeBounds)) {
	    			thePlane.onCollision(COLLISION_PASSOBJ);
	    			planeDestroyed();
	    		}
	    	}
	    	// Plane collisions with bullets
	    	for(int i=0;i<bullets.size();i++) {
	    		GameObject b = (GameObject)bullets.elementAt(i);
	    		if(b.collidesWith(planeBounds)) {
	    			thePlane.onCollision(COLLISION_BULLET);
	    			b.onCollision(COLLISION_PLANE);
	    			planeDestroyed();
	    		}
	    	}
	    }
    }

    private void planeDestroyed() {
    	gameState = STATE_GAMEOVER;
    	jumpingPilot = new GameObject(thePlane.xCoord,thePlane.yCoord,(byte)0,(byte)-5,GameObject.TYPE_PARACHUTE);
    	gameCanvas.jumpingPilot = jumpingPilot;
//    	System.out.println("Added jumping pilot");
    	SoundEffects.playEffect(SoundEffects.PLANE_DESTROYED);
    	vibrate(100,1000);
    }

    private void checkPlanePosition() {
    	Rectangle planeBounds = thePlane.bounds;
    	Rectangle visibleBounds = gameCanvas.visibleArea;
    	if((planeBounds.left<=visibleBounds.left)&&(thePlane.horizSpeed<PLANE_DEFAULT_SPEED*GameObject.SPEED_SCALE_FACTOR)) {
    		thePlane.horizSpeed = PLANE_DEFAULT_SPEED*GameObject.SPEED_SCALE_FACTOR;
    	}
    	else if((planeBounds.right>=visibleBounds.right)&&(thePlane.horizSpeed>PLANE_DEFAULT_SPEED)) {
    		thePlane.horizSpeed = PLANE_DEFAULT_SPEED*GameObject.SPEED_SCALE_FACTOR;
    	}

    	if((planeBounds.bottom<=1)&&(thePlane.vertSpeed<0)) {
    		thePlane.vertSpeed = 0;
    	}
    }

    private void updateScore(byte objectType) {
    	gameCanvas.updateScore(parent.addToScore(getScore(objectType)));
    }

    public int getScore(byte objectType) {
    	switch(objectType) {
    		case GameObject.TYPE_BOAT:
    			return 25;
    		case GameObject.TYPE_CANNON:
    			return 50;
    		case GameObject.TYPE_TANK:
    			return 75;
    		case GameObject.TYPE_SUBMARINE:
    			return 100;
    		case GameObject.TYPE_HELICOPTER:
    			return 200;
    		case GameObject.TYPE_MOVINGHELICOPTER:
    			return 200;
    		case GameObject.TYPE_SHIP:
    			return 200;
    	}

    	return 0;
    }

    private void vibrate(int freq, long duration) {
    	if(MainControl.instance.useVibration) {
    		try {
//    			Logger.logTrace("Vibrate");
    			DeviceControl.startVibra(freq,duration);
    		}
    		catch(Exception e) {
    			e.printStackTrace();
    		}
    	}
    }
}
