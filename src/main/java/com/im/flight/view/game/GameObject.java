/* This file was created by Nokia Developer's Suite for J2ME(TM) */

package com.im.flight.view.game;

import java.util.Hashtable;

import com.im.flight.control.GameControl;
import com.im.flight.control.MainControl;
import com.im.flight.view.menu.MenuScreen;
import com.im.util.*;
import com.nokia.mid.ui.DirectGraphics;
import com.nokia.mid.ui.DirectUtils;
import java.util.*;
import javax.microedition.lcdui.*;
import java.io.*;

public class GameObject
{
	// OBJECT MODES
	public static final byte MODE_ACTIVE = 0;
	public static final byte MODE_PASSIVE = 1;
	public static final byte MODE_TRANSPARENT = 2;
	public static final byte MODE_MAIN = 3;
	public static final byte MODE_BULLET = 4;

	// OBJECT TYPES
	public static final byte TYPE_CANVAS = -1;
	public static final byte TYPE_PLANE = 0;
	public static final byte TYPE_BOAT = 1;
	public static final byte TYPE_BOMB = 2;
	public static final byte TYPE_BUILDING = 3;
	public static final byte TYPE_CANNON = 4;
	public static final byte TYPE_HELICOPTER = 5;
	public static final byte TYPE_LIGHTHOUSE = 6;
	public static final byte TYPE_MISSILE = 7;
	public static final byte TYPE_MOVINGHELICOPTER = 8;
	public static final byte TYPE_SHIP = 9;
	public static final byte TYPE_SUBMARINE = 10;
	public static final byte TYPE_TANK = 11;
	public static final byte TYPE_TREE = 12;
	public static final byte TYPE_SMALL_CLOUD = 13;
	public static final byte TYPE_MEDIUM_CLOUD = 14;
	public static final byte TYPE_BIG_CLOUD = 15;
	public static final byte TYPE_BUSH = 16;
//	public static final byte TYPE_TRANSPARENT = 17;
	public static final byte TYPE_BULLET = 18;
//	public static final byte TYPE_GRASS = 19;
//	public static final byte TYPE_SMALL_ROCK = 20;
//	public static final byte TYPE_BIG_ROCK = 21;
	public static final byte TYPE_PARACHUTE = 22;

	// MISSILE CONSTANTS
	public static final byte MISSILE_UP = 0;
	public static final byte MISSILE_RIGHT = 1;
	public static final byte MISSILE_ACCEL_FACTOR = 5;
	public static final byte MISSILE_MAX_SPEED = 60;
	public static final byte MISSILE_START_SPEED = 0;

	// BOMB CONSTANTS
	public static final byte BOMB_SPLASH_IMAGE_COUNT = 5;
	public static final String BOMB_SPLASH_IMAGE_NAME = "bs";
	public static final byte BOMB_MAX_DROP_SPEED = 30;
	public static final byte BOMB_START_DROP_SPEED = 10;

	// PLANE CONSTANTS
	public static final int PLANE_STARTPOS_X = 20;
	public static final byte PLANE_STARTPOS_Y = 20;
	public static final byte WAVE_HAND_IMAGE_COUNT = 2;
	public static final String WAVE_HAND_IMAGE_NAME = "hand";

	// PARACHUTE CONSTANTS
	public static final int PARACHUTE_OPEN_ALTITUDE = 50;
	public static final int PARACHUTE_SINK_SPEED = 10;
	public static final int FREE_FALL_MAX_SPEED = 30;


	// OBJECT STATES
	public static final byte SPEED_SCALE_FACTOR = 10;
	public static final byte GRAVITY_FACTOR = 3;
	public static final byte INITIAL_STATE = 1;
	public static final byte DESTRUCTION_STATE = 2;
	public static final byte FINAL_STATE = 3;
	public static final int SCORE_DELAY = 2000;

	// IMAGE CACHE
	private static Hashtable imageCache = new Hashtable();
	private static String[] imagesToCleanUp = {"bo1","l","s1","su1","ss1","ss2","ss3","ss4","t1"};
	public static String[] imagesToPreload = {"ae1","ae2","ae3","ae4","b1","bc","bs1","bs2","bs3","bs4","bs5","bu1","bub1","bush","but1","c1","cd1","cl","e1","e2","e3","e4","h1","m1","m2","p1","p2","sc","se1","se2","se3","se4","tb1","tr1","tt1","w","ff1","po1","pa1"};

	public Rectangle bounds = new Rectangle();
	protected ObjectProperties objectProperties;

	// Images (animation) that is shown before a collision
	protected Image[] startImages;
	// Destruction animation
	protected Image[] destructionImages;
	// Images (animation) after the collision
	protected Image[] finalImages;
	// Amount of frames before image change;
//	protected byte imageLifetime;
	protected byte imageLifeCounter;
	// Set this attribute true, if this object does not exist and shouldn't be drawn anymore
	public boolean markedForDeleting = false;
	// Time of collision
//	protected long collisionTime;

	// General object attributes
	public byte vertSpeed = 0;
	public byte horizSpeed = 0;
	protected byte imageIndex = 0;
	public byte currentState = INITIAL_STATE;
	protected Image currentImage;
	public int xCoord;
    public int yCoord;
    public byte width;
    public byte height;
    public byte objType;
    protected Image[] additionalAnimation;
    private boolean splash = false;

    // Missile
    public byte missileDirection;



    public GameObject() {
    }

    /** Creates a new instance of GameObject */
    public GameObject(int x, int y, byte xSpeed, byte ySpeed, byte objectType) {
    	objectProperties = ObjectProperties.getObjectProperties(objectType);
    	horizSpeed = (byte)(xSpeed*SPEED_SCALE_FACTOR);
    	vertSpeed = (byte)(ySpeed*SPEED_SCALE_FACTOR);
    	initObject(x,y,objectType);
    	initImages();

//    	System.out.println("Game object type "+objType+" created, vertSpd = "+vertSpeed);
    }

// -------------- COMMON METHODS  -------------------------------------
    public void initObject(int x, int y, byte type) {
    	this.xCoord = x;
    	this.yCoord = y;
    	this.objType = type;
    }

    private void initImages() {
    	if(objType != TYPE_BULLET) {
	    	if((objectProperties.objectMode == MODE_PASSIVE) ||
	    	   (objectProperties.objectMode == MODE_TRANSPARENT)) {
	    		String imageName = objectProperties.startimageName;
	    		currentImage = getImage(imageName);
	    	}
	    	else {
	    		// Bomb splash
	    		if(objType == TYPE_BOMB) {
		    		additionalAnimation= new Image[BOMB_SPLASH_IMAGE_COUNT];
		    		for(int i=0;i<BOMB_SPLASH_IMAGE_COUNT;i++) {
		    			String splashImageName = BOMB_SPLASH_IMAGE_NAME+(i+1);
		    			additionalAnimation[i] = getImage(splashImageName);
		    		}
	    		}
	    		else if(objType == TYPE_PLANE) {
	    			additionalAnimation= new Image[WAVE_HAND_IMAGE_COUNT];
	    			for(int i=0;i<WAVE_HAND_IMAGE_COUNT;i++) {
	    				String splashImageName = WAVE_HAND_IMAGE_NAME+(i+1);
	    				additionalAnimation[i] = getImage(splashImageName);
	    			}
	    		}


	  // ------ STANDARD ANIMATION ----------------------------------------------
//		    	imageLifetime = objectProperties.imageLifetime;

		    	int startImageCount = objectProperties.startimageCount;
		    	startImages = new Image[startImageCount];
		    	for(int i=0;i<startImageCount;i++) {
		    		String startImageName = objectProperties.startimageName+(i+1);
		    		startImages[i] = getImage(startImageName);
		    	}

		    	int destructImageCount = objectProperties.destructimageCount;
		    	destructionImages = new Image[destructImageCount];
		    	for(int i=0;i<destructImageCount;i++) {
		    		String desctructImageName = objectProperties.destructimageName+(i+1);
		    		destructionImages[i] = getImage(desctructImageName);
		    	}

		    	int finalImageCount = objectProperties.finalimageCount;
		    	finalImages = new Image[finalImageCount];
		    	for(int i=0;i<finalImageCount;i++) {
		    		String finalImageName = objectProperties.finalimageName+(i+1);
		    		finalImages[i] = getImage(finalImageName);
		    	}

		    	currentImage = startImages[0];
	    	}
		    width = (byte)currentImage.getWidth();
		    height = (byte)currentImage.getHeight();
    	}

    	refreshBounds();
    }


    protected Image[] getImageStore() {
    	Image[] imageStore = null;
    	switch(currentState) {
    		case INITIAL_STATE:
    			imageStore = startImages;
    			break;
    		case DESTRUCTION_STATE:
    			if(objType == TYPE_BOMB && splash) {
    				return additionalAnimation;
    			}
    			else {
    				imageStore = destructionImages;
    			}
    			break;
    		case FINAL_STATE:
    			imageStore = finalImages;
    			break;
    	}
    	return imageStore;
    }

    /**
     * Get an image by name. First checks the cache (which should be populated
     * by Starter.java preloading). If not found, tries to load via
     * Image.createImage as a fallback.
     */
    public static Image getImage(String name) {

    	Image img = (Image)imageCache.get(name);

     	if(img == null) {
     		// Fallback: try to load the image via the compat layer
     		// In the browser port, resources should have been preloaded by Starter.java
	    	String imageResourceName = "/res/"+name;
	    	System.out.println("Getting image "+imageResourceName+" (cache miss - trying fallback)");

	    	try {
	    		// First try as a PNG image via Image.createImage
	    		img = Image.createImage(imageResourceName);
	    	} catch(Throwable t1) {
	    		// If that fails, try loading as raw bytes via getResourceAsStream + DirectUtils
	    		try {
	    			// In browser port, use Image's resource cache (preloaded via XHR)
			    	InputStream inputStream = Image.getResourceAsStream(imageResourceName);
			    	if (inputStream == null) {
			    		// Fallback to classpath
			    		inputStream = name.getClass().getResourceAsStream(imageResourceName);
			    	}
			    	if (inputStream != null) {
				    	DataInputStream dataInput = new DataInputStream(inputStream);

				    	Vector imageData = new Vector();
				    	try {
					    	while(true) {
					    		imageData.addElement(new Byte(dataInput.readByte()));
					    	}
				    	}
				    	catch(EOFException e) {
				    	}
				    	catch(IOException ioe) {
				    		ioe.printStackTrace();
				    	}

				    	byte[] data = new byte[imageData.size()];
				    	for(int i=0;i<data.length;i++) {
				    		data[i] = ((Byte)imageData.elementAt(i)).byteValue();
				    	}

				    	img = DirectUtils.createImage(data,0,data.length);
			    	} else {
			    		System.out.println("WARNING: Could not load image: " + imageResourceName);
			    	}
	    		} catch(Throwable t2) {
	    			System.out.println("ERROR: Failed to load image " + imageResourceName + ": " + t2);
	    		}
	    	}

	    	if (img != null) {
	    		imageCache.put(name,img);
	    	}
	    }

    	return img;
    }

    /**
     * Cache an image under the given name. Called by Starter.java during preloading.
     */
    public static void cacheImage(String name, Image img) {
    	if (name != null && img != null) {
    		imageCache.put(name, img);
    	}
    }

    public static void emptyImageCache() {
    	// Do not clean up images needed in all levels
    	for(int i=0;i<imagesToCleanUp.length;i++) {
    		imageCache.remove(imagesToCleanUp[i]);
    	}
    }

/*
    public static void preLoadImages() {
    	for(int i=0;i<imagesToPreload.length;i++) {
//    		System.out.println("Loading "+imagesToPreload[i]+".png");
    		getImage(imagesToPreload[i]);
    		MainControl.instance.progressLoading();
    	}
    }
*/
    protected void onAnimationEnd() {
    	if(currentState == DESTRUCTION_STATE) {
    		currentState = FINAL_STATE;
    	}
    }
    /*
     * This method is called when this object has collided
     */
    public void onCollision(byte collisionType) {

    	vertSpeed = 0;
    	horizSpeed = 0;

    	if(currentState == INITIAL_STATE) {
    		imageIndex = 0;
    		currentState = DESTRUCTION_STATE;
//    		collisionTime = System.currentTimeMillis();
//    		refreshBounds();
    	}

    	if(collisionType == GameControl.COLLISION_SEA) {
    		splash = true;
    	}

    	if(objType == TYPE_BULLET) {
    		markedForDeleting = true;
    	}

    }

    public boolean collidesWith(Rectangle rect) {
    	return rect.isInside(xCoord,yCoord);
    }

    public boolean hasCollided() {
    	if(currentState == INITIAL_STATE) {
    		return false;
    	}
    	else {
    		switch(objType) {
    			case TYPE_BUILDING:
    			case TYPE_TREE:
    				return false;
    		}
    		return true;
    	}
    }

// ------------- METHODS FOR DIFFERENT OBJECT TYPES -------------------------

    private Rectangle refreshBounds() {
    	switch(objType) {
    		case TYPE_SMALL_CLOUD:
    		case TYPE_MEDIUM_CLOUD:
    		case TYPE_BIG_CLOUD:
//    		case TYPE_SMALL_ROCK:
//    		case TYPE_BIG_ROCK:
//    		case TYPE_GRASS:
    		case TYPE_BUSH:
    			bounds.bottom = yCoord-height;
    			bounds.left = xCoord;
    			bounds.right = xCoord+width;
    			bounds.top = yCoord;
    			break;
    		case TYPE_PLANE:
    			bounds.left = xCoord-width/2;
    			bounds.bottom = yCoord-height/2;
    			bounds.right = xCoord+width/2;
    			bounds.top = yCoord+height/2;
    			break;
    		default:
    		// passive object
    			bounds.left = xCoord-width/2;
    			bounds.bottom = yCoord-height;
    			bounds.right = xCoord+width/2;
    			bounds.top = yCoord;
    	}

    	return bounds;
    }

    public int getImageAnchorPoint() {
    	switch(objType) {
    		case TYPE_SMALL_CLOUD:
    		case TYPE_MEDIUM_CLOUD:
    		case TYPE_BIG_CLOUD:
    			return Graphics.LEFT|Graphics.BOTTOM;
    		case TYPE_PLANE:
    			return Graphics.HCENTER|Graphics.VCENTER;
    		default:
    			return Graphics.HCENTER|Graphics.BOTTOM;
    	}
    }

    public void paint(Graphics graphics,int xOffset, int yOffset) {
    	// Exploding plane
    	if(objType == TYPE_BULLET) {
    		int x=xCoord-xOffset;
    		int y=yCoord-yOffset;
    		graphics.setColor(0);
    		graphics.drawLine(x-1,y,x+1,y);
    		graphics.drawLine(x,y-1,x,y+1);
    	}
    	else if(objType == TYPE_PLANE && currentState==DESTRUCTION_STATE) {
    		graphics.drawImage(startImages[0],xCoord-xOffset,yCoord-yOffset,getImageAnchorPoint());
    		graphics.drawImage(currentImage,xCoord-xOffset,yCoord-yOffset,getImageAnchorPoint());
    	}
    	// Missile from submarine
    	else if(objType == TYPE_MISSILE && missileDirection == MISSILE_UP && currentState==INITIAL_STATE) {
    		DirectGraphics dg = DirectUtils.getDirectGraphics(graphics);
    		dg.drawImage(currentImage,xCoord-xOffset,yCoord-yOffset,getImageAnchorPoint(),DirectGraphics.ROTATE_90);
    	}
    	// Other cases
    	else if(currentImage!=null) {
    		graphics.drawImage(currentImage,xCoord-xOffset,yCoord-yOffset,getImageAnchorPoint());
    	}

    	if(objType == TYPE_PLANE && GameControl.instance.gameEnding) {
    		graphics.drawImage(additionalAnimation[imageIndex],xCoord-xOffset+5,yCoord-yOffset,Graphics.BOTTOM|Graphics.LEFT);
    	}
    }

    public void advance() {

//    	System.out.println("Game object type "+objType+" advancing, vertSpd = "+vertSpeed);

    	xCoord = xCoord+horizSpeed/SPEED_SCALE_FACTOR;
    	yCoord = yCoord+vertSpeed/SPEED_SCALE_FACTOR;

    	if((objectProperties.objectMode != MODE_PASSIVE) &&
    	   (objectProperties.objectMode != MODE_TRANSPARENT) &&
    	   (objType != TYPE_BULLET)) {

    		if(currentState == FINAL_STATE) {
    			markedForDeleting = true;
    		}

    		switch(objType) {
    			case TYPE_MISSILE:
    				if(currentState == INITIAL_STATE) {
    					if(missileDirection == MISSILE_RIGHT && horizSpeed<MISSILE_MAX_SPEED) {
    						horizSpeed = (byte)(horizSpeed+MISSILE_ACCEL_FACTOR);
    					}
    					else if(missileDirection == MISSILE_UP && (-1*vertSpeed)<MISSILE_MAX_SPEED) {
    						vertSpeed = (byte)(vertSpeed-MISSILE_ACCEL_FACTOR);
    					}
    				}
    				break;
    			case TYPE_BOMB:
    				if(currentState == INITIAL_STATE) {
    					if(vertSpeed<BOMB_MAX_DROP_SPEED) {
    						vertSpeed = (byte)(vertSpeed+GRAVITY_FACTOR);
    					}
    				}
    				break;
    			case TYPE_PARACHUTE:
//    				System.out.println("Parachute at "+xCoord+","+yCoord);
    				if(currentState == INITIAL_STATE) {
    					if(vertSpeed<FREE_FALL_MAX_SPEED) {
    						vertSpeed = (byte)(vertSpeed+GRAVITY_FACTOR);
    					}
    					if((yCoord>PARACHUTE_OPEN_ALTITUDE)&&(vertSpeed>0)) {
    						currentState = DESTRUCTION_STATE;
    						vertSpeed = PARACHUTE_SINK_SPEED;
    					}
    				}
    				break;
    		}

// --------- STANDARD STUFF FOR ANIMATION ----------------------------------
    		Image[] imageStore = getImageStore();

	    	imageLifeCounter++;
	    	// Switch to next image
	    	if(imageLifeCounter == objectProperties.imageLifetime) {
	    		imageIndex++;
	    		imageLifeCounter = 0;
	    	}

	    	if(imageIndex>=imageStore.length) {
	    		imageIndex=0;
	    		// After the destrucion state, automatically move to final state
	    		onAnimationEnd();
	    		imageStore = getImageStore();
	    	}

	    	if(imageStore.length>0) {
	    		currentImage = imageStore[imageIndex];
	    		width = (byte)currentImage.getWidth();
	    		height = (byte)currentImage.getHeight();
	    	}
	    	else {
	    		currentImage = null;
	    		width = 0;
	    		height = 0;
	    	}
	    }

    	refreshBounds();
    }

}
