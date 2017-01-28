package com.zhou.arkanoid;

import com.badlogic.gdx.math.Rectangle;

class Ball {
	public static final double WIDTH = 16;
	public static final double HEIGHT = 16;
	private double x,y,ang;
	public Ball(double x, double y, double ang){
		this.x = x;
		this.y = y;
		this.ang = ang;
	}
	
	public void move(double dist){
		//moves by the given distance with the current angle
		this.x += Math.cos(Math.PI*this.ang/180)*dist;
		this.y += Math.sin(Math.PI*this.ang/180)*dist;
	}
	
	//Simple getter methods
	public double getX(){
		return x;
	}
	public double getY(){
		return y;
	}
	public double getAngle(){
		return ang;
	}
	
	public Rectangle getRect(){
		return new Rectangle((float)x,(float)y,(float)(WIDTH),(float)(HEIGHT));
	}
	
	//simple setter methods
	public void setX(double x){
		this.x = x;
	}
	public void setY(double y){
		this.y = y;
	}
	public void setAngle(double newAng){
		//changes the angle
		this.ang = newAng;
	}
	
}
