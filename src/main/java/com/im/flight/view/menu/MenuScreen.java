/* This file was created by Nokia Developer's Suite for J2ME(TM) */

package com.im.flight.view.menu;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.rms.RecordStoreException;

import com.im.flight.control.GameControl;
import com.im.flight.control.MainControl;
import com.im.flight.control.score.ScoreEntry;
import com.im.util.SoundEffects;
import com.im.util.TextResources;

//public class MenuScreen extends List {
public class MenuScreen extends FullScreenInfo /*implements Runnable*/ {

	public static final byte MENU_MENU = 0;
	public static final byte MENU_HELP = 1;
	public static final byte MENU_HISCORE = 2;
	public static final byte MENU_LEVEL = 3;
	public static final byte MENU_SCORE = 4;
	public static final byte MENU_SETTINGS= 5;
	public static final byte MENU_SPLASH = 6;
	public static final byte MENU_ABOUT = 7;
	public static final byte MENU_LOADER = 8;
	public static final byte MENU_LICENSE = 9;


	private byte menuType;
	public static int selectedIndex = 0;

	// FOR SPLASH SCREEN
/*
	private Image companyLogo;
	private Image splash;
	private Image license;
	*/
//	public boolean showLogo = true;
	public static int loadingProgress = 0;

	// FOR HELP SCREEN
	private int currentPage = 1;
	private boolean isLastPage = false;
//	public String[] titles;
//	public String[][] text;

	// FOR LEVEL INFO SCREEN
	public boolean gameLoaded = false;
//	private boolean backgroundDrawn = false;
//	private int level;
	private boolean loading = true;
//	public Vector lines = new Vector();

	// FOR HIGHSCORES
	public Vector scoreEntries;
	private int highlightedRow = -1;
	public int skillLevel = -1;

	// FOR SCORE
	private static final int CHAR_SPACING = 10;
	public int score;
	public boolean highScore = false;
	private static int[] initials = {0,0,0};
	private int activeChar = 0;

	// FOR SETTINGS
	private MainControl mainControl;
//	private int selectedItem = 0;


	public MenuScreen(byte type) throws RecordStoreException {
        menuType = type;
        try {
//        	System.out.println("Created menu type "+type);
        	selectedIndex = 0;
        	mainControl = MainControl.instance;
        	switch(type) {
	        	case MENU_LICENSE:
	        		background = Image.createImage("/res/license.png");
	        		break;
	        	case MENU_LOADER:
	        		background = Image.createImage("/res/co");
	        		break;
			    case MENU_SPLASH:
			    	background = Image.createImage("/res/lo");
			    	break;
			    case MENU_LEVEL:
			    	loading = true;
//			    	break;
			    case MENU_HISCORE:
			    case MENU_SETTINGS:
			    	skillLevel = mainControl.skillLevel;
			    	scoreEntries = MainControl.instance.scoreStore.getHighScores(skillLevel);
			    case MENU_MENU:
			    case MENU_ABOUT:
			    case MENU_HELP:
			    	background = Image.createImage("/res/background.png");
			    	break;
			    case MENU_SCORE:
			    	if(mainControl.level<5) {
			    		background = Image.createImage("/res/parachute.png");
			    	}
			    	else {
			    		background = Image.createImage("/res/background.png");
			    	}
	        }
        }
        catch(Exception e) {
        	e.printStackTrace();
        }
	}

	public void initPage(int page) {
		currentPage = page;

		if(page>=TextResources.HELP_PAGE_TITLES.length) {
			isLastPage = true;
		}
		else {
			isLastPage = false;
		}
	}

	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.Displayable#paint(javax.microedition.lcdui.Graphics)
	 */
	public void paint(Graphics g) {
		try {

			if((menuType != MENU_LOADER) && (background != null)) {
				drawBackground(g);
			}

			if(menuType != MENU_HELP) {
				if(commands.size()>0) {
					Command cmd = (Command)commands.elementAt(0);
					drawLeftSoftKey(g,cmd.getLabel());
				}
				if(commands.size()>1) {
					Command cmd = (Command)commands.elementAt(1);
					drawRightSoftKey(g,cmd.getLabel());
				}
			}

			g.setColor(0);
			g.setFont(smallFont);

			String title = "";

			switch(menuType) {
				case MENU_LICENSE:
//					System.out.println("Drawing license");
					g.drawImage(background,0,0,Graphics.LEFT|Graphics.TOP);
					break;
				case MENU_MENU:
					title = TextResources.MENUSCREEN_TITLE;

					for(int i=0;i<TextResources.MENU_OPTIONS.length;i++) {
						if(selectedIndex==i) {
							drawMarker(17,41+i*LINE_SPACE,g);
						}
						drawShadedString((String)TextResources.MENU_OPTIONS[i],20,(38+i*LINE_SPACE),Graphics.TOP|Graphics.LEFT,g);
					}

					g.setFont(smallFont);
					break;
				case MENU_LOADER:
					g.drawImage(background,64,60,Graphics.VCENTER|Graphics.HCENTER);
					g.setColor(0);
					g.drawRect(15,110,96,10);
//					g.setColor(5*loadingProgress,0,255-5*loadingProgress);
					g.fillRect(17,112,5+loadingProgress*2,7);
					break;
				case MENU_SPLASH:
					g.drawImage(background,0,0,Graphics.LEFT|Graphics.TOP);
					Command cmd = (Command)commands.elementAt(0);
					g.setFont(mediumFont);
					g.drawString(cmd.getLabel(),2,(getHeight()-mediumFont.getHeight()-2),Graphics.TOP|Graphics.LEFT);
					break;
				case MENU_ABOUT:
					title = TextResources.ABOUT_TITLE;

					for(int i=0;i<TextResources.ABOUT_PAGES.length;i++) {
						drawShadedString(TextResources.ABOUT_PAGES[i],10,37+13*i,Graphics.TOP|Graphics.LEFT,g);
					}
					break;
				case MENU_HELP:
					title = TextResources.HELP_PAGE_TITLES[currentPage-1];

					for(int i=0;i<TextResources.HELP_PAGES[currentPage-1].length;i++) {
						drawShadedString(TextResources.HELP_PAGES[currentPage-1][i],10,37+13*i,Graphics.TOP|Graphics.LEFT,g);
					}

					if(currentPage == 1) {
						drawLeftSoftKey(g,TextResources.COMMAND_NEXT);
						drawRightSoftKey(g,TextResources.COMMAND_BACKTOMENU);
					}
					else if(isLastPage) {
						drawLeftSoftKey(g,TextResources.COMMAND_BACKTOMENU);
						drawRightSoftKey(g,TextResources.COMMAND_BACK);
					}
					else {
						drawLeftSoftKey(g,TextResources.COMMAND_NEXT);
						drawRightSoftKey(g,TextResources.COMMAND_BACK);
					}
					break;
				case MENU_LEVEL:
					background = null;
					System.gc();

					if(MainControl.instance.level<5) {
						title = TextResources.LEVEL_TITLE+MainControl.instance.level;
					}
					else {
						title = TextResources.LEVEL_TITLE_PASSED;
						loading = false;
					}
//					g.drawString("Free: "+Runtime.getRuntime().freeMemory()+" bytes",10,28,Graphics.TOP|Graphics.LEFT);

					for(int i=0;i<TextResources.LEVEL_INFOS[MainControl.instance.level-1].length;i++) {
						drawShadedString(TextResources.LEVEL_INFOS[MainControl.instance.level-1][i],10,40+i*13,Graphics.TOP|Graphics.LEFT,g);
					}

					if(loading) {
						String loadingText = TextResources.INFO_LOADING;
						int loadingLenght = smallFont.charsWidth(loadingText.toCharArray(),0,loadingText.length());
						drawShadedString(loadingText,20,95,Graphics.TOP|Graphics.LEFT,g);
						if(gameLoaded) {
							drawShadedString(TextResources.INFO_READY,25+loadingLenght,95,Graphics.TOP|Graphics.LEFT,g);
						}
					}
					break;
				case MENU_HISCORE:
					title = TextResources.HIGHSCORE_TITLE;
					drawShadedString(TextResources.HIGHSCORE_SKILL+": "+TextResources.SETTINGS_LEVELS[skillLevel-1],15,33,Graphics.TOP|Graphics.LEFT,g);

					for(int i=0;i<5;i++) {
						ScoreEntry score = (ScoreEntry)scoreEntries.elementAt(i);
						drawShadedString(((i+1)+" - "+score.score+" "+score.name),20,(48+i*(LINE_SPACE-1)),Graphics.TOP|Graphics.LEFT,g);
					}
					break;
				case MENU_SCORE:
					if(highScore) {
						title = TextResources.SCORE_CONGRATULATIONS;
					}
					else {
						title = TextResources.SCORE_TITLE;
					}

					g.setFont(mediumFont);


					if(highScore) {
						drawShadedString(TextResources.SCORE_HIGHSCORE[0],10,33,Graphics.TOP|Graphics.LEFT,g);
						drawShadedString(TextResources.SCORE_HIGHSCORE[1]+score,10,49,Graphics.TOP|Graphics.LEFT,g);
						drawShadedString(TextResources.SCORE_HIGHSCORE[2],10,65,Graphics.TOP|Graphics.LEFT,g);
						drawShadedString(TextResources.SCORE_HIGHSCORE[3],10,81,Graphics.TOP|Graphics.LEFT,g);

						int charsStartX = 15+mediumFont.charsWidth(TextResources.SCORE_HIGHSCORE[3].toCharArray(),0,TextResources.SCORE_HIGHSCORE[3].length());
						int charsStartY = 81;

						for(int i=0;i<3;i++) {
							drawShadedString(TextResources.HIGHSCORE_CHARSET.substring(initials[i],initials[i]+1),charsStartX+i*CHAR_SPACING,charsStartY,Graphics.TOP|Graphics.LEFT,g);
						}
						g.drawLine(charsStartX+activeChar*CHAR_SPACING,charsStartY+14,charsStartX+(activeChar+1)*CHAR_SPACING-2,charsStartY+14);
					}
					else {
						drawShadedString(TextResources.SCORE_YOURSCORE+score,15,55,Graphics.TOP|Graphics.LEFT,g);
					}
					break;
				case MENU_SETTINGS:
					title = TextResources.SETTINGS_TITLE;
					drawMarker(17,44+LINE_SPACE*selectedIndex,g);

					g.setFont(smallFont);

					String skillLevelText = TextResources.SETTINGS_SKILL+": "+TextResources.SETTINGS_LEVELS[mainControl.skillLevel-1];
					String vibrationsText = null;
					if(mainControl.useVibration) {
						vibrationsText = TextResources.SETTINGS_VIBRATION+": "+TextResources.SETTINGS_YES;
					}
					else {
						vibrationsText = TextResources.SETTINGS_VIBRATION+": "+TextResources.SETTINGS_NO;
					}

					String soundsText = null;
					if(mainControl.useSounds) {
						soundsText = TextResources.SETTINGS_SOUNDS+": "+TextResources.SETTINGS_YES;
					}
					else {
						soundsText = TextResources.SETTINGS_SOUNDS+": "+TextResources.SETTINGS_NO;
					}

					String backlightText = null;
					if(mainControl.useBacklight) {
						backlightText = TextResources.SETTINGS_BACKLIGHT+": "+TextResources.SETTINGS_YES;
					}
					else {
						backlightText = TextResources.SETTINGS_BACKLIGHT+": "+TextResources.SETTINGS_NO;
					}

					String cheatText = null;
					if(mainControl.cheatMode) {
						cheatText = "CHEAT MODE: "+TextResources.SETTINGS_YES;
					}
					else {
						cheatText = "CHEAT MODE: "+TextResources.SETTINGS_NO;
					}

					drawShadedString(skillLevelText,20,41,Graphics.TOP|Graphics.LEFT,g);
					drawShadedString(vibrationsText,20,(41+LINE_SPACE),Graphics.TOP|Graphics.LEFT,g);
					drawShadedString(backlightText,20,(41+LINE_SPACE*2),Graphics.TOP|Graphics.LEFT,g);
					drawShadedString(soundsText,20,(41+LINE_SPACE*3),Graphics.TOP|Graphics.LEFT,g);
					drawShadedString(cheatText,20,(41+LINE_SPACE*4),Graphics.TOP|Graphics.LEFT,g);
					break;
			}
			drawTitle(title,g);

		}
		catch(Throwable t) {
			t.printStackTrace();
		}
	}

	public void keyPressed(int keyCode) {
//		Logger.logTrace("Key pressed: "+keyCode);
		int gameAction = getGameAction(keyCode);
		switch(menuType) {
			case MENU_MENU:
				switch(keyCode) {
					case KEY_STAR:
						MainControl.instance.cheatMode = !MainControl.instance.cheatMode;
						SoundEffects.playEffect(SoundEffects.ENEMY_DESTRUCT);
						return;
					}

					switch(gameAction) {
						case UP:
						if(selectedIndex>0) {
							selectedIndex--;
						}
						else {
							selectedIndex = TextResources.MENU_OPTIONS.length - 1;
						}
						repaint();
						return;
					case DOWN:
						if(selectedIndex<TextResources.MENU_OPTIONS.length - 1) {
							selectedIndex++;
						}
						else {
							selectedIndex = 0;
						}
						repaint();
						return;
					}
					break;
				case MENU_HELP:
					switch(keyCode) {
						case KEY_SOFTKEY1:
							if(isLastPage) {
								commandListener.commandAction((Command)commands.elementAt(0),this);
							}
							else {
								initPage(currentPage+1);
							}
							break;
						case KEY_SOFTKEY2:
							if(currentPage == 1) {
								commandListener.commandAction((Command)commands.elementAt(0),this);
							}
							else {
								initPage(currentPage-1);
							}
							break;
					}

					repaint();
					return;
				case MENU_SCORE:
					if(highScore) {
						switch(keyCode) {
							case KEY_RIGHT_ARROW:
							case KEY_NUM3:
								if(activeChar<2) {
									activeChar++;
								}
								break;
							case KEY_LEFT_ARROW:
							case KEY_NUM2:
								if(activeChar>0) {
									activeChar--;
								}
								break;
							case KEY_UP_ARROW:
							case KEY_NUM1:
								if(initials[activeChar]>0) {
									initials[activeChar]--;
								}
								else {
									initials[activeChar] = TextResources.HIGHSCORE_CHARSET.length()-1;
								}
								break;
							case KEY_DOWN_ARROW:
							case KEY_NUM4:
								if(initials[activeChar]<(TextResources.HIGHSCORE_CHARSET.length()-2)) {
									initials[activeChar]++;
								}
								else {
									initials[activeChar] = 0;
								}
								break;
						}

						repaint();
					}
					break;
				case MENU_SETTINGS:
					switch(keyCode) {
						case KEY_SOFTKEY1:
							if(selectedIndex == 0) {
								mainControl.skillLevel++;
								if(mainControl.skillLevel>3) {
									mainControl.skillLevel=1;
								}
							}
							else if(selectedIndex == 1) {
								mainControl.useVibration = !mainControl.useVibration;
							}
							else if(selectedIndex == 2) {
								mainControl.useBacklight= !mainControl.useBacklight;
							}
							else if(selectedIndex == 3) {
								mainControl.useSounds= !mainControl.useSounds;
							}
							else if(selectedIndex == 4) {
								mainControl.cheatMode= !mainControl.cheatMode;
							}
							repaint();
							return;
						case KEY_SOFTKEY2:
							commandListener.commandAction((Command)commands.elementAt(1),this);
							return;
					}

					switch(gameAction) {
						case UP:
							if(selectedIndex>0) {
								selectedIndex--;
							}
							else {
								selectedIndex = 4;
							}
							repaint();
							return;
						case DOWN:
							if(selectedIndex<4) {
								selectedIndex++;
							}
							else {
								selectedIndex = 0;
							}
							repaint();
							return;
					}
					break;
		}

		switch(keyCode) {
			case KEY_SOFTKEY1:
				if(commands.size()>0) {
					commandListener.commandAction((Command)commands.elementAt(0),this);
				}
				break;
			case KEY_SOFTKEY2:
				if(commands.size()>1) {
					commandListener.commandAction((Command)commands.elementAt(1),this);
				}
				break;
		}
	}

	public String getInitials() {
		StringBuffer buffy = new StringBuffer();
		for(int i=0;i<3;i++) {
			buffy.append(TextResources.HIGHSCORE_CHARSET.substring(initials[i],initials[i]+1));
		}

		return buffy.toString();
	}

	public void freeImages() {
		background = null;
/*
		companyLogo = null;
		splash = null;
		license = null;
		*/
	}
/*
	public void run() {
		try {
			Thread.sleep(2000);
		}
		catch(InterruptedException e) {}

		showLogo = false;
		repaint();
		serviceRepaints();

		SoundEffects.playEffect(SoundEffects.INTRO);
	}
	*/
}
