package View;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
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
    private static final double HEX_RADIUS = 30, HEX_SIZE = Math.sqrt(HEX_RADIUS * HEX_RADIUS * 0.75);
    private static final int SCENE_WIDTH = (int) (1.75*(COLS + 2) * HEX_RADIUS),
                SCENE_HEIGHT = (int) (1.5*(ROWS + 2) * HEX_RADIUS);
    private static final double HEX_HEIGHT = 2* HEX_RADIUS, HEX_WIDTH = 2*HEX_SIZE;
    private static final double MAIN_FONT_SIZE = HEX_HEIGHT/2.5;
    private static final Font MAIN_FONT = new Font("Helvetica", MAIN_FONT_SIZE);
    private static final double LABEL_OFFSETX = HEX_WIDTH/2.5 - MAIN_FONT_SIZE/6, LABEL_OFFSETY = HEX_HEIGHT/6 - MAIN_FONT_SIZE/2.5;


    // gui variables
    private Hexagon[][] rectGrid;
    private AnchorPane gridPane;
    
    // controller variable
    private MineSweeperController controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
    	// initialize controller
        controller = new MineSweeperController(NUM_BOMBS);
    	// add as observer for model (MineSweeperBoard)
        controller.addObserver(this);
        rectGrid = new Hexagon[ROWS][COLS];
        gridPane = new AnchorPane();

        // creates the initial blank board
        createBoard();

        BorderPane pane = new BorderPane();
        pane.setCenter(gridPane);
        Scene scene = new Scene(pane, SCENE_WIDTH, SCENE_HEIGHT);
        stage.setScene(scene);
        stage.setTitle("Mine Sweeper");
        stage.show();
        
        // create instance of MouseHandler
//        scene.setOnMousePressed(new MouseHandler());
    }


    /**
     *  Creates the blank game board of 20 x 20 rectangles
     */
    public void createBoard() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                addHex(row, col);
            }
        }
    }

    /**
     * Creates a new rectangle object at the specified row and col
     * 
     * @param row is the y coord
     * @param col is the x coord
     */
    private void addHex(int row, int col) {
        double yCoord = (row+1) * HEX_HEIGHT * 0.75;
        double xCoord = (col+1) * HEX_WIDTH + (row %2) * HEX_SIZE;
        Hexagon hex = new Hexagon(xCoord, yCoord);
        hex.setFill(UNGUESSED.getColor());

        Label label = new Label("1");
        label.setFont(MAIN_FONT);
        label.setTranslateX(xCoord + LABEL_OFFSETX);
        label.setTranslateY(yCoord + LABEL_OFFSETY);

        hex.setOnMousePressed(e -> {
            if (e.isPrimaryButtonDown())
                controller.updateTileStatus(row, col, GUESSED);

			else if (e.isSecondaryButtonDown())
                controller.updateTileStatus(row, col, FLAGGED);

        });

        rectGrid[row][col] = hex;
        gridPane.getChildren().add(hex);
        gridPane.getChildren().add(label);
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
    	if (controller.isGameOver())  // checks with Controller if game is over
    		displayGameOver(); // calls the method to display the game over msg if true

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
    	if (!controller.win()) // checks with the controller if the player didn't win
    		msg = "YOU LOSE!".split("");
		int row = COLS/2;
		int i = -1;

        for (int col = (COLS/4); col < COLS*((double)3/4); col++) {
            rectGrid[row][col].setFill(Color.WHITE);
            StackPane stackPane = new StackPane(); 
            stackPane.setTranslateX(col * HEX_SIZE);
            stackPane.setTranslateY(row * HEX_SIZE);

            if (i >= 0 && i < msg.length) {
            	Text text = new Text(msg[i]); 
            	text.setFont(new Font(20));
            	text.setTextAlignment(TextAlignment.CENTER);
                stackPane.getChildren().add(text); 
                gridPane.getChildren().add(stackPane);
                text.toFront();
            }

            i++;
        }
    }

    /**
     * This inner class creates a hexagon which can be places on the board with an x and y position
     * We calculate this x and y position inside the addHex method
     * This generates a new polygon with the needed hex points
     */
    private static class Hexagon extends Polygon {
        Hexagon(double x, double y) {
            // creates the polygon using the corner coordinates
            getPoints().addAll(
                    x, y,
                    x, y + HEX_RADIUS,
                    x + HEX_SIZE, y + HEX_RADIUS * 1.5,
                    x + HEX_WIDTH, y + HEX_RADIUS,
                    x + HEX_WIDTH, y,
                    x + HEX_SIZE, y - HEX_RADIUS * 0.5
            );

            setStroke(Color.BLACK);
        }
    }
}


