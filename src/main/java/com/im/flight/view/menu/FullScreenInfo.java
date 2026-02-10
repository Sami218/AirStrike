/*
 * Created on 29.12.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.im.flight.view.menu;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.im.flight.control.MainControl;
import com.im.flight.view.game.GameCanvas;
import com.nokia.mid.ui.DirectGraphics;
import com.nokia.mid.ui.DirectUtils;
import com.nokia.mid.ui.FullCanvas;

/**
 * @author surpila
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class FullScreenInfo extends FullCanvas {

	protected static final int LINE_SPACE = 12;
	public static Font smallFont = Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_SMALL);
//	protected Font mediumFont = Font.getDefaultFont();
	public static Font mediumFont = Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_MEDIUM);
	public static Font largeFont = Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_LARGE);
	protected Vector commands = new Vector();
	protected Image background;
//	protected String title;
	protected CommandListener commandListener;

	public FullScreenInfo() {
		super();
	}


	protected void drawMarker(int x,int y, Graphics g) {
//		DirectGraphics dg = DirectUtils.getDirectGraphics(g);
//		dg.fillTriangle(x-8,y+2,x-8,y-2,x,y,0xFF000000);
		g.setColor(255,255,255);
		g.fillRect(0,y-5,getWidth(),12);
		g.setColor(0,0,0);
	}

	protected void drawTitle(String title,Graphics g) {
		g.setFont(largeFont);
		drawShadedString(title,11,6,Graphics.TOP|Graphics.LEFT,g);
		g.setFont(smallFont);
	}

	protected void drawShadedString(String text,int x,int y,int anchor,Graphics g) {
		g.setColor(255,255,255);
		g.drawString(text,x+1,y+1,anchor);
		g.setColor(0,0,0);
		g.drawString(text,x,y,anchor);

	}

	protected void drawBackground(Graphics g) {
		int origColor = g.getColor();
		g.setColor(GameCanvas.SKY_COLOUR);
		g.fillRect(0,0,getWidth(),getHeight());

		if(background != null) {
			g.drawImage(background,64,60,Graphics.VCENTER|Graphics.HCENTER);
		}

		g.setColor(GameCanvas.WATER_COLOUR);
		//g.fillRect(0,0,getWidth(),35);

 		g.fillRect(0,108,getWidth(),20);
		g.setColor(origColor);
	}

	public void drawLeftSoftKey(Graphics g, String text) {
		g.setFont(mediumFont);
		g.setColor(255,255,255);
		g.drawString(text,2,(getHeight()-mediumFont.getHeight()-2),Graphics.TOP|Graphics.LEFT);
	}

	public void drawRightSoftKey(Graphics g, String text) {
		g.setFont(mediumFont);
		g.setColor(255,255,255);
		g.drawString(text,(getWidth()-mediumFont.charsWidth(text.toCharArray(),0,text.length())-2),(getHeight()-mediumFont.getHeight()-2),Graphics.TOP|Graphics.LEFT);
	}

	public void setCommandListener(CommandListener listener) {
		commandListener = listener;
	}

	public void addCommand(Command cmd) {
		commands.addElement(cmd);
	}

	public void keyRepeated(int keyCode) {
		keyPressed(keyCode);
	}

	/**
	 * Map browser key codes to J2ME game actions.
	 * In the browser, the compat Canvas layer delivers browser key codes.
	 * We map:
	 *   - Arrow keys and WASD to directional game actions
	 *   - Space to FIRE
	 *   - Z/X to GAME_A
	 *   - Original numpad mappings are preserved
	 *
	 * Browser key codes (from KeyboardEvent.keyCode):
	 *   ArrowUp=38, ArrowDown=40, ArrowLeft=37, ArrowRight=39
	 *   W=87, A=65, S=83, D=68
	 *   Space=32, Z=90, X=88
	 *   Numpad: 1=49, 2=50, 3=51, 4=52, 5=53, 6=54
	 *
	 * The compat Canvas may also pass negative key codes for arrows:
	 *   -1=UP, -2=DOWN, -3=LEFT, -4=RIGHT
	 */
	public int getGameAction(int keyCode) {
		switch(keyCode) {
			// Original numpad mappings
			case KEY_NUM1:
				return UP;
			case KEY_NUM4:
				return DOWN;
			case KEY_NUM3:
				return RIGHT;
			case KEY_NUM2:
				return LEFT;
			case KEY_NUM5:
				return FIRE;
			case KEY_NUM6:
				return GAME_A;

			// Browser arrow keys
			case 38: // ArrowUp
			case 87: // W
			case -1: // Compat UP
				return UP;
			case 40: // ArrowDown
			case 83: // S
			case -2: // Compat DOWN
				return DOWN;
			case 39: // ArrowRight
			case 68: // D
			case -4: // Compat RIGHT
				return RIGHT;
			case 37: // ArrowLeft
			case 65: // A
			case -3: // Compat LEFT
				return LEFT;
			case 32: // Space
				return FIRE;
			case 90: // Z
			case 88: // X
				return GAME_A;
		}
		return super.getGameAction(keyCode);
	}
}
