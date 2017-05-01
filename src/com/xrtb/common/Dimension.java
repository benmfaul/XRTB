package com.xrtb.common;


/**
 * Determines if  leftx <= x <= rightx AND righty <= y <= rightx
 * @author Ben M. Faul
 *
 */
public class Dimension {
	
	// left x value
	int leftX = 0;
	// left y value
	int leftY = 0;
	
	// right x value
	int rightX = 0;
	// right y value
	int rightY = 0;
	
	
	// Default constructor for JSON
	public Dimension() {
		
	}
	
	/**
	 * Get left x value.
	 * @return left x.
	 */
	public Integer getLeftX() {
		return leftX;
	}

	/**
	 * Set left x value.
	 * @param leftX int. The value of the left side of the x equation.
	 */
	public void setLeftX(Integer leftX) {
		this.leftX = leftX;
	}

	/**
	 * Get the left hand y value.
	 * @return int. The value of the left y side of the y equation.
	 */
	public Integer getLeftY() {
		return leftY;
	}

	/**
	 * Set the left y value of the equation.
	 * @param leftY int. The left y value of the equation.
	 */
	public void setLeftY(Integer leftY) {
		this.leftY = leftY;
	}

	/** 
	 * Get right X side of the equation.
	 * @return int. The right hand x value of the equation.
	 */
	public Integer getRightX() {
		return rightX;
	}

	/**
	 * Set the right hand side of the x eqyation.
	 * @param rightX int. The value of the right hand eside of the x equation.
	 */
	public void setRightX(Integer rightX) {
		this.rightX = rightX;
	}

	/**
	 * Get the right hand side of y of the equation.
	 * @return int. The right hand y value.
	 */
	public Integer getRightY() {
		return rightY;
	}

	/**
	 * Set the right hand y value in the equation.
	 * @param rightY int. The right hand side of the y equation.
	 */
	public void setRightY(Integer rightY) {
		this.rightY = rightY;
	}
	

	/**
	 * Handles x in the form of leftx op x op righty. Example 100 <= x <= 400. Or x is between 100 and 400, To make it x = 200 use 200 <= x >= 200
	 * @param leftX int. The left hand x value
	 * @param rightX int. The right y value
	 * @param leftY int. The left y value.
	 * @param rightY int. The value of the right y.
	 */
		
	public Dimension(int leftX, int rightX, int leftY, int rightY) {
		
		this.leftX = leftX;
		this.leftY = leftY;
		this.rightX = rightX;
		this.rightY = rightY;
		
	}
	
	public Dimension(int x, int y) {
		this.leftX = x;
		this.leftY = y;
		this.rightX = x;
		this.rightY = y;
	}
	
	/**
	 * Answers the question does leftX <= x <= rightX AND lefty <= y <= rightY?
	 * @param x int. The value of x. (width dimension)
	 * @param y int the value of y. (height dimension)
	 * @return boolean. Returns true of the equation is true. Else, returns false. If x or y is null, it returns true.
	 */
	public boolean fits(Integer x, Integer y) {
	
		
		if (x != null && !(leftX <= x && x <= rightX))
			return false;
		
		if (y != null && !(leftY <= y && y <= rightY))
			return false;
		
		return true;
	}
	
}
