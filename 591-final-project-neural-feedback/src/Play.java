import java.util.ArrayList;
import java.util.Random;

import org.newdawn.slick.*;
import org.newdawn.slick.state.*;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;
import org.newdawn.slick.TrueTypeFont;
import java.awt.Font;

public class Play extends BasicGameState {
	boolean bUsingData;
	boolean bDrawLabel;
	int numSuccess;
	int numFails;
	int actualMovesIndex;
	long lastMoveTime;
	long currTime;
	Balloon balloon;
	Sky sky1;
	Sky sky2;
	Label label;
	Random rand;
	DataProcessor dp;
	InputProcessor ip;
	InputReader ir;
	TrueTypeFont ttf;
	FmriClassification cl;
	ArrayList<String> actualMoves;

	/**
	 * Game Play constructor
	 * 
	 * @param state the integer representing the state of the game
	 */
	public Play(int state) {
		;
	}

	/**
	 * init class to initialized all objects and parameters needed for render() and
	 * update()
	 */
	@Override
	public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
		// processor classes
		rand = new Random();
		dp = new DataProcessor();
		ip = new InputProcessor();
		ir = new InputReader();
		cl = new FmriClassification();
		// sprite classes
		balloon = new Balloon((SetupGame.SCREEN_X / 2 - 50), SetupGame.SCREEN_Y / 2, 0.3f, 0.5f, new Image("sprites/balloon-3.png"));
		sky1 = new Sky(0, (-SetupGame.SKY_DIMENSION_Y + SetupGame.SCREEN_Y));
		sky2 = new Sky(0, ((-SetupGame.SKY_DIMENSION_Y * 2) + SetupGame.SCREEN_Y));
		label = new Label();
		lastMoveTime = 0;
		actualMoves = new ArrayList<>();
		
		// init variables
		ip.bInputSuccess = false;
		bUsingData = false;
		bDrawLabel = false;
		
		actualMoves = cl.getActualMoves();
		actualMovesIndex = 0;
		Font font = new Font("Helvetica", Font.BOLD, 30);
		ttf = new TrueTypeFont(font, true);
	}

	/**
	 * Displays balloon and sky with different instructions and if the player
	 * followed instruction based on the update method. the update() and render()
	 * are run for each frame
	 */
	@Override
	public void render(GameContainer gc, StateBasedGame arg1, Graphics g) throws SlickException {
		// TODO Auto-generated method stub
		Image imgSky = new Image("sprites/bg-sky-vert-1.png");
		Image imgSky2 = new Image("sprites/bg-sky-vert-2.png");
		
		
		// balloon and sky positions
		imgSky.draw(sky1.x, sky1.y);
		imgSky2.draw(sky2.x, sky2.y);
		balloon.img.draw(balloon.x, balloon.y, balloon.scale);
	
		// create labels
		if ((currTime - label.lastLabelDrawTime) > 1400) {
			if (label.bDraw) {
				label.getLabelImage().draw(125, (SetupGame.SCREEN_Y - 225));
				label.bVisible = true;
			} else {
				label.bVisible = false;
			}
		}
		
		// write text
		g.setFont(ttf);
		
		// Displays good job if it the player made the right move, wrong move otherwise
		if ((currTime - lastMoveTime) < 1000) {
			if (dp.isRightMove()) {
				g.drawString("GOOD JOB!", (SetupGame.SCREEN_X / 2 - 100), (SetupGame.SCREEN_Y / 2 - 50));
			} else if (!dp.isRightMove()) {
				g.drawString("WRONG MOVE.", (SetupGame.SCREEN_X / 2 - 100), (SetupGame.SCREEN_Y / 2 - 50));
			}
		}
	
		g.drawString("Elevation: " + balloon.elevation, SetupGame.SCREEN_X - 250, 0);
	}

	/**
	 * Update method to check if there is data for input, if there is then make the
	 * balloon move up if the player made a right move (comparing predicted moves to
	 * actual moves). If there is no data, player can play by pressing w.
	 */
	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int arg2) throws SlickException {
		// input reader class used here in case of no data
		actualMovesIndex = dp.getArrayIndex();
		if (actualMovesIndex < 72) {
			ir.updateGameInput(gc.getInput());
			currTime = System.currentTimeMillis();
			// process input
			if (dp.isUsingData()) {
				while ((currTime - lastMoveTime) > 2432) {

					actualMovesIndex = dp.getArrayIndex();
					lastMoveTime = currTime;
					
					// should label be drawn
					try {
						label.bDraw = true;
						label.setLabelDrawInfo(actualMoves.get(actualMovesIndex), currTime);
						
						ip.updateInputData(dp.getData(), currTime);
					} catch (IndexOutOfBoundsException e) {
						System.out.println("Play.update IndexOutOfBoundsException");
					}
				}
			} else {
				ip.updateInputKeyboard(currTime);
			}
			// update sky speed
			int modSkySpeed = 1;
			if (ip.isInputProcessing(currTime)) {
				modSkySpeed = 6;
			}
			// update balloon elevation
			if (currTime - balloon.elevationTimer > (480 / modSkySpeed)) {
				balloon.elevationTimer = currTime;
				balloon.elevation += 1;
			}
			// move sky
			sky1.scroll(modSkySpeed);
			sky2.scroll(modSkySpeed);
		}
		else if (actualMovesIndex >= 72){
			sbg.enterState(2, new FadeOutTransition(), new FadeInTransition());
		}
	}

	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return 1;
	}

}
