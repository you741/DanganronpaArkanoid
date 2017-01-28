package com.zhou.arkanoid;

class Brick {
	private double x,y,width,height;
	private int dur;
	private String eff;
	public Brick(double x, double y, double width, double height, int dur){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.dur = dur;
		this.eff = "";
	}
	public Brick(double x, double y, double width, double height, int dur, String eff){
		//allows for effect
		this(x,y,width,height,dur);
		this.eff = eff;
	}
	public boolean isGone(){
		return dur <= 0;
	}
	
	public int getDurability(){
		return dur;
	}
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
	
	public double getWidth(){
		return width;
	}
	
	public double getHeight(){
		return height;
	}
	
	public String getEffect(){
		return eff;
	}
	
	public void setEffect(String newEff){
		eff = newEff;
	}
	public boolean checkCollision(Ball ball){
		//check for collision with ball
		if((checkTop(ball.getY(),Ball.HEIGHT) || checkBot(ball.getY())) && (checkLeft(ball.getX()) || checkRight(ball.getX(),Ball.WIDTH))){
			if(dur < 6)
				dur--; //doesn't allow durability decrease for strong bricks
			if(dur < 0) dur = 0; //prevents negative durability
			return true;
		} else{
			return false;
		}
	}
	public boolean checkTop(double y, double height){
		//checks if top side of ball collides
		return y + height <= this.y + this.height && y + height >= this.y;
	}
	public boolean checkBot(double y){
		//checks if bottom collides
		return y >= this.y && y <= this.y + this.height;
	}
	public boolean checkLeft(double x){
		//checks if left side collides
		return x <= this.x + this.width && x >= this.x;
	}
	public boolean checkRight(double x, double width){
		//check if right side collides
		return x + width >= this.x && x + width <= this.x + this.width;
	}
	public boolean isXBounce(Ball ball){
		if(checkTop(ball.getY(),Ball.HEIGHT)){
			double dy = (ball.getY()+Ball.HEIGHT) - this.y;
			double dx = dy+1;
			if(checkLeft(ball.getX())){
				dx = this.x + this.width - ball.getX();
			}
			if(checkRight(ball.getX(),Ball.WIDTH)){
				dx =  (ball.getX()+Ball.WIDTH) - this.x;
			}
			return dx < dy;
		}
		if(checkBot(ball.getY())){
			double dy = this.y + this.height - ball.getY();
			double dx = dy+1;
			if(checkLeft(ball.getX())){
				dx = this.x + this.width - ball.getX();
			}
			if(checkRight(ball.getX(),Ball.WIDTH)){
				dx =  (ball.getX()+Ball.WIDTH) - this.x;
			}
			return dx < dy;
		}
		return false;
	}
}
