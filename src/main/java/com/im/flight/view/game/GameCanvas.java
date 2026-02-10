/* This file was created by Nokia Developer's Suite for J2ME(TM) */

package com.im.flight.view.game;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import com.nokia.mid.ui.*;
import javax.microedition.lcdui.*;

import com.im.flight.control.GameControl;
import com.im.flight.control.MainControl;
import com.im.flight.view.menu.FullScreenInfo;
import com.im.util.*;

public class GameCanvas extends FullScreenInfo{

	// How many pixels before becoming visible the objects are activated
	public static final int OBJECT_ACTIVATION_DISTANCE= 45;

//	public static final int SKY_COLOUR = 0x4F91DE;

	public static final int SKY_COLOUR = 0xAACCFF;

//	private static final Image skyStrip = GameObject.getImage("sky.png");
	private static final Image wave = GameObject.getImage("w");
	private static final Image landscape = GameObject.getImage("terrain.png");
	private static final Image fortress = GameObject.getImage("fortress.png");
	private static final Image bush = GameObject.getImage("bush");
	private static Image scoreTable;

	public static final int WATER_COLOUR = 0x4000C0;
	public static final int GROUND_COLOUR = 0xE0E080;
//	public static final int GRASS_COLOUR_1 = 0x009900;
//	public static final int GRASS_COLOUR_2 = 0x99CC00;
//	public static final int GRASS_COLOUR_3 = 0xCCCC00;
//	public static final int GRASS_COLOUR_4 = 0xCCFF00;

//	public static final int SCORE_COLOUR = SKY_COLOUR;
	public static final int SCORE_COLOUR = 0xFFFFFF;
//	public static final int WATER_COLOUR_INCREMENT = 0x040404;
//	public static final int SKY_COLOUR_INCREMENT = 0x070400;

	private static final int NUMBER_OF_SMALL_CLOUDS = 20;
	private static final int NUMBER_OF_MEDIUM_CLOUDS = 20;
	private static final int NUMBER_OF_BIG_CLOUDS = 20;
	private static final int NUMBER_OF_BUSHES = 2000;
/*
	private static final int NUMBER_OF_GRASS = 0;
	private static final int NUMBER_OF_SMALL_ROCKS = 1000;
	private static final int NUMBER_OF_BIG_ROCKS = 1000;
*/
	// Width of a single landscapa imege
	private static final int IMAGE_WIDTH = 64;

    public  Rectangle visibleArea;
    private int visibleWidth;
    private int visibleHeight;
    public int screenSpeed = 1;
    private int levelLength;
    private byte seaHeight;
    private byte[] landHighLimit;
    // Storage for landscape images;
    private Vector landscapeImages = new Vector();
    // Locations of the landscape images
    private Vector landscapeImageLocations = new Vector();
    private int imageCount;
    private Vector passiveObjects;
    private Vector activeObjects;
    private Vector ownBombs;
    private Vector bullets;
    public Vector visibleActiveObjects = new Vector();
    public Vector activeActiveObjects = new Vector();
    public Vector visiblePassiveObjects = new Vector();
    private Vector clouds = new Vector();
    private Vector bushes = new Vector();
/*
    private Vector rocks = new Vector();
    private Vector grass = new Vector();
*/
    // The main object of the game
    private GameObject mainObject;
    // The main object of the game
    public GameObject jumpingPilot;
    // First passive object to print off-screen
    private int firstPassiveObject = 0;
    // Last passive object to print off-screen
    private int lastPassiveObject = 0;
    // First active object to print off-screen
    private int firstActiveObject = 0;
    // Last active object to print off-screen
    private int lastVisibleActiveObject = 0;
    // Last active object that is currently active
    private int lastActiveActiveObject = 0;
    // Last cloud to print off-screen
    private int lastCloud = 0;
    // Last bush to print off-screen
    private int lastBush = 0;
    // Last rock to print off-screen
    //private int lastRock = 0;
    // Last grass to print off-screen
    //private int lastGrass = 0;
    // Action listener for keyboard actions
    private int landscapeIndex = 1;
    private Image[] landscapeStrips;

    private GameControl keybListener;
    private boolean gamePaused = false;
    private Random randomGen = new Random();

    public GameCanvas(byte sea,byte[] landHigh, Vector passObjects, Vector actObjects, Vector ownObjects, Vector attackObjects, GameObject mainObj) {
//    	super(null,0);

    	levelLength = landHigh.length;
        seaHeight = sea;
        landHighLimit = landHigh;
        passiveObjects = passObjects;
        activeObjects = actObjects;
        ownBombs = ownObjects;
        bullets = attackObjects;
        mainObject = mainObj;
        initialiseLandscapeStrip();
        createScoreTable();
        createClouds();
        initialiseCanvas();
    }

    private void initialiseLandscapeStrip() {
    	landscapeStrips = new Image[(levelLength/8)+9];
    	for(int i=0;i<landscapeStrips.length;i++) {
    		if((MainControl.instance.level == 3) && (
    				(i>22 && i<31) ||
    				(i>68 && i<74) ||
    				(i>98 && i<105) ||
    				(i>195 && i<201)))
    		{
    			landscapeStrips[i] = fortress;
    		}
    		else if((MainControl.instance.level == 4) && (
    				(i>21 && i<54) ||
    				(i>144 && i<146))) {

    			landscapeStrips[i] = fortress;
    		}
    		else {
    			landscapeStrips[i] = landscape;
    		}
    	}

    }

    private void initialiseCanvas() {
    	visibleWidth = getWidth();
        visibleHeight = getHeight();
        visibleArea = new Rectangle();
        visibleArea.left = 0;
        visibleArea.right = visibleWidth;
        visibleArea.bottom = 0;
        visibleArea.top = visibleHeight;

        imageCount = (visibleWidth/(IMAGE_WIDTH+1))+2;
        for(int i=0;i<imageCount;i++) {
        	landscapeImages.addElement(createLandscapeImage(i*IMAGE_WIDTH));
        	landscapeImageLocations.addElement(new Integer(i*IMAGE_WIDTH));
        }
    }

    private void createScoreTable() {
/*
    	Image immutableScoreTable = GameObject.getImage("score.png");
    	scoreTable = Image.createImage(immutableScoreTable.getWidth(), immutableScoreTable.getHeight());
    	Graphics g = scoreTable.getGraphics();
    	g.drawImage(immutableScoreTable, 0, 0, Graphics.TOP|Graphics.LEFT);
  */

    	scoreTable = Image.createImage(128,10);
    	updateScore(0);
    }

    private void createClouds() {
    	addClouds(GameObject.TYPE_SMALL_CLOUD,NUMBER_OF_SMALL_CLOUDS);
    	addClouds(GameObject.TYPE_MEDIUM_CLOUD,NUMBER_OF_MEDIUM_CLOUDS);
    	addClouds(GameObject.TYPE_BIG_CLOUD,NUMBER_OF_BIG_CLOUDS);
    	addBushes(GameObject.TYPE_BUSH,NUMBER_OF_BUSHES);
/*
    	addBushes(GameObject.TYPE_BIG_ROCK,NUMBER_OF_BIG_ROCKS,rocks);
    	addBushes(GameObject.TYPE_SMALL_ROCK,NUMBER_OF_SMALL_ROCKS,rocks);
    	addBushes(GameObject.TYPE_GRASS,NUMBER_OF_GRASS,grass);
 */
 /*
    	Logger.logTrace("Clouds:");
    	for(int i=0;i<clouds.size();i++) {
    		PassiveObject cloud = (PassiveObject)clouds.elementAt(i);
    		Logger.logTrace(cloud.xCoord+", "+cloud.yCoord);
    	}
*/
    }

    private void addClouds(byte type, int number){
    	for(int i=0;i<number;i++) {
    		int xCoord = Math.abs(randomGen.nextInt()/(Integer.MAX_VALUE/levelLength));
    		int yCoord = Math.abs(randomGen.nextInt()/(Integer.MAX_VALUE/(seaHeight-10)));
    		GameObject newObject = new GameObject(xCoord,yCoord,(byte)0,(byte)0,type);
//    		System.out.println("New cloud, x:"+xCoord+" y:"+yCoord);

    		Rectangle objectBounds = newObject.bounds;
//    		System.out.println("New bush, x:"+xCoord+" y:"+yCoord+" Bounds: "+objectBounds.left+"->"+objectBounds.right);
    		if(objectBounds.right<levelLength && (objectBounds.top<landHighLimit[objectBounds.left] && objectBounds.top<landHighLimit[objectBounds.right]) && (objectBounds.top<landHighLimit[(objectBounds.left+objectBounds.right)/2])) {
//    			System.out.println("Adding bush "+xCoord+","+yCoord);
    			addToOrderedObjectsVector(newObject,clouds);
    		}
    	}
    }


    private void addBushes(byte type, int number){
    	for(int i=0;i<number;i++) {
    		int xCoord = Math.abs(randomGen.nextInt()/(Integer.MAX_VALUE/levelLength));
    		int yCoord = Math.abs(randomGen.nextInt()/(Integer.MAX_VALUE/(seaHeight-4)));
//    		GameObject newObject = new GameObject(xCoord,yCoord,(byte)0,(byte)0,type);

    		Rectangle objectBounds = new Rectangle(xCoord,yCoord,xCoord+bush.getWidth(),yCoord-bush.getHeight());
//    		System.out.println("New bush, x:"+xCoord+" y:"+yCoord+" Bounds: "+objectBounds.left+"->"+objectBounds.right);
    		if(objectBounds.right<levelLength && (objectBounds.bottom>landHighLimit[objectBounds.left] && objectBounds.bottom>landHighLimit[objectBounds.right]) && landscapeStrips[objectBounds.left/8]==landscape && landscapeStrips[objectBounds.right/8]==landscape) {
//    			System.out.println("Adding bush "+xCoord+","+yCoord);
    			int objectIndex = 0;

    			if(!bushes.isEmpty()) {
    				Rectangle nextX = (Rectangle)bushes.elementAt(0);
    				while((objectIndex < bushes.size()-1) && (nextX.left<xCoord)) {
    					objectIndex++;
    					nextX= (Rectangle)bushes.elementAt(objectIndex);
    				}

    			}
    			bushes.insertElementAt(objectBounds,objectIndex);
//    			System.out.println("Added bush at  "+objectBounds.left+","+objectBounds.bottom+", sea height = "+seaHeight);
    		}
    	}
    }

    private void addToOrderedObjectsVector(GameObject newObject,Vector orderedVector) {
    	int objectIndex = 0;
    	if(!orderedVector.isEmpty()) {
    		GameObject nextObject = (GameObject)orderedVector.elementAt(0);
    		while((objectIndex < orderedVector.size()-1) && (nextObject.xCoord<newObject.xCoord)) {
    			objectIndex++;
    			nextObject = (GameObject)orderedVector.elementAt(objectIndex);
    		}

    	}
    	orderedVector.insertElementAt(newObject,objectIndex);

    }

    public void updateScore(int score) {
    	Graphics g = scoreTable.getGraphics();
    	g.setColor(WATER_COLOUR);
//  	g.fillRect(2,2,scoreTable.getWidth()-4,scoreTable.getHeight()-4);
    	g.fillRect(0,0,scoreTable.getWidth(),scoreTable.getHeight());
    	g.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_BOLD,Font.SIZE_SMALL));
    	g.setColor(SCORE_COLOUR);
    	String scoreText = TextResources.INFO_SCORE+":00000";
    	String scoreNumber = Integer.toString(score);
    	g.drawString(scoreText.substring(0,11-scoreNumber.length())+scoreNumber+"       "+TextResources.INFO_LEVEL+":"+GameControl.instance.level,2,1,Graphics.TOP|Graphics.LEFT);
    }

    private Image createLandscapeImage(int imagePosition) {

    	// Is the landscape grass or tiles?



//    	System.out.println("Creating landscape image "+landscapeIndex+" at: "+imagePosition+" tiles: "+isTiles);
    	Image img = Image.createImage(IMAGE_WIDTH,visibleHeight);
    	int drawLength = Math.min(IMAGE_WIDTH,(levelLength-imagePosition-1));

    	Graphics graphics = img.getGraphics();

/*
    	graphics.setColor(SKY_COLOUR);
    	graphics.fillRect(0,0,IMAGE_WIDTH,visibleHeight);
*/


    	// Draw the landscape base
    	for(int i=0;i<8;i++) {
    		graphics.drawImage(landscapeStrips[landscapeIndex],i*8,seaHeight,Graphics.LEFT|Graphics.BOTTOM);
    		landscapeIndex++;
    	}


    	// Draw the sky
    	graphics.setColor(SKY_COLOUR);
    	for(int i=0;i<drawLength;i++) {
 			graphics.drawLine(i,landHighLimit[imagePosition+i]-1,i,0);
    	}

    	// Draw the sea
    	graphics.setColor(WATER_COLOUR);
    	graphics.fillRect(0,seaHeight+1,IMAGE_WIDTH,visibleHeight-seaHeight-1);
    	/*
    	 int waterColor = WATER_COLOUR;
    	 for(int i=visibleHeight;i>seaHeight;i--) {
    	 waterColor = waterColor+WATER_COLOUR_INCREMENT;
    	 graphics.setColor(waterColor);
    	 graphics.drawLine(0,i,drawLength,i);
    	 }
    	 */

    	int waterMaskWidth = wave.getWidth();
    	for(int i=0;i<drawLength-8;i=i+waterMaskWidth) {
    		if(landHighLimit[i+imagePosition+8]>seaHeight) {
    			graphics.drawImage(wave,i,seaHeight-2,Graphics.LEFT|Graphics.TOP);
    		}
    	}
     	// Draw the clouds
    	lastCloud = printVisibleObjects(clouds,null,lastCloud,imagePosition,graphics);

/*
    	int skyStripWidth = skyStrip.getWidth();
    	for(int i=0;i<drawLength;i=i+skyStripWidth) {
    		graphics.drawImage(skyStrip,i,0,Graphics.LEFT|Graphics.TOP);
    	}
*/

    	// Draw the landscape

//    	Logger.logTrace("DrawLength: "+drawLength);

    	// Print the landscape extras
    /*
    	lastRock = printVisibleObjects(rocks,null,lastRock,imagePosition,graphics);
    	lastGrass= printVisibleObjects(grass,null,lastGrass,imagePosition,graphics);
    	*/
    	lastBush = printImages(bush,bushes,lastBush,imagePosition,graphics);

    	// Print the passive objects
    	lastPassiveObject = printVisibleObjects(passiveObjects,visiblePassiveObjects,lastPassiveObject,imagePosition,graphics);

    	return img;
    }

    /**
     * Draw objects to an offscreen image. Also update the array of currently visible objects if needed
     *
     * @param allObjects All objects of this levele
     * @param allVisibleObjects Array of currently visible objects, will be updated if != null
     * @param firstObjectToDraw Index of the first object that will be drawn
     * @param visibleStart x-coordinate of  the beginning of the offscreen image
     * @param graphics
     * @return Returns the index of the first object to be drawn to next offscreen image
     */
    private int printVisibleObjects(Vector allObjects, Vector allVisibleObjects, int firstObjectToDraw, int visibleStart, Graphics graphics) {
//    	Logger.logTrace("Last passive object index: "+lastPassiveObject);
    	if(allObjects.size()>0) {
    		int objectIndex = firstObjectToDraw;
    		GameObject gameObj = (GameObject)allObjects.elementAt(objectIndex);
    		while(gameObj.bounds.left<(visibleStart+IMAGE_WIDTH)) {
//	    		Logger.logTrace("Painting passive object at index: "+lastPassiveObject+", image pos: "+imagePosition+", object pos: "+passObj.xCoord);
    			gameObj.paint(graphics,visibleStart,0);
    			if((allVisibleObjects != null) && (!allVisibleObjects.contains(gameObj))) {
    				allVisibleObjects.addElement(gameObj);
    			}
//	    		Logger.logTrace("Passive object left: "+passObj.getBounds().left+"Passive object right: "+passObj.getBounds().right);
    			if(objectIndex<(allObjects.size()-1)) {
    				if(gameObj.bounds.right<(visibleStart+IMAGE_WIDTH)) {
//	    				Logger.logTrace("Moving to next passive object");
    					firstObjectToDraw++;
    				}
    				objectIndex++;
    				gameObj = (GameObject)allObjects.elementAt(objectIndex);
    			}
    			else {
    				break;
    			}
    		}
    	}
    	return firstObjectToDraw;
    }

    private int printImages(Image image, Vector imageBoundVector, int firstObjectToDraw, int visibleStart, Graphics graphics) {
//    	Logger.logTrace("Last passive object index: "+lastPassiveObject);
    	if(imageBoundVector.size()>0) {
    		int objectIndex = firstObjectToDraw;
    		Rectangle gameObj = (Rectangle)imageBoundVector.elementAt(objectIndex);
    		while(gameObj.left<(visibleStart+IMAGE_WIDTH)) {
//	    		Logger.logTrace("Painting passive object at index: "+lastPassiveObject+", image pos: "+imagePosition+", object pos: "+passObj.xCoord);
//    			System.out.println("Painting bush number "+objectIndex+" at "+gameObj.bottom);
    			graphics.drawImage(image,gameObj.left-visibleStart,gameObj.bottom,Graphics.LEFT|Graphics.TOP);

//	    		Logger.logTrace("Passive object left: "+passObj.getBounds().left+"Passive object right: "+passObj.getBounds().right);
    			if(objectIndex<(imageBoundVector.size()-1)) {
    				if(gameObj.right<(visibleStart+IMAGE_WIDTH)) {
//	    				Logger.logTrace("Moving to next passive object");
    					firstObjectToDraw++;
    				}
    				objectIndex++;
    				gameObj = (Rectangle)imageBoundVector.elementAt(objectIndex);
    			}
    			else {
    				break;
    			}
    		}
    	}
    	return firstObjectToDraw;
    }


    public void advance() {
    	refreshVisibleActiveObjects();
    	visibleArea.left = visibleArea.left+screenSpeed;
    	visibleArea.right= visibleArea.right+screenSpeed;

    	Integer firstImgPosInt = (Integer)landscapeImageLocations.elementAt(0);
    	int firstImgPos = firstImgPosInt.intValue();

    	if((firstImgPos+IMAGE_WIDTH)<visibleArea.left) {
//    		Logger.logTrace("Removing first image");
    		int lastImagePos = ((Integer)landscapeImageLocations.elementAt(imageCount-1)).intValue();
    		removeFirstImage();
//    		Logger.logTrace("Adding a new image at "+(lastImagePos+imageWidth));
    		landscapeImages.addElement(createLandscapeImage(lastImagePos+IMAGE_WIDTH));
    		landscapeImageLocations.addElement(new Integer(lastImagePos+IMAGE_WIDTH));

//    		Logger.logTrace("Image array size = "+landscapeImages.size());
//    		Logger.logTrace("Location array size = "+landscapeImageLocations.size());
    	}
//    	System.out.println("Visible p/a:"+visiblePassiveObjects.size()+"/"+visibleActiveObjects.size()+", Own bullets:"+ownBombs.size()+", Enemy bullets:"+bullets.size());
    }

    public void paint(Graphics g) {
    	try {
	    	// Draw the off-screen image

     		for(int i=0;i<imageCount;i++) {
     			Image img = (Image)landscapeImages.elementAt(i);
     			int imagePos = ((Integer)landscapeImageLocations.elementAt(i)).intValue();
     			g.drawImage(img,imagePos-visibleArea.left,0,Graphics.TOP|Graphics.LEFT);
     		}

     		// Draw the main object
     		mainObject.paint(g,visibleArea.left,0);

    		// Draw the own bombs
    		for(int i=0;i<ownBombs.size();i++) {
    			GameObject obj = (GameObject)ownBombs.elementAt(i);
    			obj.paint(g,visibleArea.left,0);
    		}

    		// Draw the bullets
    		for(int i=0;i<bullets.size();i++) {
    			GameObject obj = (GameObject)bullets.elementAt(i);
    			obj.paint(g,visibleArea.left,0);
    		}

    		// Draw the active objects
    		Vector visibleActObjs = visibleActiveObjects;
    		for(int i=0;i<visibleActObjs.size();i++) {
    			GameObject actObj = (GameObject)visibleActObjs.elementAt(i);
    			actObj.paint(g,visibleArea.left,0);
    		}

    		if(jumpingPilot != null) {
    			jumpingPilot.paint(g,visibleArea.left,0);
    		}

    		if(gamePaused) {
//    		if(false) {
    			Font font = Font.getFont(Font.FACE_SYSTEM,Font.STYLE_BOLD,Font.SIZE_LARGE);
    			g.setFont(font);
    			g.drawString(TextResources.GAME_GAME,64,30,Graphics.HCENTER|Graphics.BASELINE);
    			g.drawString(TextResources.GAME_PAUSED,64,50,Graphics.HCENTER|Graphics.BASELINE);

    			g.setColor(GameCanvas.WATER_COLOUR);
    			g.fillRect(0,108,getWidth(),20);
    			drawRightSoftKey(g,TextResources.COMMAND_INTERRUPT);
    			drawLeftSoftKey(g,TextResources.COMMAND_CONTINUE);

    		}
    		else {
    			g.drawImage(scoreTable,0,visibleHeight-scoreTable.getHeight(),Graphics.LEFT|Graphics.TOP);
    		}
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    }

    public void refreshVisibleActiveObjects() {
    	// Remove previous objects
    	GameObject actObj = null;
		while(visibleActiveObjects.size()>0) {
			actObj = (GameObject)visibleActiveObjects.elementAt(0);
			if((actObj.xCoord+actObj.width)<visibleArea.left) {
				visibleActiveObjects.removeElementAt(0);
				activeActiveObjects.removeElementAt(0);
//				Logger.logTrace("Active object is hidden at "+actObj.xCoord);
			}
			else {
				break;
			}
		}

		// Check if new objects have become visible
		while(lastVisibleActiveObject<activeObjects.size()) {
			actObj = (GameObject)activeObjects.elementAt(lastVisibleActiveObject);
			if(actObj.bounds.left<(visibleArea.right)) {
				visibleActiveObjects.addElement(actObj);
				lastVisibleActiveObject++;
//				Logger.logTrace("Active object has become visible at "+actObj.xCoord);
			}
			else {
				break;
			}
		}

		// Check if new objects have become active
		while(lastActiveActiveObject<activeObjects.size()) {
			actObj = (GameObject)activeObjects.elementAt(lastActiveActiveObject);
			if((actObj.bounds.left-OBJECT_ACTIVATION_DISTANCE)<(visibleArea.right)) {
				activeActiveObjects.addElement(actObj);
				lastActiveActiveObject++;
//				Logger.logTrace("Active object has become visible at "+actObj.xCoord);
			}
			else {
				break;
			}
		}

    }

    private void removeFirstImage() {
    	int imageStart = ((Integer)landscapeImageLocations.elementAt(0)).intValue();
    	int visibleLimit = imageStart+IMAGE_WIDTH;

    	for(int i=0;i<visiblePassiveObjects.size();i++) {
    		GameObject passObj = (GameObject)visiblePassiveObjects.elementAt(0);

    		if(passObj.bounds.right < visibleLimit) {
    			visiblePassiveObjects.removeElementAt(0);
    		}
    		else {
    			break;
    		}
    	}

    	landscapeImages.removeElementAt(0);
    	landscapeImageLocations.removeElementAt(0);
    }

    public void keyPressed(int keyCode) {
    	try {
	    	if(keybListener != null) {
	    		keybListener.keyPressed(keyCode,getGameAction(keyCode));
	    	}
    	}
    	catch(Throwable t) {
    		MainControl.instance.showErrorDialog(getClass().getName(),t,null);
    		t.printStackTrace();
    	}
    }

    public void keyRepeated(int keyCode) {
    	try  {
	    	if(keybListener != null) {
	    		keybListener.keyRepeated(keyCode,getGameAction(keyCode));
	    	}
    	}
    	catch(Throwable t) {
    		MainControl.instance.showErrorDialog(getClass().getName(),t,null);
    		t.printStackTrace();
    	}
    }

    public void keyReleased(int keyCode) {
    	try {
	    	if(keybListener != null) {
	    		keybListener.keyReleased(keyCode,getGameAction(keyCode));
	    	}
    	}
    	catch(Throwable t) {
    		MainControl.instance.showErrorDialog(getClass().getName(),t,null);
    		t.printStackTrace();
    	}
    }

    public void setMainObject(GameObject mainObj) {
    	mainObject = mainObj;
    }

    public void setKeyboardActionListener(GameControl listener) {
    	keybListener = listener;
    }

    /*
    public Vector getVisibleActiveObjects() {
    	return visibleActiveObjects;
    }

    public Vector getActiveActiveObjects() {
    	return activeActiveObjects;
    }

    public Vector getVisiblePassiveObjects() {
    	return visiblePassiveObjects;
    }
    */
    public void freeImages() {

    	landscapeImages.removeAllElements();
    	landscapeImageLocations.removeAllElements();

    	visibleActiveObjects.removeAllElements();
    	visiblePassiveObjects.removeAllElements();
    	clouds.removeAllElements();
    	bushes.removeAllElements();
/*
    	passiveObjects.removeAllElements();
    	activeObjects.removeAllElements();
    	ownBombs.removeAllElements();
    	bullets.removeAllElements();
*/
    }


    /*
    public Rectangle getVisibleArea() {
    	return visibleArea;
    }
    */

    public void setGamePaused(boolean paused) {
    	gamePaused = paused;
    }
/*
    protected void drawLeftSoftKey(Graphics g, String text) {
    	Font font = Font.getDefaultFont();
    	g.setFont(font);
    	g.drawString(text,2,(getHeight()-font.getHeight()-2),Graphics.TOP|Graphics.LEFT);
    }

    protected void drawRightSoftKey(Graphics g, String text) {
    	Font font = Font.getDefaultFont();
    	g.setFont(font);
    	g.drawString(text,(getWidth()-font.charsWidth(text.toCharArray(),0,text.length())-2),(getHeight()-font.getHeight()-2),Graphics.TOP|Graphics.LEFT);
    }
*/
    /* (non-Javadoc)
	 * @see javax.microedition.lcdui.Canvas#hideNotify()
	 */
	protected void hideNotify() {
		// TODO Auto-generated method stub
		super.hideNotify();
		GameControl instance = GameControl.instance;
		if(instance!=null && instance.gameState == GameControl.STATE_PLAYING) {
			instance.pauseGame();
		}
	}
}
