/*
 * Created on 13.12.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.im.flight.view.game;


public final class Rectangle {

	public int top;
	public int bottom;
	public int left;
	public int right;

	public Rectangle() {
		super();
	}

	public Rectangle(int x1,int y1,int x2,int y2) {
		top = y1;
		bottom = y2;
		left = x1;
		right = x2;
	}

	public boolean collidesWith(Rectangle otherRect) {
		if(isInside(otherRect.left,otherRect.top)) {
			return true;
		}
		else if(isInside(otherRect.left,otherRect.bottom)) {
			return true;
		}
		else if(isInside(otherRect.right,otherRect.top)) {
			return true;
		}
		else if(isInside(otherRect.right,otherRect.bottom)) {
			return true;
		}
		else {
			return false;
		}
	}

	public boolean isInside(int x,int y) {
		if((x>left)&&(x<right)&&(y>bottom)&&(y<top)) {
			return true;
		}
		else {
			return false;
		}
	}
}
