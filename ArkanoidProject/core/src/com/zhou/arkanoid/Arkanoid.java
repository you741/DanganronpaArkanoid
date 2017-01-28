package com.zhou.arkanoid;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;

import java.util.*;

/**
 * @author Yttrium Z (You Zhou)
 * @category game
 * @version 1.0
 * @since January 27th, 2017
 * This is a simple game for a school assignment. It is based off the game Arkanoid or Pong.
 * NOTE: No copyright infringement was intended for this non-profit game. All images and music belong to their
 * respectful owners
 */

public class Arkanoid extends ApplicationAdapter implements InputProcessor{
	SpriteBatch batch;
	Ball ball;
	ArrayList<Ball> extraBalls; //extra balls from bonus brick
	ArrayList<Brick> bricks;
	ShapeRenderer shapeRenderer;
	
	BitmapFont font;
	BitmapFont title;
	
	//TEXTURES AND SPRITES
	Texture[] brickSprites; //brick textures based on durability
	Texture menuBG;
	Texture playButton;
	Texture playButtonHover;
	Texture ballsEffectSym;
	Texture lifeEffectSym;
	Texture bigEffectSym;
	Texture smallEffectSym;
	Sprite ballSprite;
	Sprite platSprite;
	
	double platx,platy,platwidth,platheight; //platform x and y and width and height
	Rectangle platRect;
	boolean onPlat; //is ball sitting on the platform?
	
	int lives;
	int level;
	int time; //time elapsed in ticks
	int savePoint; //savePoint is a counter for time, for time based events
	int spd; //speed of ball
	int quota; //amount of bricks that need to be left at most to win
	
	String mode; //which mode the game is in (menu, game, dead, victory)
	
	Music music;
	Sound sound;
	
	//Rectangles
	Rectangle playRect;
	@Override
	public void create () {
		//Input
		Gdx.input.setInputProcessor(this);
		batch = new SpriteBatch();
		
		mode = "menu";
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		title = new BitmapFont();
		title.getData().setScale(3);
		title.setColor(Color.WHITE);
		//Shape Renderer
		shapeRenderer = new ShapeRenderer();
		//MUSIC
		if(music == null){
			music = Gdx.audio.newMusic(Gdx.files.internal("102-dangan-ronpa-.mp3"));
			music.setLooping(true);
			music.play();
		}
		
		//sets important textures
		brickSprites = new Texture[7];
		String[] brickTextSrc = new String[]{"Red","Red","Yellow","Green","Cyan","Blue","Strong"};
		for(int i = 0;i < 7;i++){
			brickSprites[i] = new Texture(brickTextSrc[i]+"Brick.png");
		}
		menuBG = new Texture("menu.png");
		playButton = new Texture("playButton.png");
		playButtonHover = new Texture("playButtonHover.png");
		ballsEffectSym = new Texture("ballSymbol.png");
		lifeEffectSym = new Texture("lifeSymbol.png");
		bigEffectSym = new Texture("bigSymbol.png");
		smallEffectSym = new Texture("smallSymbol.png");
		//sets important rectangles
		playRect = new Rectangle(335,100,256,128);
	}

	@Override
	public void render () {
		if(mode.equals("menu")){
			Gdx.gl.glClearColor(0,0,0,1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			batch.begin();
			batch.draw(menuBG, 0, -384);
			if(playRect.contains(getMx(),getMy())){
				batch.draw(playButtonHover, 335, 100);
			} else{
				batch.draw(playButton, 335, 100);
			}
			batch.end();
			String buttonClicked = checkClick(); //checks for button clicks
			if(buttonClicked.equals("play")){
				startGame();
			}
		} if(mode.equals("victory")){
			Gdx.gl.glClearColor(0, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			batch.begin();
			font.draw(batch,"YOU WIN!",Gdx.graphics.getWidth()/2-32, Gdx.graphics.getHeight()/2-5);
			batch.end();
			if(savePoint < 0)
				savePoint = time;
			if(time >= savePoint + 240){
				create();
			}
			time++;
		} else if(mode.equals("dead")){
			//handles defeat
			Gdx.gl.glClearColor((float)0.7, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			batch.begin();
			font.draw(batch, "GAME OVER", Gdx.graphics.getWidth()/2-32, Gdx.graphics.getHeight()/2-5);
			batch.end();
			if(savePoint < 0)
				savePoint = time;
			if(time >= savePoint + 240){
				startGame();
			}
			time++;
		} else if(mode.equals("game")){
			Gdx.gl.glClearColor((float)0.88, (float)0.5, (float)0.88, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			if(!onPlat){
				//handles user moving platform
				ball.move(spd);
				for(int i = extraBalls.size()-1;i>=0;i--){
					Ball b = extraBalls.get(i);
					b.move(spd);
				}
				movePlatform();
				bounce(ball);
				for(int i = extraBalls.size()-1;i>=0;i--){
					Ball b = extraBalls.get(i);
					bounce(b);
				}
				checkLose();
			}
			//sets position of sprites
			ballSprite.setPosition((float)ball.getX(), (float)ball.getY());
			//handles big and small effects before platsprite
			if(time - savePoint > 1200 && savePoint > 0){
				//resets small and big
				platSprite.setScale(1,1);
				platwidth = 128;
			}
			platRect.width = (float)platwidth;
			float npx = (float)platx; //new plat x, it's what the sprite's x becomes
			if(platwidth == 256){
				npx += 64;
			} else if(platwidth == 64){
				npx -= 32;
			}
			platSprite.setPosition(npx,(float)platy);
			batch.begin();
			
			//draws bricks
			for(int i = bricks.size()-1; i >= 0; i--){
				Brick b = bricks.get(i);
				batch.draw(brickSprites[b.getDurability()],(float)b.getX(),(float)b.getY());
				//draws effects on bricks that have them
				if(b.getEffect().equals("balls")){
					batch.draw(ballsEffectSym, (float)b.getX(), (float)b.getY());
				} else if(b.getEffect().equals("life")){
					batch.draw(lifeEffectSym,(float)b.getX(), (float)b.getY());
				} else if(b.getEffect().equals("big")){
					batch.draw(bigEffectSym, (float)b.getX(), (float)b.getY());
				} else if(b.getEffect().equals("small")){
					batch.draw(smallEffectSym, (float)b.getX(), (float)b.getY());
				}
			}
			
			//draws ball
			ballSprite.draw(batch);
			//draws extra balls
			for(Ball b: extraBalls){
				Sprite bs = new Sprite(ballSprite.getTexture());
				bs.setPosition((float)b.getX(), (float)b.getY());
				bs.draw(batch);
			}
			//draws platform
			platSprite.draw(batch);
			
			//draws lives
			font.draw(batch, "Lives: " + lives, 5, Gdx.graphics.getHeight()-16);
			if(time < 120){
				font.draw(batch, "Level " + level, Gdx.graphics.getWidth()/2-16, Gdx.graphics.getHeight()/2);
			}
			batch.end();
			if(onPlat){
				//if ball on platform adjust angle
				changeAngle();
				//draws line
				drawGuideLine();
			}
			//removes all dead bricks
			for(int i = bricks.size()-1;i >= 0;i--){
				if(bricks.get(i).isGone()){
					bricks.remove(i);
				}
			}
			time++;
			//handles level success
			if(bricks.size() <= quota){
				level++;
				time = 0;
				drawLevel();
				onPlat = true;
				platx = Gdx.graphics.getWidth()/2-platwidth/2;
				platRect.x = (float)platx;
				ball.setX(platx+platwidth/2 - 8);
				ball.setY(16);
				ball.setAngle(90);
				extraBalls = new ArrayList<Ball>();
			}
		}
	}
	
	//user interaction methods
	public float getMx(){
		return Gdx.input.getX();
	}
	public float getMy(){
		return Gdx.graphics.getHeight() - Gdx.input.getY();
	}
	public String checkClick(){
		//returns which button is clicked on menu
		if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
			if(playRect.contains(getMx(),getMy())){
				return "play";
			}
		}
		return "";
	}
	
	public void startGame(){
		//starts game
		mode = "game";
		ball = new Ball(Gdx.graphics.getWidth()/2-8,16,90);
		ballSprite = new Sprite(new Texture("Ball.png"));
		platSprite = new Sprite(new Texture("Platform.png"));
		platwidth = platSprite.getWidth();
		platheight = 16;
		platx = Gdx.graphics.getWidth()/2-platwidth/2;
		platy = 0;
		level = 1;
		
		//sets rectangle of platform
		platRect = new Rectangle((float)platx,(float)platy,(float)platwidth,(float)platheight);
		
		bricks = new ArrayList<Brick>();
		extraBalls = new ArrayList<Ball>();
		onPlat = true;
		savePoint = -1;
		time = 0;
		lives = 3; //3 lives
		spd = 5;
		quota = 0;
		drawLevel(); //adds all bricks
	}
	
	//DRAWING METHODS
	public void drawLevel(){
		//although it's called drawLevel(), all it really does is add bricks :/
		if(level == 1){
			//draws level 1
			//add red bricks on bottom
			for(int i = 0;i < 15;i++){
				if(i == 13){
					bricks.add(new Brick(i*66+2,320,64,32,1,"balls"));
				} else if(i == 5){
					bricks.add(new Brick(i*66+2,320,64,32,1,"big"));
				} else{
					bricks.add(new Brick(i*66+2,320,64,32,1));
				}
			}
			//add stronger bricks on 2nd layer
			for(int i = 0; i < 14;i++){
				if(i == 3){
					bricks.add(new Brick(i*66+33,384,64,32,2,"balls"));
				} else{
					bricks.add(new Brick(i*66+33,384,64,32,2));	
				}
			}
			//adds stronger bricks on top
			for(int i = 0;i < 15;i++){
				if(i == 8){
					bricks.add(new Brick(i*66+2,468,64,32,3,"life"));
				} else if(i == 3 || i == 9){
					bricks.add(new Brick(i*66+2,468,64,32,3,"big"));
				}	else{
					bricks.add(new Brick(i*66+2,468,64,32,3));
				}
			}
			quota = 0;
		}
		else if(level == 2){
			//draws level 2
			//add red bricks on bottom
			for(int i = 0;i < 15;i++){
				if(i == 12 || i == 3){
					bricks.add(new Brick(i*66+2,320,64,32,1,"balls"));
				} else{
					bricks.add(new Brick(i*66+2,320,64,32,1));
				}
			}
			//add stronger bricks on 2nd layer
			for(int i = 0; i < 14;i++){
				if(i == 2 || i == 8){
					bricks.add(new Brick(i*66+33,384,64,32,2,"big"));
				} else{
					bricks.add(new Brick(i*66+33,384,64,32,2));
				}
			}
			//adds stronger bricks on top
			for(int i = 0;i < 15;i++){
				if(i == 5){
					bricks.add(new Brick(i*66+2,468,64,32,3,"small"));
				} else if(i == 10){
					bricks.add(new Brick(i*66+2,468,64,32,3,"balls"));
				}	else{
					bricks.add(new Brick(i*66+2,468,64,32,3));
				}
			}
			for(int i = 0;i < 14;i++){
				if(i == 11){
					bricks.add(new Brick(i*66+33,552,64,32,4,"life"));
				} else{
					bricks.add(new Brick(i*66+33,552,64,32,4));
				}
			}
			quota = 0;
		} else{
			mode = "victory";
			savePoint = -1; //resets savePoint so victory can use it again
		}
	}
	
	public void drawGuideLine(){
		//draws guideline for directing the ball
		float x1,x2,y1,y2;
		x1 = (float)ball.getX()+8;
		y1 = (float)ball.getY()+8;
		x2 = x1 + 20*(float)Math.cos(ball.getAngle()*Math.PI/180);
		y2 = y1 + 20*(float)Math.sin(ball.getAngle()*Math.PI/180);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(new Color(0,0,0,1));
		shapeRenderer.line(x1,y1,x2,y2);
		shapeRenderer.end();
	}
	
	//POSITION AFFECTING METHODS
	public void changeAngle(){
		//changes angle based on arrow keys
		if(Gdx.input.isKeyPressed(Keys.LEFT)){
			ball.setAngle(ball.getAngle()+3);
		}
		if(Gdx.input.isKeyPressed(Keys.RIGHT)){
			ball.setAngle(ball.getAngle()-3);
		}
		if(ball.getAngle() > 160){
			ball.setAngle(160);
		}
		if(ball.getAngle() < 20){
			ball.setAngle(20);
		}
	}

	public void movePlatform(){
		//moves the platform based on input
		if(Gdx.input.isKeyPressed(Keys.LEFT)){
			platx -= 7;
		}
		if(Gdx.input.isKeyPressed(Keys.RIGHT)){
			platx += 7;
		}
		if(platx < 0) platx = 0;
		if(platx > Gdx.graphics.getWidth() - platwidth) platx = Gdx.graphics.getWidth()-platwidth;
		platRect.x = (float)platx;
	}
	
	public void bounce(Ball ball){
		//bounces on bricks or platform or walls
		//WALLS
		if(ball.getX() + Ball.WIDTH >= Gdx.graphics.getWidth() || ball.getX() <= 0){
			ball.move(-spd);
			ball.setAngle(180 - ball.getAngle()+1);
		} else if(ball.getY() + Ball.HEIGHT >= Gdx.graphics.getHeight()){
			ball.move(-spd);
			ball.setAngle(-ball.getAngle()+1); //the plus one is to prevent an infinite bounce loop
		}
		//PLATFORM
		else if(platRect.overlaps(ball.getRect())){
			ball.setAngle(120-120*(ball.getX()-platx)/platwidth+30); //the angle is based on where the ball hits the paddle
		}
		//BRICKS
		for(int i = 0;i < bricks.size();i++){
			Brick b = bricks.get(i);
			if(b.checkCollision(ball)){
				if(!b.isXBounce(ball)){
					//vertical bounce
					ball.setAngle(-ball.getAngle()+1);
				} else{
					//horizontal bounce
					ball.setAngle(180-ball.getAngle()+1);
				}
				//handles effects
				if(b.getEffect().equals("balls")){
					//adds balls to list
					for(int j = 0;j < 3;j++)
						extraBalls.add(new Ball(platx+platwidth/2-8,platy+18,(float)(Math.random()*140)+20));
				} else if(b.getEffect().equals("life")){
					lives++;
				} else if(b.getEffect().equals("big")){
					platwidth = 256; //double the width
					platSprite.setScale(2,1);
					savePoint = time;
					if(platx + platwidth > Gdx.graphics.getWidth())
						platx = Gdx.graphics.getWidth() - platwidth; //prevents flowing out from the edge
				} else if(b.getEffect().equals("small")){
					platwidth = 64; //halve the width
					platSprite.setScale((float)0.5,1);
					savePoint = time;
				}
				if(!b.getEffect().equals("")){
					b.setEffect("");
				}
				break;
			}
		}
	}
	
	//GAMEPLAY METHODS
	public void checkLose(){
		//checks if we lose a life
		if(ball.getY() + Ball.HEIGHT <= 0){
			if(extraBalls.size() == 0){
				//no extra balls we lose a life and let user shoot again
				lives--;
				onPlat = true;
				ball.setX(platx+platwidth/2-8);
				ball.setY(16);
				ball.setAngle(90);
			} else{
				//otherwise we just set the ball to one of the available extra balls and remove it from the list
				ball = extraBalls.get(0);
				extraBalls.remove(0);
			}
		}
		for(int i = extraBalls.size()-1;i>=0;i--){
			if(extraBalls.get(i).getY() + Ball.HEIGHT <= 0){
				extraBalls.remove(i);
			}
		}
		if(lives <= 0){
			mode = "dead";
			savePoint = -1; //resets savePoint so dead mode can handle time skip to restart game
		}
	}
	
	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		if(onPlat){
			if(keycode == Keys.SPACE){
				onPlat = false;
			}
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
}
