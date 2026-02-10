/*
 * Created on Aug 25, 2004
 *
 * Sound effects stub for TeaVM browser port.
 * Uses the compat layer's Sound class (which is a no-op stub).
 */
package com.im.util;

import java.util.Vector;

import com.im.flight.control.MainControl;
import com.nokia.mid.sound.Sound;

/**
 * @author surpila
 *
 * Sound effects - stubbed for browser port.
 * Keeps the same API as the original so all call sites work unchanged.
 */
public class SoundEffects {

	public static final byte INTRO = 0;
//	public static final byte ENEMY_SHOOT = 1;
	public static final byte ENEMY_DESTRUCT = 1;
//	public static final byte OWN_BOMB = 1;
	public static final byte PLANE_DESTROYED = 2;
	public static final byte HIGH_SCORE = 3;

	private static Sound sound = new Sound(1000,1000);
	private static Vector effects = new Vector();


	public static void initEffects() {
		try  {
			// Create stub Sound objects for each effect slot
			// The compat Sound class is a no-op stub
			effects.addElement(new Sound(new byte[0], Sound.FORMAT_TONE)); // INTRO
			effects.addElement(new Sound(new byte[0], Sound.FORMAT_TONE)); // ENEMY_DESTRUCT
			effects.addElement(new Sound(new byte[0], Sound.FORMAT_TONE)); // PLANE_DESTROYED
			effects.addElement(new Sound(new byte[0], Sound.FORMAT_TONE)); // HIGH_SCORE
		}
		catch(Throwable t) {
			t.printStackTrace();
		}
	}

	public static void playEffect(byte effectId) {
		try {
			if(MainControl.instance != null && MainControl.instance.useSounds) {
				if(effectId < effects.size()) {
					Sound effect = (Sound)effects.elementAt(effectId);
					effect.play(1);
					sound = effect;
				}
			}
		}
		catch(Throwable t) {
			t.printStackTrace();
		}
	}

	public static void stopSound() {
		try {
			sound.stop();
		}
		catch(Throwable t) {
			// Ignore - sound is a stub anyway
		}
	}
}
