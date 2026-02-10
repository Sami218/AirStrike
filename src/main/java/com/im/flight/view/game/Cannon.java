/*
 * Created on 6.12.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.im.flight.view.game;

import java.util.Random;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.im.flight.control.GameControl;
import com.im.flight.control.MainControl;
import com.im.util.ObjectProperties;
import com.nokia.mid.ui.DirectGraphics;
import com.nokia.mid.ui.DirectUtils;

public class Cannon extends GameObject {

	private static int SHOOTSTARTFACTOR = 1500000000;
	private static Random randomGen = new Random();
	public static final int BULLET_SPEED = 2;
//	protected int minTicksSinceLastShot = 0;
	private int ticksSinceLastShot = -1;
	private int interval = 0;
	protected byte bulletOffset = 0;

//	public static byte CANNON_INTERVAL = 90;
//	public static byte TANK_INTERVAL = 30;

	// FOR SUBMARINE
	public static byte SHOOT_IMAGE_COUNT = 4;
	public static String SHOOT_IMAGE_NAME = "ss";
	private boolean firingMissile = false;

	// SHIP
	public static final byte MAX_HELICOPTER_HIT_COUNT = 1;
	public static final byte MAX_HELICOPTER_Y_COORD = 127;
	public static final byte MAX_SHIP_HIT_COUNT = 3;
	private byte shipCannonX = 0;
//	private boolean shipForward = false;

//	public static final byte FLAME_TIME = 5;
//	public static final byte SMOKE_TIME = 10;
//	private Image cannonSmoke = getImage("cannoncloud.png");
	private byte hitCount = 0;
	private boolean movingORsinking = false;
	private byte maxHelicopterYCoord = 127;
	private byte shootSequence = 0;
//	private byte flameState = 0;

	/*
	public Cannon() {
		super();
	}
*/
	public Cannon(int x, byte y, byte objectType) {
		super(x, y, (byte)0, (byte)0, objectType);
//		minTicksSinceLastShot = objectProperties.cannonInterval;
		maxHelicopterYCoord = y;
		initObjectProperties();
	}

	private void initObjectProperties() {
		interval = objectProperties.cannonInterval-((MainControl.instance.skillLevel-1)*objectProperties.cannonInterval)/4;

		switch(objectProperties.objectType) {
			case TYPE_BOAT:
				bulletOffset = -2;
				break;
			case TYPE_SUBMARINE:
				additionalAnimation = new Image[SHOOT_IMAGE_COUNT];
				for(int i=0;i<SHOOT_IMAGE_COUNT;i++) {
					String shootImageName = SHOOT_IMAGE_NAME+(i+1);
					additionalAnimation[i] = getImage(shootImageName);
				}
				break;
			case TYPE_MOVINGHELICOPTER:
				movingORsinking = true;
				break;

		}
	}


	protected boolean isTimeToShoot() {
		// randomize the starting time of shooting
		if(ticksSinceLastShot<0) {
//			Logger.logTrace("Waiting...");
			int randNum = randomGen.nextInt();
			if(randNum>SHOOTSTARTFACTOR) {
				ticksSinceLastShot=interval;
//				Logger.logTrace("Interval set first time to: "+ticksSinceLastShot);
			}
		}
		else if(currentState==INITIAL_STATE){
			ticksSinceLastShot++;
//			Logger.logTrace("Ticks since last shot: "+ticksSinceLastShot);
			if((ticksSinceLastShot>interval)&&((objType!=TYPE_MOVINGHELICOPTER && objType!=TYPE_HELICOPTER)||(GameControl.instance.gameCanvas.visibleArea.right<GameControl.instance.levelLength-64))&&GameControl.instance.gameState == GameControl.STATE_PLAYING) {
				ticksSinceLastShot=0;
				return true;
			}
		}
		return false;
	}


	protected void shoot() {
		switch(objType) {
			case TYPE_CANNON:
			case TYPE_BOAT:
				GameControl.instance.addBullet(new GameObject(xCoord+bulletOffset,yCoord-height,(byte)0,(byte)(-1*BULLET_SPEED),TYPE_BULLET));
				break;
			case TYPE_TANK:
				GameControl.instance.addBullet(new GameObject(xCoord-4,yCoord-height,(byte)(-1*BULLET_SPEED),(byte)(-1*BULLET_SPEED),TYPE_BULLET));
				break;
			case TYPE_SUBMARINE:
				firingMissile = true;
				break;
			case TYPE_HELICOPTER:
			case TYPE_MOVINGHELICOPTER:
				if(GameControl.instance.level != 4) {
					GameObject bullet = new GameObject(xCoord-5,yCoord-1,(byte)(-1*BULLET_SPEED),(byte)0,TYPE_BULLET);
					GameControl.instance.addBullet(bullet);
				}
				else if(isHelicopterFlyingForward(3)) {
					GameObject bullet = new GameObject(xCoord+5,yCoord-1,(byte)(2*BULLET_SPEED),(byte)0,TYPE_BULLET);
					GameControl.instance.addBullet(bullet);
				}
				break;
			case TYPE_SHIP:
//				int cannonX = 0;
				boolean shipForward = false;

				switch(shootSequence) {
					case 0: shipCannonX = 0;
						shipForward =true;
						break;
					case 1: shipCannonX = 8;
						shipForward = true;
						break;
					case 2: shipCannonX = 43;
						break;
					case 3: shipCannonX = 49;
						break;
					case 4: shipCannonX = 55;
				}

				GameObject bullet = null;
				if(shipForward) {
					bullet = new GameObject(bounds.left+shipCannonX,yCoord-height,(byte)(-1*BULLET_SPEED),(byte)(-1*BULLET_SPEED),TYPE_BULLET);
				}
				else {
					bullet = new GameObject(bounds.left+shipCannonX,yCoord-height+2,(byte)0,(byte)(-1*BULLET_SPEED),TYPE_BULLET);
				}
				GameControl.instance.addBullet(bullet);

				shootSequence++;
				if(shootSequence>4) {
					shootSequence = 0;
				}
				break;

		}
	}

/*
	protected void printScore(Graphics graphics,int xOffset, int yOffset) {
		if(currentState!=INITIAL_STATE && ((System.currentTimeMillis()-collisionTime)<SCORE_DELAY)) {
			int score = GameControl.getInstance().getScore(objType);
			if(score>0) {
				graphics.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_SMALL));
				graphics.drawString(Integer.toString(score),xCoord-xOffset,yCoord-yOffset,getImageAnchorPoint());
			}
		}
	}*/

	protected void onAnimationEnd() {
		super.onAnimationEnd();

		if(objType == TYPE_SUBMARINE && currentState == INITIAL_STATE && firingMissile) {
			firingMissile = false;
			GameObject missile = new GameObject(xCoord-3,(byte)(yCoord-height+12),(byte)0,(byte)-1,TYPE_MISSILE);
			missile.missileDirection = MISSILE_UP;
			GameControl.instance.addBullet(missile);
		}
	}

	protected Image[] getImageStore() {
		if((objType == TYPE_SUBMARINE) && (currentState==INITIAL_STATE)&&firingMissile) {
			return additionalAnimation;
		}
		else {
			return super.getImageStore();
		}
	}

	public void onCollision(byte collisionType) {
		switch(objType) {
			case TYPE_HELICOPTER:
			case TYPE_MOVINGHELICOPTER:
				if(GameControl.instance.level != 4) {
					hitCount++;
				}

				if(hitCount>=MainControl.instance.skillLevel) {
					currentState = DESTRUCTION_STATE;
				}
				break;
			case TYPE_SHIP:
				hitCount++;
				break;
			default:
				super.onCollision(collisionType);
		}
	}

	public void paint(Graphics graphics,int xOffset, int yOffset) {
		if(objType == TYPE_HELICOPTER || objType == TYPE_MOVINGHELICOPTER) {
			int propelOffset = 0;
			if(GameControl.instance.level != 4) {
				super.paint(graphics,xOffset,yOffset);
			}
			else if(currentImage!=null) {
				DirectGraphics dg = DirectUtils.getDirectGraphics(graphics);
				if(isHelicopterFlyingForward(3)) {
					propelOffset = 4;
					dg.drawImage(currentImage,xCoord-xOffset,yCoord-yOffset,getImageAnchorPoint(),DirectGraphics.FLIP_HORIZONTAL);
				}
				else {
					dg.drawImage(currentImage,xCoord-xOffset,yCoord-yOffset,getImageAnchorPoint(),0);

				}
			}
			if((currentState == INITIAL_STATE) && ((imageLifeCounter*2)<objectProperties.imageLifetime)) {
				graphics.drawLine(xCoord-width/2-xOffset+propelOffset,yCoord-height+1,xCoord-width/2-xOffset+propelOffset+29,yCoord-height+1);
			}
		}
		else if(objType == TYPE_SHIP && currentState == DESTRUCTION_STATE) {
			for(int i=0;i<3;i++) {
				graphics.drawImage(currentImage,xCoord-xOffset+22-i*22,yCoord-yOffset,getImageAnchorPoint());
			}
		}
		else {
			super.paint(graphics,xOffset,yOffset);
		}
	}

	public void advance() {
		super.advance();

		if(isTimeToShoot()) {
//			flameState = 0;
			shoot();
		}

		if(objType == TYPE_HELICOPTER || objType == TYPE_MOVINGHELICOPTER) {

			GameCanvas gameCanvas = GameControl.instance.gameCanvas;
			Rectangle visibleArea = gameCanvas.visibleArea;
			if((currentState == INITIAL_STATE) && (bounds.left<visibleArea.right)) {
				if(movingORsinking && (bounds.right<visibleArea.right-3)||(GameControl.instance.gameState == GameControl.STATE_ENDING)) {
					xCoord = xCoord+gameCanvas.screenSpeed;
				}
				else if(isHelicopterFlyingForward(3)) {
					xCoord = xCoord+gameCanvas.screenSpeed;
				}

				int planeY = GameControl.instance.thePlane.yCoord;
				if((GameControl.instance.level != 4) || isHelicopterFlyingForward(60)) {
					if(yCoord<maxHelicopterYCoord && yCoord<planeY) {
						yCoord++;
					}
					else if(yCoord>planeY) {
						yCoord--;
					}
				}
			}
		}
		else if(objType == TYPE_SHIP && currentState == INITIAL_STATE && hitCount>=MAX_SHIP_HIT_COUNT) {
			currentState = DESTRUCTION_STATE;
			MainControl.instance.shipDestroyed();
		}

	}

	private boolean isHelicopterFlyingForward(int offset) {
		Rectangle visibleArea = GameControl.instance.gameCanvas.visibleArea;
		return (GameControl.instance.level == 4) && ((bounds.left<(visibleArea.left+offset))||(GameControl.instance.gameState == GameControl.STATE_ENDING));
	}

}
