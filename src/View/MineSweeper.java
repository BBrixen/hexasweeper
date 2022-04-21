package View;

import Models.MineSweeperTile;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import static Utils.GUESS_STATUS.FLAGGED;
import static Utils.GUESS_STATUS.GUESSED;
import static Utils.GUESS_STATUS.UNGUESSED;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import Controllers.MineSweeperController;

@SuppressWarnings("deprecation")
public class MineSweeper extends Application implements Observer {

    // game constants
    public static final int ROWS = 25, COLS = 45;
    public static final int NUM_BOMBS = 200; // i have no clue if this is too many
    private static ScheduledExecutorService executor = null;
    public static final int DELTA_TIME_MS = 10;

    // gui constants
    private static final double SCREEN_WIDTH = Screen.getPrimary().getVisualBounds().getWidth();
    private static final double SCREEN_HEIGHT = Screen.getPrimary().getVisualBounds().getHeight();
    
    private static final double HEX_RADIUS = Math.min(SCREEN_HEIGHT/(ROWS*2), 30), HEX_SIZE = Math.sqrt(HEX_RADIUS * HEX_RADIUS * 0.75);
    private static final int
            SCENE_WIDTH = (int) (1.75*(COLS + 2) * HEX_RADIUS),
            SCENE_HEIGHT = (int) (1.5*(ROWS + 5) * HEX_RADIUS);
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
    private Stage stage;
    
    private VBox vBox;
    private HBox buttonRow;
    private Button saveButton;
    private Button loadButton;
    private Button resetButton;
    
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
    	this.stage = stage;
    	// initialize game
        controller = new MineSweeperController();
        controller.addObserver(this); // add as observer for model (MineSweeperBoard)
        rectGrid = new Hexagon[ROWS][COLS];
        labelGrid = new Label[ROWS][COLS];
        gridPane = new AnchorPane();

        saveButton = new Button("Save");        
        loadButton = new Button("Load");
        resetButton = new Button("Reset");
        
        setButtonActions();

        buttonRow = new HBox(15);
        buttonRow.getChildren().addAll(saveButton, loadButton, resetButton);
        buttonRow.setPadding(new Insets(5, 15, 5, 40));
        buttonRow.setAlignment(Pos.CENTER);
        
        Text timer = createTimer();
        
        vBox = new VBox(15);
        vBox.getChildren().addAll(timer, gridPane, buttonRow);
        vBox.setPadding(new Insets(10,0,0,0));
        vBox.setAlignment(Pos.CENTER);
        
        // creates the initial blank board
        createBoard(controller.getBoard());

        BorderPane pane = new BorderPane();
        pane.setCenter(vBox);
        Scene scene = new Scene(pane, SCENE_WIDTH, SCENE_HEIGHT);
        stage.setScene(scene);
        stage.setTitle("Mine Sweeper");
        stage.show();
        stage.setOnCloseRequest(e -> {
                if (executor != null)
                    executor.shutdown();
        });
    }

    /**
     * Sets the functionality of the buttons on the screen.
     */
    public void setButtonActions() {
        saveButton.setOnAction(e -> {
        	if (!controller.isGameOver()) {
	        	// Should stop the clock when the user clicks save, so as not to rush them
	        	
        		FileChooser fileChooser = new FileChooser();
        		 
                //Set extension filter for text files
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
                fileChooser.getExtensionFilters().add(extFilter);
     
                //Show save file dialog
                File f = fileChooser.showSaveDialog(stage);
     
                if (f != null) {
                	try {
						controller.saveGame(f);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
	        	}
                }
        });
    	
        loadButton.setOnAction(e -> {
        	// Unlike save, you should be able to load a game even after having ended another one
        	
        	FileChooser chooser = new FileChooser();
        	
        		File f = chooser.showOpenDialog(stage);
        		if (f != null) {
                    try {
                        controller.loadGame(f);
                    } catch (IOException | ClassNotFoundException e1) {
                        e1.printStackTrace();
                    }
                }
        });
        
        resetButton.setOnAction(e -> {
        	stage.close();
			this.start(new Stage());
        });
        
    }

    /**
     *  Creates the blank game board of ROW x COL hexagons
     */
    public void createBoard(MineSweeperTile[][] board) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                addHex(row, col, board);
            }
        }
    }

    /**
     * Creates a new rectangle object at the specified row and col
     * 
     * @param row is the y coord
     * @param col is the x coord
     */
    private void addHex(int row, int col, MineSweeperTile[][] board) {
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
            if (board[row][col] != null)
	            if (board[row][col].getStatus().getColor() == Color.WHITE) {
	                animateTiles(row, col);
	            }
            if (board[row][col] != null)
	            if (board[row][col].getStatus().getColor() == Color.BLACK) {
	                animateBombs(row, col);
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
        MineSweeperTile[][] board = (MineSweeperTile[][]) arg;
        if (controller.isGameOver())  {// checks with Controller if game is over
            displayGameOver(); // calls the method to display the game over msg if true
            return;
        }

        // if the game isn't over, all the tiles are updated according to their enum
        for (int row = 0; row < ROWS; row++)
            for (int col = 0; col < COLS; col++) {

                rectGrid[row][col].setFill(board[row][col].getStatus().getColor());

                // Reveals minecount of any guessed tiles
                if (board[row][col].getMineCount() > 0 && board[row][col].getStatus().equals(GUESSED)) {
                    labelGrid[row][col].setText(""+board[row][col].getMineCount());
                    rectGrid[row][col].setFill(MINE_COUNT_TO_COLOR.get(board[row][col].getMineCount()));
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
        executor.shutdown();
    	String msg = "YOU WIN!!";
    	Paint p = Color.GREEN;
    	if (!controller.win()) { // checks with the controller if the player didn't win
    		msg = "YOU LOSE!";
    		p = Color.RED;
        }

    	Stage popUp = new Stage();
		Label label = new Label();
		
		label.setText(msg);
		label.setTextFill(Color.WHITE);
		label.setFont(Font.font("Helvetica", 50));
		label.setMaxWidth(Double.MAX_VALUE);
		label.setAlignment(Pos.BOTTOM_CENTER);
		
		Button btn = new Button();
		btn.setText("Play again");
		
		// play again scene (might want to make this a new function)
		BorderPane root = new BorderPane();
		root.setBackground(
				new Background(new BackgroundFill(p, new CornerRadii(6.0), Insets.EMPTY)));
		root.setTop(label);
		root.setBottom(btn);
		root.setAlignment(label, Pos.CENTER);
		root.setAlignment(btn, Pos.CENTER);
		
		Scene popScene = new Scene(root, 400, 120);

		popUp.setScene(popScene);
		popUp.setTitle("Game Over");
		popUp.show();
		
		btn.setOnMousePressed(me -> {
			stage.close();
			this.start(new Stage());
			popUp.close();
        });
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
    /**
     * TODO: Change from while true to a Scheduled Executor
     * Creates a timer that continually updates 
     * @return - a text object which can be added to the screen and updated with the timer
     */
    private Text createTimer() {
    	Text timer = new Text();
    	timer.setFont(MAIN_FONT);

        executor = Executors.newScheduledThreadPool(1, e -> {
            Thread t = new Thread(e);
            t.setDaemon(true);
            return t;
        });

        Runnable updateTimerRunner = () -> Platform.runLater(() -> updateTimer(timer));

        executor.scheduleAtFixedRate(updateTimerRunner, 0, DELTA_TIME_MS, TimeUnit.MILLISECONDS);

    	return timer;
    }

    private void updateTimer(Text timer) {
        timer.setText("Time: "+ String.format("%.2f", controller.getSecondsElapsed()));
    }
}