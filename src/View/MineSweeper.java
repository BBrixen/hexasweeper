package View;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import Models.MineSweeperTile;

import static Utils.GUESS_STATUS.FLAGGED;
import static Utils.GUESS_STATUS.GUESSED;
import static Utils.GUESS_STATUS.UNGUESSED;

import java.util.Observable;
import java.util.Observer;

import Controllers.MineSweeperController;

@SuppressWarnings("deprecation")
public class MineSweeper extends Application implements Observer {

    // game constants
    private static final int ROWS = 20, COLS = 20;
    private static final int NUM_BOMBS = 80; // i have no clue if this is too many

    // gui constants
    private static final int GRID_SIZE = 40;
    private static final int RECTS_SIZE = (int) (0.9 * GRID_SIZE);
    private static final int SCENE_WIDTH = (COLS + 2) * GRID_SIZE, SCENE_HEIGHT = (ROWS + 2) * GRID_SIZE;

    // gui variables
    private Rectangle[][] rectGrid;
    private Group gridGroup;
    
    // controller variable
    private MineSweeperController c;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
    	// initialize controller
    	c = new MineSweeperController(NUM_BOMBS);
    	// add as observer for model (MineSweeperBoard)
    	c.addObserver(this);
        rectGrid = new Rectangle[ROWS][COLS];
        gridGroup = new Group();

        // creates the initial blank board
        createBoard();

        BorderPane pane = new BorderPane();
        pane.setCenter(gridGroup);
        Scene scene = new Scene(pane, SCENE_WIDTH, SCENE_HEIGHT);
        stage.setScene(scene);
        stage.setTitle("Mine Sweeper");
        stage.show();
        
        // create instance of MouseHandler
        scene.setOnMousePressed(new MouseHandler());
    }


    /**
     *  Creates the blank game board of 20 x 20 rectangles
     */
    public void createBoard() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                addLabel(row, col);
                addLabel(row, col);
            }
        }
    }

    /**
     * Creates a new rectangle object at the specified row and col
     * 
     * @param row is the y coord
     * @param col is the x coord
     */
    private void addLabel(int row, int col) {
        Rectangle rect = new Rectangle(RECTS_SIZE, RECTS_SIZE);
        rect.setFill(UNGUESSED.getColor());
        rect.setStyle("-fx-border-style: solid; -fx-border-width: 5; -fx-border-color: black;");
        rect.setTranslateX(col * GRID_SIZE);
        rect.setTranslateY(row * GRID_SIZE);

        rectGrid[row][col] = rect;
        gridGroup.getChildren().add(rect);
    }
    
    
    /**
     * Any time the Model calls notifyObservers(), this update()
     * method is called. 
     * This method calls the below changeBoard() method with
     * the entire game board as a parameter.
     */
    public void update(Observable o, Object arg) {
		changeBoard((MineSweeperTile[][])arg);
	}
    
    /**
     * Updates the game board with the corresponding enum colors for
     * each tile
     * 
     * @param arg is the MineSweeperTile[][] board from the Model
     */
    private void changeBoard(MineSweeperTile[][] arg) {
    	if (c.isGameOver()) { // checks with Controller if game is over
    		displayGameOver(); // calls the method to display the game over msg if true
         }
    	else // if the game isn't over, all the tiles are updated according to their enum
	    	for (int row = 0; row < ROWS; row++) 
	            for (int col = 0; col < COLS; col++) {
	                rectGrid[row][col].setFill(arg[row][col].getStatus().getColor());
	            }
	}
        
    /**
     * This method displays the game over message in the middle of the board
     * when the game is over.
     */
    public void displayGameOver() {
    	String[] msg = "YOU WIN!!".split("");
    	if (!c.win()) // checks with the controller if the player didn't win
    		msg = "YOU LOSE!".split("");
		int row = COLS/2;
		int i = -1;
        for (int col = (COLS/4); col < COLS*((double)3/4); col++) {
            rectGrid[row][col].setFill(Color.WHITE);
            StackPane stackPane = new StackPane(); 
            stackPane.setTranslateX(col * GRID_SIZE);
            stackPane.setTranslateY(row * GRID_SIZE);
            if (i >= 0 && i < msg.length) {
            	Text text = new Text(msg[i]); 
            	text.setFont(new Font(20));
            	text.setTextAlignment(TextAlignment.CENTER);
                stackPane.getChildren().add(text); 
                gridGroup.getChildren().add(stackPane);
                text.toFront();
                }
            i++;
        }
    }

	/**
     * 
     * This inner class creates MouseHandler objects that determine when a mouse button is clicked.
     *
     */
    class MouseHandler implements EventHandler<MouseEvent> {
		/**
		 * Checks if left or right mouse button clicked
		 */
		@Override
		public void handle(MouseEvent me) {
			int x = (int) (me.getX()/GRID_SIZE)-1; // casting to int rounds down, finds coord of click
			int y = (int) (me.getY()/GRID_SIZE)-1;
			if (me.isPrimaryButtonDown()) {
	            c.updateTileStatus(y, x, GUESSED);
	        }
			else if (me.isSecondaryButtonDown()) {
	            c.updateTileStatus(y, x, FLAGGED);
	        }
		}
    }
}
