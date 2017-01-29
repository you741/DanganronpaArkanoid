package com.zhou.arkanoid;

import com.badlogic.gdx.math.Rectangle;

class Ball {
	public static final double WIDTH = 16;
	public static final double HEIGHT = 16;
	private double x,y,spd,ang;
	private int speedCounter; //counter for when speed was last edited - for time based events
	public Ball(double x, double y, double ang){
		this.x = x;
		this.y = y;
		this.ang = ang;
		this.spd = 5;
		speedCounter = -1;
	}
	public Ball(double x, double y, double ang, double spd, int time){
		//for setting speed and speed counter as well
		this(x,y,ang);
		this.spd = spd;
		this.speedCounter = time;
	}
	
	public void move(double dist){
		//moves by the given distance with the current angle
		this.x += Math.cos(Math.PI*this.ang/180)*dist;
		this.y += Math.sin(Math.PI*this.ang/180)*dist;
	}
	public void move(int mult){
		//multiplier to scale speed
		move((double)mult*spd);
	}
	public void move(){
		move(spd); //moves at spd
	}
	public void normalizeSpeed(int time){
		//normalizes speed based on time
		if(time - speedCounter >= 1200 && spd != 5){
			spd = 5;
		}
	}
	//Simple getter methods
	public double getX(){
		return x;
	}
	public double getY(){
		return y;
	}
	public double getSpeed(){
		return spd;
	}
	public double getAngle(){
		return ang;
	}
	public int getSpeedCounter(){
		return speedCounter;
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
	public void setSpeed(double spd){
		this.spd = spd;
	}
	public void setAngle(double newAng){
		//changes the angle
		this.ang = newAng;
	}
	public void setSpeedCounter(int time){
		speedCounter = time;
	}
	
}
