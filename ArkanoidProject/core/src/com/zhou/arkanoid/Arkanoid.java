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
	public static final int NUM_LEVELS = 2;
	
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
	Texture fastEffectSym;
	Texture slowEffectSym;
	Texture[] backgrounds;
	Texture pausedSign;
	Texture vicScreen;
	Texture deadScreen;
	Sprite ballSprite;
	Sprite platSprite;
	
	double platx,platy,platwidth,platheight; //platform x and y and width and height
	Rectangle platRect;
	
	boolean onPlat; //is ball sitting on the platform?
	boolean paused; //is game paused?
	
	int lives;
	int level;
	int time; //time elapsed in ticks
	int savePoint; //savePoint is a counter for time, for time based events
	int quota; //amount of bricks that need to be left at most to win
	int numBricks; //number of bricks the level had originally
	
	String mode; //which mode the game is in (menu, game, dead, victory)
	
	Music music;
	Sound brickHit;
	
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
		//SOUND FX
		brickHit = Gdx.audio.newSound(Gdx.files.internal("BrickHit.mp3"));
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
		fastEffectSym = new Texture("fastSymbol.png");
		slowEffectSym = new Texture("slowSymbol.png");
		pausedSign = new Texture("pausedSign.png");
		backgrounds = new Texture[NUM_LEVELS];
		for(int i = 0;i < NUM_LEVELS;i++){
			backgrounds[i] = new Texture("level"+(i+1)+"Bg.png");
		}
		vicScreen = new Texture("victoryScreen.png");
		deadScreen = new Texture("deadScreen.jpg");
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
			if(buttonClicked.equals("play")){ //if the user clicks play
				startGame();
			}
		} if(mode.equals("victory")){
			//handles victory screen
			Gdx.gl.glClearColor(0, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			batch.begin();
			batch.draw(vicScreen,0,-384); //draws the victory screen
			batch.end();
			if(savePoint < 0) //sets savePoint to when the victory screen starts
				savePoint = time;
			if(time >= savePoint + 240){ //after 240 ticks, we restart program to let user play again
				create();
			}
			time++;
		} else if(mode.equals("dead")){
			//handles defeat screen
			Gdx.gl.glClearColor((float)0.7, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			batch.begin();
			batch.draw(deadScreen,0,-384); //draws the dead screen
			batch.end();
			if(savePoint < 0) //sets the savePoint to when the dead screen starts 
				savePoint = time;
			if(time >= savePoint + 240){ //after 240 ticks, we restart the program to allow user to play again
				create();
			}
			time++;
		} else if(mode.equals("game")){
			Gdx.gl.glClearColor((float)0.3, (float)0.3, (float)0.3, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			drawBackground(); //draws background for level
			if(paused){
				//if paused we just draw a game state and put the paused sign
				drawGameState();
				batch.begin();
				batch.draw(pausedSign, Gdx.graphics.getWidth()/2-128, Gdx.graphics.getHeight()/2 - 64);
				batch.end();
			} else{
				gameLoop(); //runs main game loop that handles everything gameplay related
			}
		} else if(mode.equals("nextLevel")){
			//moves on to next level after a bit
			drawBackground();
			drawGameState();
			if(time - savePoint > 240){
				savePoint = time;
			}
			if(time - savePoint == 240){
				startNextLevel();
			}
			time++;
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
	
	//GAMEPLAY METHODS
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
		quota = 0;
		paused = false;
		createLevel(); //adds all bricks
	}
	public void startNextLevel(){
		//starts next level
		bricks.clear(); //resets bricks
		extraBalls = new ArrayList<Ball>(); //resets balls
		ball = new Ball(Gdx.graphics.getWidth()/2-8,16,90);
		level++;
		time = 0;
		onPlat = true;
		platwidth = 128;
		platRect.width = (float)platwidth;
		platx = Gdx.graphics.getWidth()/2-platwidth/2;
		platRect.x = (float)platx;
		platSprite.setScale(1,1);
		savePoint = -1;
		mode = "game";
		createLevel();
	}

	public void gameLoop(){
		if(!onPlat){
			//handles user moving platform and ball movement
			ball.normalizeSpeed(time);
			for(Ball b: extraBalls){
				b.normalizeSpeed(time);
			}
			ball.move();
			for(int i = extraBalls.size()-1;i>=0;i--){
				Ball b = extraBalls.get(i);
				b.move();
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
		drawGameState(); //draws current game state
		//removes all dead bricks
		for(int i = bricks.size()-1;i >= 0;i--){
			if(bricks.get(i).isGone()){
				bricks.remove(i);
			}
		}
		time++;
		//handles level success
		if(bricks.size() <= quota){
			mode = "nextLevel"; //sets mode to next Level mode
		}
	}
	//DRAWING METHODS
	public void drawGameState(){
		//draws the game state, which is the bricks, ball and platform
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
			} else if(b.getEffect().equals("fast")){
				batch.draw(fastEffectSym, (float)b.getX(), (float)b.getY());
			} else if(b.getEffect().equals("slow")){
				batch.draw(slowEffectSym, (float)b.getX(), (float)b.getY());
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
		
		//draws text saying lives
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
	}
	public void createLevel(){
		//creates a level by adding bricks
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
				} else if(i == 8){
					bricks.add(new Brick(i*66+33,384,64,32,2,"fast"));
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
				} else if(i == 7){
					bricks.add(new Brick(i*66+2,468,64,32,3,"fast"));
				} else if(i == 0 || i == 12){
					bricks.add(new Brick(i*66+2,468,64,32,3,"balls"));
				} else{
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
				} else if(i == 5 || i == 1){
					bricks.add(new Brick(i*66+33,384,64,32,2,"fast"));
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
				} else if(i == 0 || i == 8){
					bricks.add(new Brick(i*66+2,468,64,32,3,"slow"));
				} else{
					bricks.add(new Brick(i*66+2,468,64,32,3));
				}
			}
			for(int i = 0;i < 14;i++){
				if(i == 11){
					bricks.add(new Brick(i*66+33,552,64,32,4,"life"));
				} else if(i == 3){
					bricks.add(new Brick(i*66+33,552,64,32,4,"small"));
				} else{
					bricks.add(new Brick(i*66+33,552,64,32,4));
				}
			}
			quota = 0;
		} else{
			mode = "victory";
			savePoint = -1; //resets savePoint so victory can use it again
		}
		numBricks = bricks.size(); //number of bricks level had originally
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
		shapeRenderer.setColor(new Color(1,1,1,1));
		shapeRenderer.line(x1+1, y1+1, x2+1, y2+1); //draws a white line to avoid invisibility
		shapeRenderer.end();
	}
	public void drawBackground(){
		if(numBricks - quota <= 0)
			numBricks = quota+1; //prevents crashing
		float bgBase = Gdx.graphics.getHeight() - Gdx.graphics.getHeight()*(bricks.size()-quota)/(numBricks-quota);

		batch.begin();
		batch.draw(backgrounds[level-1],0,-384);//background of level
		batch.end();
		shapeRenderer.begin(ShapeType.Filled);
		//draws a background that gets slowly goes up to reveal everything
		shapeRenderer.setColor(new Color((float)0.7,(float)0.7,(float)0.7,1));
		shapeRenderer.rect(0, bgBase, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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
			platx -= ball.getSpeed()+2;
		}
		if(Gdx.input.isKeyPressed(Keys.RIGHT)){
			platx += ball.getSpeed()+2;
		}
		if(platx < 0) platx = 0;
		if(platx > Gdx.graphics.getWidth() - platwidth) platx = Gdx.graphics.getWidth()-platwidth;
		platRect.x = (float)platx;
	}
	
	public void bounce(Ball ball){
		//bounces on bricks or platform or walls
		//WALLS
		if(ball.getX() + Ball.WIDTH >= Gdx.graphics.getWidth() || ball.getX() <= 0){
			ball.move(-1);
			ball.setAngle(180 - ball.getAngle()+1);
		} else if(ball.getY() + Ball.HEIGHT >= Gdx.graphics.getHeight()){
			ball.move(-1);
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
				brickHit.play(); //plays the brickhit
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
						extraBalls.add(new Ball(platx+platwidth/2-8,platy+18,(float)(Math.random()*140)+20,ball.getSpeed(),ball.getSpeedCounter()));
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
				} else if(b.getEffect().equals("fast")){
					ball.setSpeed(7);
					ball.setSpeedCounter(time);
					for(Ball bl: extraBalls){
						bl.setSpeed(7);
						bl.setSpeedCounter(time);
					}
				} else if(b.getEffect().equals("slow")){
					ball.setSpeed(3);
					ball.setSpeedCounter(time);
					for(Ball bl: extraBalls){
						bl.setSpeed(3);
						bl.setSpeedCounter(time);
					}
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
		if(mode.equals("game")){
			if(onPlat){
				if(keycode == Keys.SPACE){
					onPlat = false;
				}
			}
			if(keycode == Keys.P){
				paused = !paused;
			}
		}
		//CHEATS
		if(keycode == Keys.L){
			bricks.clear();
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
