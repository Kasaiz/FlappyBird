package com.mygdx.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.actions.DelayAction;
import com.badlogic.gdx.utils.Timer;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {
	SpriteBatch batch;
	Texture background;
	Texture[] bird = new Texture[2];
	int flapState = 0;
	float birdY;
	int gameState = 0;
	float velocity = 0;
	float gravity = 2;
	Texture topTube;
	Texture bottomTube;
	float gap = 400;
	Random random = new Random();
	int numberOfTube = 4;
	float[] tubetOffset = new float[numberOfTube];
	float[] tubeX = new float[numberOfTube];
	float tubeVelocity = 4;
	float distanceBetweenTubes;

	int scores = 0;
	int highScore = 0;
	int scoringTube = 0;
	BitmapFont bitmapFont;
	Texture gameOver;

	ShapeRenderer shapeRenderer;
	Circle circle;

	Rectangle[] topTubeRectangle;
	Rectangle[] bottomTubeRectangle;

	Sound dead;
	Sound fly;
	Sound ping;

	boolean death = false;

	Preferences preferences;

	@Override
	public void create () {

		preferences = Gdx.app.getPreferences("My Preferences");
		highScore = preferences.getInteger("HIGHSCORE",0);

		dead = Gdx.audio.newSound(Gdx.files.internal("Dead.mp3"));
		fly = Gdx.audio.newSound(Gdx.files.internal("Fly.mp3"));
		ping = Gdx.audio.newSound(Gdx.files.internal("Ping.mp3"));

		batch = new SpriteBatch();
		background = new Texture("bg.png");
		bird[0] = new Texture("bird.png");
		bird[1] = new Texture("bird2.png");
		birdY = Gdx.graphics.getHeight()/2-bird[flapState].getHeight()/2;
		topTube = new Texture("toptube.png");
		bottomTube = new Texture("bottomtube.png");
		gameOver = new Texture("gameOver.png");

		bitmapFont = new BitmapFont();
		bitmapFont.setColor(Color.WHITE);
		bitmapFont.getData().scale(10);

		shapeRenderer = new ShapeRenderer();
		circle = new Circle();

		topTubeRectangle = new Rectangle[numberOfTube];
		bottomTubeRectangle = new Rectangle[numberOfTube];

		distanceBetweenTubes = Gdx.graphics.getWidth()/2;
		for(int i=0;i<numberOfTube;i++){
			tubetOffset[i] = (random.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - gap - 200);
			tubeX[i] = Gdx.graphics.getWidth() + i*distanceBetweenTubes;
			topTubeRectangle[i] = new Rectangle();
			bottomTubeRectangle[i] = new Rectangle();
		}

	}

	@Override
	public void render () {

		batch.begin();
		batch.draw(background, 0 , 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		if(gameState==1){

			if(Gdx.input.justTouched()){
				//bird jump 30 distance
				fly.play();
				velocity = -30;

			}

			if(tubeX[scoringTube] < Gdx.graphics.getWidth()/2){
				ping.play();
				scores++;

				if(scoringTube<numberOfTube-1){
					scoringTube++;
				}else {
					scoringTube=0;
				}
			}

			for (int i=0;i<numberOfTube;i++){
				if(tubeX[i] < -topTube.getWidth()){
					tubeX[i] += numberOfTube*distanceBetweenTubes;
				}else {
					tubeX[i] -= tubeVelocity;
				}


				batch.draw(topTube, tubeX[i],
						Gdx.graphics.getHeight()/2 + gap/2 + tubetOffset[i]);
				batch.draw(bottomTube, tubeX[i],
						Gdx.graphics.getHeight()/2 - gap/2 - bottomTube.getHeight() + tubetOffset[i]);
				topTubeRectangle[i] = new Rectangle(tubeX[i], Gdx.graphics.getHeight()/2 + gap/2 + tubetOffset[i]+50, topTube.getWidth(), topTube.getHeight());
				bottomTubeRectangle[i] = new Rectangle(tubeX[i], Gdx.graphics.getHeight()/2 - gap/2-bottomTube.getHeight() + tubetOffset[i]-50, bottomTube.getWidth(), bottomTube.getHeight());
			}

			if(birdY>0) {
				velocity += gravity;
				//update position of bird
				birdY -= velocity;

			}else {
				if(scores>highScore){
					highScore = scores;
					preferences.putInteger("HIGHSCORE", highScore);
					preferences.flush();
				}

				dead.play();
				gameState = 2;
			}

			if(flapState==0){
				flapState = 1;
			}else {
				flapState = 0;
			}
		}else if(gameState==0) {
			if(Gdx.input.justTouched()){
				gameState = 1;
			}
		}else{
			batch.draw(gameOver, Gdx.graphics.getWidth()/2 - gameOver.getWidth()/2,
					Gdx.graphics.getHeight()/2 - gameOver.getHeight()/2);
			if(Gdx.input.justTouched()){
				death = false;
				gameState=1;
				//game Start
				birdY = Gdx.graphics.getHeight()/2-bird[flapState].getHeight()/2;

				scoringTube = 0;
				scores = 0;
				velocity = 0;

				for(int i=0;i<numberOfTube;i++){
					tubetOffset[i] = (random.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - gap - 200);
					tubeX[i] = Gdx.graphics.getWidth() + i*distanceBetweenTubes;
					topTubeRectangle[i] = new Rectangle();
					bottomTubeRectangle[i] = new Rectangle();
				}
			}
		}


		batch.draw(bird[flapState], Gdx.graphics.getWidth()/2-bird[flapState].getWidth()/2,
				birdY);

		bitmapFont.draw(batch, Integer.toString(scores), 200, 200);
		bitmapFont.draw(batch, Integer.toString(highScore), 800, 200);

		batch.end();

		circle.set(Gdx.graphics.getWidth()/2, birdY+bird[flapState].getWidth()/2, bird[flapState].getWidth()/2);
		for(int i=0;i<numberOfTube;i++){
			if(Intersector.overlaps(circle, topTubeRectangle[i]) || Intersector.overlaps(circle, bottomTubeRectangle[i])){
				if(!death) {
					if(scores>highScore){
						highScore = scores;
						preferences.putInteger("HIGHSCORE", highScore);
						preferences.flush();
					}

					dead.play();
					death = true;
				}
				gameState = 2;
			}
		}
		shapeRenderer.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		dead.dispose();
		fly.dispose();
		ping.dispose();
	}
}
