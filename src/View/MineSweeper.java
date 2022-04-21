package View;

import Models.MineSweeperTile;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.stage.Screen;
import static Utils.GUESS_STATUS.FLAGGED;
import static Utils.GUESS_STATUS.GUESSED;
import static Utils.GUESS_STATUS.UNGUESSED;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import Controllers.MineSweeperController;

@SuppressWarnings("deprecation")
public class MineSweeper extends Application implements Observer {

    // game constants
    public static final int ROWS = 15, COLS = 30;
    public static final int NUM_BOMBS = 60; // i have no clue if this is too many

    // gui constants
    private static final double SCREEN_WIDTH = Screen.getPrimary().getVisualBounds().getWidth();
    private static final double SCREEN_HEIGHT = Screen.getPrimary().getVisualBounds().getHeight();
    
    private static final double HEX_RADIUS = Math.min(SCREEN_HEIGHT/(ROWS*2), 30), HEX_SIZE = Math.sqrt(HEX_RADIUS * HEX_RADIUS * 0.75);
    private static final int
            SCENE_WIDTH = (int) (1.75*(COLS + 2) * HEX_RADIUS),
            SCENE_HEIGHT = (int) (1.5*(ROWS + 2) * HEX_RADIUS);
    private static final double HEX_HEIGHT = 2* HEX_RADIUS, HEX_WIDTH = 2*HEX_SIZE;
    private static final double MAIN_FONT_SIZE = HEX_HEIGHT/2.5;
    private static final Font MAIN_FONT = new Font("Helvetica", MAIN_FONT_SIZE);
    private static final double
            LABEL_OFFSETX = HEX_WIDTH/2.5 - MAIN_FONT_SIZE/6,
            LABEL_OFFSETY = HEX_HEIGHT/6 - MAIN_FONT_SIZE/2.5;
    private static final HashMap<Integer, Color> MINE_COUNT_TO_COLOR = new HashMap<>();


    // gui variables
    private Hexagon[][] rectGrid;
    private Label[][] labelGrid;
    private AnchorPane gridPane;
    
    // controller variable
    private MineSweeperController controller;

    public static void main(String[] args) {
        // filling hashmap, this only needs to be done once
        MINE_COUNT_TO_COLOR.put(1, Color.rgb(207, 236, 207));
        MINE_COUNT_TO_COLOR.put(2, Color.rgb(204, 236, 239));
        MINE_COUNT_TO_COLOR.put(3, Color.rgb(221, 212, 232));
        MINE_COUNT_TO_COLOR.put(4, Color.rgb(253, 222, 238));
        MINE_COUNT_TO_COLOR.put(5, Color.rgb(253, 202, 162));
        MINE_COUNT_TO_COLOR.put(6, Color.rgb(255, 105, 97));
        launch(args);
    }

    @Override
    public void start(Stage stage) {
    	// initialize game
        controller = new MineSweeperController();
        controller.addObserver(this); // add as observer for model (MineSweeperBoard)
        rectGrid = new Hexagon[ROWS][COLS];
        labelGrid = new Label[ROWS][COLS];
        gridPane = new AnchorPane();

        // creates the initial blank board
        createBoard();

        BorderPane pane = new BorderPane();
        pane.setCenter(gridPane);
        Scene scene = new Scene(pane, SCENE_WIDTH, SCENE_HEIGHT);
        stage.setScene(scene);
        stage.setTitle("Mine Sweeper");
        stage.show();
    }


    /**
     *  Creates the blank game board of ROW x COL hexagons
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
        double xCoord = (col+1) * HEX_WIDTH + ((row % 2) * HEX_SIZE);
        Hexagon hex = new Hexagon(xCoord, yCoord);
        hex.setFill(UNGUESSED.getColor());

        Label label = new Label("");
        label.setFont(MAIN_FONT);
        label.setTranslateX(xCoord + LABEL_OFFSETX);
        label.setTranslateY(yCoord + LABEL_OFFSETY);

        // TODO: compact these into the same event
        hex.setOnMousePressed(e -> {
            if (e.getClickCount() == 2) {
                controller.updateTilesAround(row, col);
            } else if (e.isPrimaryButtonDown()) {
    			controller.updateTileStatus(row, col, GUESSED);
            } else if (e.isSecondaryButtonDown()) {
    			controller.updateTileStatus(row, col, FLAGGED);
            }
        });

        label.setOnMousePressed(e -> {
            if (e.getClickCount() == 2) {
                controller.updateTilesAround(row, col);
            } else if (e.isPrimaryButtonDown()) {
                controller.updateTileStatus(row, col, GUESSED);
            } else if (e.isSecondaryButtonDown()) {
                controller.updateTileStatus(row, col, FLAGGED);
            }
        });

        // adding it to the grids and groups
        rectGrid[row][col] = hex;
        labelGrid[row][col] = label;
        gridPane.getChildren().add(hex);
        gridPane.getChildren().add(label);
    }

    /**
     *
     * @param o     the model
     * @param arg   the MineSweeperTile[][] board from the model
     */
    public void update(Observable o, Object arg) {
        if (controller.isGameOver())  {// checks with Controller if game is over
            displayGameOver(); // calls the method to display the game over msg if true
            return;
        }

        // if the game isn't over, all the tiles are updated according to their enum
        MineSweeperTile[][] board = (MineSweeperTile[][]) arg;
        for (int row = 0; row < ROWS; row++)
            for (int col = 0; col < COLS; col++) {

                rectGrid[row][col].setFill(board[row][col].getStatus().getColor());

                // Reveals minecount of any guessed tiles
                if (board[row][col].getMineCount() > 0 && board[row][col].getStatus().equals(GUESSED)) {
                    labelGrid[row][col].setText(""+board[row][col].getMineCount());
                    rectGrid[row][col].setFill(MINE_COUNT_TO_COLOR.get(board[row][col].getMineCount()));
                }
                if (board[row][col].getStatus().getColor() == Color.WHITE) {
                	animateTiles(row, col);
        		}
                
                if (board[row][col].getStatus().getColor() == Color.BLACK) {
                	animateBombs(row, col);
                }
            }
	}

	private void animateBombs(int row, int col) {
		rectGrid[row][col].toFront();
    	ScaleTransition big = new ScaleTransition(Duration.millis(400), rectGrid[row][col]);
    	RotateTransition rt = new RotateTransition(Duration.millis(1000), rectGrid[row][col]);
        rt.setByAngle(360);
        rt.setCycleCount(1);
        big.setByX(.2f);
    	big.setByY(.2f);
    	big.setCycleCount(1);
    	rectGrid[row][col].toFront();
    	big.play();
    	rt.play();
	}

	private void animateTiles(int row, int col) {
		ScaleTransition big = new ScaleTransition(Duration.millis(300), rectGrid[row][col]);
    	big.setByX(.1f);
    	big.setByY(.1f);
    	big.setAutoReverse(true);
    	big.setCycleCount(2);
    	big.play();	
	}

	/**
     * This method displays the game over message in the middle of the board
     * when the game is over.
     */
    public void displayGameOver() {
    	String[] msg = "YOU WIN!!".split("");
    	if (!controller.win()) // checks with the controller if the player didn't win
    		msg = "YOU LOSE!!".split("");

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                labelGrid[r][c].setText(""); // clear the text
            }
        }

        // creating lose message
        int row = ROWS/2;
        int i = 0;
        for (int col = (COLS/4); col < COLS*((double)3/4); col++) {
            rectGrid[row][col].setFill(Color.WHITE);
            if (i >= 0 && i < msg.length) {
            	labelGrid[row][col].setText(msg[i]);
            	labelGrid[row][col].toFront();
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