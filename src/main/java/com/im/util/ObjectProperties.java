/*
 * Created on Jul 25, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.im.util;

import java.util.Hashtable;

import com.im.flight.view.game.GameObject;

/**
 * @author surpila
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ObjectProperties {

	// Default values
	public byte objectType = 1;
	public byte objectMode = GameObject.MODE_ACTIVE;
	public byte startimageCount = 1;
	public String startimageName = null;
	public byte destructimageCount = 0;
	public String destructimageName = null;
	public byte finalimageCount = 0;
	public String finalimageName = null;
	public byte imageLifetime = 5;
	public byte cannonInterval = 90;

	private static Hashtable objectPropertyTable = new Hashtable();

	public static ObjectProperties getObjectProperties(byte objectType) {
		ObjectProperties properties = (ObjectProperties)objectPropertyTable.get(new Byte(objectType));

		if(properties == null) {
			properties = new ObjectProperties();
			properties.objectType = objectType;

			switch(objectType) {
				case GameObject.TYPE_BOAT:
					properties.startimageName = "bo";
					properties.destructimageCount = 4;
					properties.destructimageName = "e";
					break;
				case GameObject.TYPE_BOMB:
					properties.startimageName = "b";
					properties.destructimageCount = 4;
					properties.destructimageName = "se";
					break;
				case GameObject.TYPE_BUILDING:
					properties.startimageName = "bu";
					properties.destructimageCount = 1;
					properties.destructimageName = "bub";
					properties.finalimageCount = 1;
					properties.finalimageName = "but";
					properties.imageLifetime = 60;
					break;
				case GameObject.TYPE_CANNON:
					properties.startimageName = "c";
					properties.destructimageCount = 4;
					properties.destructimageName = "e";
					properties.finalimageCount = 1;
					properties.finalimageName = "cd";
					break;
				case GameObject.TYPE_HELICOPTER:
					properties.startimageCount = 1;
					properties.startimageName = "h";
					properties.destructimageCount = 4;
					properties.destructimageName = "ae";
					properties.cannonInterval = 40;
					break;
				case GameObject.TYPE_LIGHTHOUSE:
					properties.objectMode = GameObject.MODE_PASSIVE;
					properties.startimageName = "l";
					break;
				case GameObject.TYPE_MISSILE:
					properties.startimageCount = 2;
					properties.startimageName = "m";
					properties.destructimageCount = 4;
					properties.destructimageName = "se";
					break;
				case GameObject.TYPE_MOVINGHELICOPTER:
					properties.startimageCount = 1;
					properties.startimageName = "h";
					properties.destructimageCount = 4;
					properties.destructimageName = "ae";
					properties.cannonInterval = 40;
					break;
				case GameObject.TYPE_PLANE:
					properties.objectMode = GameObject.MODE_MAIN;
					properties.startimageCount = 2;
					properties.startimageName = "p";
					properties.destructimageCount = 4;
					properties.destructimageName = "ae";
					properties.imageLifetime = 4;
					break;
				case GameObject.TYPE_SHIP:
					properties.startimageName = "s";
					properties.destructimageCount = 4;
					properties.destructimageName = "e";
					properties.imageLifetime = 10;
					properties.cannonInterval = 20;
					break;
				case GameObject.TYPE_SUBMARINE:
					properties.startimageName = "su";
					properties.destructimageCount = 4;
					properties.destructimageName = "e";
					properties.imageLifetime = 10;
					properties.cannonInterval = 60;
					break;
				case GameObject.TYPE_TANK:
					properties.startimageName = "t";
					properties.destructimageCount = 4;
					properties.destructimageName = "e";
					properties.finalimageCount = 1;
					properties.finalimageName = "cd";
					properties.cannonInterval = 30;
					break;
				case GameObject.TYPE_TREE:
					properties.startimageName = "tr";
					properties.destructimageCount = 1;
					properties.destructimageName = "tb";
					properties.finalimageCount = 1;
					properties.finalimageName = "tt";
					properties.imageLifetime = 60;
					break;
				case GameObject.TYPE_SMALL_CLOUD:
					properties.objectMode = GameObject.MODE_TRANSPARENT;
					properties.startimageName = "sc";
					break;
				case GameObject.TYPE_MEDIUM_CLOUD:
					properties.objectMode = GameObject.MODE_TRANSPARENT;
					properties.startimageName = "cl";
					break;
				case GameObject.TYPE_BIG_CLOUD:
					properties.objectMode = GameObject.MODE_TRANSPARENT;
					properties.startimageName = "bc";
					break;
				case GameObject.TYPE_BUSH:
					properties.objectMode = GameObject.MODE_TRANSPARENT;
					properties.startimageName = "bush";
					break;
				case GameObject.TYPE_PARACHUTE:
					properties.objectMode = GameObject.MODE_ACTIVE;
					properties.startimageCount = 1;
					properties.startimageName = "ff";
					properties.destructimageCount = 1;
					properties.destructimageName = "po";
					properties.finalimageCount = 1;
					properties.finalimageName = "pa";
					properties.imageLifetime = 20;
					break;

				}
			objectPropertyTable.put(new Byte(objectType),properties);
		}

		return properties;
	}
}
