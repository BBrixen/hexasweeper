package View;

import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import Models.MineSweeperTile;
import Controllers.MineSweeperController;
import javafx.util.Pair;

import static Utils.GUESS_STATUS.*; // this is fine since its 4 items

/**
 * This class is a graphical user interface for a game of Minesweeper.
 * 
 * The playing field is a grid of hexagonal tiles in the middle of the screen that can be clicked to reveal or flag them,
 * while a row of buttons beneath it controls saving, loading, pausing, and starting a new game.
 * Meanwhile, a scoreboard on the left side displays the top times for the current difficulty setting,
 * and labels above the playing field show the time spent on the current game and the number of flags and mines.
 *
 */
@SuppressWarnings("deprecation")
public class MineSweeper extends Application implements Observer {

    // game variables
    private MineSweeperController controller;

    // gui constants
    private static final double SCREEN_WIDTH = Screen.getPrimary().getVisualBounds().getWidth();
    private static final double SCREEN_HEIGHT = Screen.getPrimary().getVisualBounds().getHeight();
    private static double HEX_RADIUS = Math.min(SCREEN_HEIGHT/(16*2), 30);
    private static double HEX_SIZE = Math.sqrt(HEX_RADIUS * HEX_RADIUS * 0.75);
    private static int
            SCENE_WIDTH = (int) (1.75*(24 + 2) * HEX_RADIUS);
    private static int SCENE_HEIGHT = (int) (1.5*(16 + 5) * HEX_RADIUS);
    private static double HEX_HEIGHT = 2* HEX_RADIUS;
    private static double HEX_WIDTH = 2*HEX_SIZE;
    private static double MAIN_FONT_SIZE = HEX_HEIGHT/2.5;
    private static Font MAIN_FONT = new Font("Helvetica", MAIN_FONT_SIZE);
    private static final String BUTTON_STYLE = "-fx-background-color: white;"
    		+ "  -fx-border-color: black;"
    		+ "  -fx-border-radius: 10;"
    		+ "  -fx-background-radius: 10;";
    private static final Insets DEFAULT_INSETS = new Insets(10);
    private static final Paint GREEN_BACKGROUND = Color.rgb(120, 190, 120);
    private static final Paint RED_BACKGROUND =  Color.rgb(190, 120, 120);
    private static final HashMap<Integer, Color> MINE_COUNT_TO_COLOR = new HashMap<>();
    private static final Color ONE_MINE = Color.rgb(207, 236, 207);
    private static final Color TWO_MINE = Color.rgb(204, 236, 239);
    private static final Color THREE_MINE = Color.rgb(221, 212, 232);
    private static final Color FOUR_MINE = Color.rgb(253, 222, 238);
    private static final Color FIVE_MINE =  Color.rgb(253, 202, 162);
    private static final Color SIX_MINE = Color.rgb(255, 105, 97);


    // gui variables
    // these grid variables must be global since they are used
    // inside the update function and there is no way to pass them as parameters
    private Hexagon[][] rectGrid;
    private Label[][] labelGrid;
    private HashSet<Pair<Integer, Integer>> animatedTiles;
    // the stage must be global since it is used and modified in many locations.
    // it is far too much pain to make it a local variable
    // also this variable makes sense to be global since it is the main display stage
    private Stage stage;

    //////////// CREATING THE SCENE AND GAME ////////////

    /**
     * Main method, called on program startup. It allocates colors to adjacent-mine counts before starting up the view.
     */
    public static void main(String[] args) {
        // filling hashmap, this only needs to be done once
        MINE_COUNT_TO_COLOR.put(1, ONE_MINE);
        MINE_COUNT_TO_COLOR.put(2, TWO_MINE);
        MINE_COUNT_TO_COLOR.put(3, THREE_MINE);
        MINE_COUNT_TO_COLOR.put(4, FOUR_MINE);
        MINE_COUNT_TO_COLOR.put(5, FIVE_MINE);
        MINE_COUNT_TO_COLOR.put(6, SIX_MINE);
        launch(args);
    }

    /**
     * Runs upon launch. It fills out and then creates the main window of the game, before prompting the user to select a difficulty for their first game.
     */
    @Override
    public void start(Stage stage) {
    	this.stage = stage;
    	// initialize game
        createController("Normal");

        stage.setTitle("Mine Sweeper");
        stage.show();
        stage.setOnCloseRequest(e -> controller.shutdownTimer());
        chooseDiff();
    }

    /**
     * Sets up the basic structure of the game screen, including the buttons, timer, and their relative locations in the panes.
     */
    private Scene createScene() {
        rectGrid = new Hexagon[controller.getRows()][controller.getCols()];
        labelGrid = new Label[controller.getRows()][controller.getCols()];
        AnchorPane gridPane = new AnchorPane();
        HBox buttonRow = new HBox(MAIN_FONT_SIZE);
        HBox mainPane = new HBox();
        VBox mainVBox = new VBox(MAIN_FONT_SIZE);

        // creating bottom buttons
        Button saveButton = new Button("Save");
        Button loadButton = new Button("Load");
        Button resetButton = new Button("Reset");
        saveButton.setStyle(BUTTON_STYLE);
        loadButton.setStyle(BUTTON_STYLE);
        resetButton.setStyle(BUTTON_STYLE);
        saveButton.setFont(MAIN_FONT);
        loadButton.setFont(MAIN_FONT);
        resetButton.setFont(MAIN_FONT);
        setButtonActions(saveButton, loadButton, resetButton);
        Button pause = createPauseButton();
        pause.setStyle(BUTTON_STYLE);

        buttonRow.getChildren().addAll(pause, saveButton, loadButton, resetButton);
        buttonRow.setAlignment(Pos.CENTER);


        // creating timer and number of bombs

        // Delete the current executor for the timer; we'll make a new one in the next line
        controller.shutdownTimer();
        HBox timerAndMineCount = createTimerAndMineCount();

        mainVBox.getChildren().addAll(timerAndMineCount, gridPane, buttonRow);
        mainVBox.setAlignment(Pos.CENTER);

        // creates the initial blank board
        createBoard(controller.getBoard(), gridPane);
        createScoreBoard(controller, mainPane);

        mainPane.getChildren().addAll(mainVBox);
        return new Scene(mainPane, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    /**
     * Adjusts constants in the program based on the size of the game board, in order to rescale the board so it fits on screen.
     * 
     * @param rows The size of the game board, in rows.
     * @param cols The size of the game board, in columns.
     */
    private void generateConstants(int rows, int cols) {
        HEX_RADIUS = Math.min(SCREEN_HEIGHT/(rows*2), 30);
        HEX_SIZE = Math.sqrt(HEX_RADIUS * HEX_RADIUS * 0.75);
        SCENE_WIDTH = (int) (1.75*(cols + 2) * HEX_RADIUS);
        SCENE_HEIGHT = (int) (1.5*(rows + 5) * HEX_RADIUS);
        HEX_HEIGHT = 2* HEX_RADIUS;
        HEX_WIDTH = 2*HEX_SIZE;
        MAIN_FONT_SIZE = HEX_HEIGHT/2;
        MAIN_FONT = new Font("Helvetica", MAIN_FONT_SIZE);
    }

    /**
     * Creates a new controller with the chosen difficulty, then sets up the display with it.
     * 
     * @param difficulty The string for the difficulty of game to set up.
     */
    private void createController(String difficulty) {
        controller = new MineSweeperController(difficulty);
        createDisplayFromController();
    }

    /**
     * Creates a controller from a loaded file, which entails updating the board after setting up the display.
     * 
     * @param file The file to load the game from.
     * @throws IOException If file loading fails.
     * @throws ClassNotFoundException If a MineSweeperTile[][] could not be loaded from the file information.
     */
    private void createController(File file) throws IOException, ClassNotFoundException {
        controller = new MineSweeperController(file);
        createDisplayFromController();
        update(null, controller.getBoard()); // we need to update the view with the newly-loaded board
    }

    /**
     * Uses the information in the controller (namely the size of the board) to create an initial grid of hexagons of that size.
     * 
     * This method also sets the current view to be the observer for the controller's stored model.
     */
    private void createDisplayFromController() {
        generateConstants(controller.getRows(), controller.getCols());
        animatedTiles = new HashSet<>();
        controller.setObserver(this); // add as observer for model (MineSweeperBoard)
        rectGrid = new Hexagon[controller.getRows()][controller.getCols()];
        labelGrid = new Label[controller.getRows()][controller.getCols()];
        stage.setScene(createScene());
    }

    /**
     *  Creates the blank game board of ROW x COL hexagons
     */
    public void createBoard(MineSweeperTile[][] board, AnchorPane gridPane) {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                addHex(row, col, board, gridPane);
            }
        }
    }

    /**
     * Creates a scoreboard from the top times for the current difficulty.
     * 
     * @param controller
     * @param mainPane
     */
    public void createScoreBoard(MineSweeperController controller, HBox mainPane) {
    	String[] topTimes = controller.getTopTimes();
        Label topLabel = new Label("Top Scores   ");
        topLabel.setFont(MAIN_FONT);
        Label[] topTimeLabels = new Label[topTimes.length+1];
        topTimeLabels[0] = topLabel;

        for (int i = 0; i < topTimes.length; i++) {
            Label label = new Label(topTimes[i]);
            label.setTextFill(GREEN_BACKGROUND);
            label.setFont(MAIN_FONT);
            label.setTextFill(GREEN_BACKGROUND);
            label.setPadding(DEFAULT_INSETS);
            topTimeLabels[i+1] = label;
        }
        
        VBox scoreBoard = new VBox(MAIN_FONT_SIZE / 2);
        scoreBoard.getChildren().addAll(topTimeLabels);
        scoreBoard.setAlignment(Pos.CENTER);
        
	    mainPane.getChildren().add(0, scoreBoard);
	    mainPane.setAlignment(Pos.CENTER);
    }

    //////////// CREATING COMPONENTS FOR THE DISPLAY ////////////

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
     * Creates a new rectangle object at the specified row and col
     *
     * @param row is the y coord
     * @param col is the x coord
     */
    private void addHex(int row, int col, MineSweeperTile[][] board, AnchorPane gridPane) {
        double yCoord = (row+1) * HEX_HEIGHT * 0.75;
        double xCoord = (col+1) * HEX_WIDTH + ((row % 2) * HEX_SIZE);
        Hexagon hex = new Hexagon(xCoord, yCoord);
        hex.setFill(UNGUESSED.getColor());

        Label label = new Label("");
        label.setFont(MAIN_FONT);
        label.setTranslateX(xCoord);
        label.setTranslateY(yCoord);
        label.setPadding(new Insets(HEX_HEIGHT/-8,0,0,HEX_WIDTH/3));

        EventHandler clicker = event -> {
            MouseEvent e = (MouseEvent) event;
            if (e.getClickCount() == 2 && e.isPrimaryButtonDown()) {
                // double click to reveal around
                controller.updateTilesAround(row, col);

            } else if (e.isPrimaryButtonDown()) {
                // "step on" the tile
                controller.updateTileStatus(row, col, GUESSED);

                if (board[row][col] != null && e.getClickCount() == 1) {
                    if (board[row][col].getStatus() == GUESSED) {
                        animateTiles(row, col);
                    }
                    else if (board[row][col].getStatus() == BOMB)
                        animateBombs(row, col);

                }

            } else if (e.isSecondaryButtonDown()) {
                controller.updateTileStatus(row, col, FLAGGED);  // flag the tile
            }
        };
        hex.setOnMousePressed(clicker);
        label.setOnMousePressed(clicker);

        // adding it to the grids and groups
        rectGrid[row][col] = hex;
        labelGrid[row][col] = label;
        gridPane.getChildren().add(hex);
        gridPane.getChildren().add(label);
    }

    /**
     * Creates a timer that continually updates
     * @return - a text object which can be added to the screen and updated with the timer
     */
    private HBox createTimerAndMineCount() {
        HBox gameInfo = new HBox(MAIN_FONT_SIZE);
        gameInfo.setAlignment(Pos.CENTER);
        gameInfo.setPadding(DEFAULT_INSETS);

        Text mineCount = new Text();
        Text timer = new Text();
        mineCount.setFont(MAIN_FONT);
        timer.setFont(MAIN_FONT);
        gameInfo.getChildren().addAll(mineCount, timer);

        // platform.runlater so this runs after javafx has initialized
        Runnable updateTimerRunner = () -> Platform.runLater(() -> {
            mineCount.setText(controller.getMineCount());
            timer.setText("Time: "+ String.format("%.2f", controller.getSecondsElapsed()));
        });

        controller.createTimer(updateTimerRunner);
        return gameInfo;
    }

    /**
     * Creates the actual pause button to be displayed in the scene
     * @return - the pause button to be added to the scene
     */
    private Button createPauseButton() {
        Button button = new Button();
        Image pauseImage = new Image("file:Images/pause.png", 2*MAIN_FONT_SIZE, 2*MAIN_FONT_SIZE, true, false);
        ImageView view = new ImageView(pauseImage);
        view.setFitHeight(2*MAIN_FONT_SIZE);
        view.setPreserveRatio(true);
        button.setGraphic(view);
        button.setPrefSize(2*MAIN_FONT_SIZE, 2*MAIN_FONT_SIZE);
        button.setOnMouseClicked(e -> {
            // If game is paused and button is clicked, switch image back to pause button
            if (controller.isGamePaused()) {
                button.setGraphic(view);
                unpauseGame();
            }


            else {
                Image playImage = new Image("file:Images/play.png",
                        2*MAIN_FONT_SIZE, 2*MAIN_FONT_SIZE, true, false);
                ImageView playView = new ImageView(playImage);
                button.setGraphic(playView);
                pauseGame();
            }

        });
        return button;
    }

    /**
     * Sets the functionality of the save, load, and reset buttons on the screen.
     */
    public void setButtonActions(Button saveButton, Button loadButton, Button resetButton) {
        saveButton.setOnAction(e -> {
            if (controller.isGameOver()) return;
            // Call pause method in order to prevent player cheating with file dialog box
            pauseGame();

            FileChooser fileChooser = new FileChooser();
            //Set extension filter for text files
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
            fileChooser.getExtensionFilters().add(extFilter);

            //Show save file dialog
            File f = fileChooser.showSaveDialog(stage);
            if (f != null)
                try {
                    controller.saveGame(f);
                } catch (IOException ignored) {} // TODO make popup showing error?

        	unpauseGame();
        });
    	
        loadButton.setOnAction(e -> {
        	// Unlike save, you should be able to load a game even after having ended another one
        	
        	FileChooser chooser = new FileChooser();
        	pauseGame();
        		File f = chooser.showOpenDialog(stage);
        		if (f != null)
                    try {
                        createController(f);
                    } catch (IOException | ClassNotFoundException ignored) {} // TODO make popup showing error?

        		unpauseGame();
        });
        
        resetButton.setOnAction(e -> chooseDiff());
    }

    //////////// POP UP DISPLAYS ////////////

    /**
     * This method sets up the game over message in the middle of the board
     * when the game is over, then calls its display method, playAgainPop().
     */
    public void displayGameOver() {
    	controller.disableTimer();
        String msg = "YOU WIN!";
        Paint p = GREEN_BACKGROUND;
        if (!controller.win()) { // checks with the controller if the player didn't win
            msg = "YOU LOSE!";
            p = RED_BACKGROUND;
        }

        Stage popUp = new Stage();
        BorderPane root = new BorderPane();

        Button btn = new Button("Play again");
        btn.setTextFill(Color.BLACK);
        btn.setStyle(BUTTON_STYLE);
        btn.setFont(MAIN_FONT);
        btn.setAlignment(Pos.CENTER);

        Label label = new Label(msg);
        label.setTextFill(Color.WHITE);
        label.setFont(MAIN_FONT);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.BOTTOM_CENTER);
        label.setPadding(new Insets(20, 0, 0, 0));  // move down 10
        playAgainPop(root, label, btn, popUp, p);
    }

    /**
     * Displays the play-again message in the view.
     * 
     * @param root
     * @param label
     * @param btn
     * @param popUp
     * @param p
     */
    private void playAgainPop(BorderPane root, Label label, Button btn, Stage popUp, Paint p) {
        root.setBackground(
                new Background(new BackgroundFill(p, new CornerRadii(6.0), Insets.EMPTY)));
        root.setTop(label);
        root.setBottom(btn);
        BorderPane.setAlignment(label, Pos.CENTER);
        BorderPane.setAlignment(btn, Pos.CENTER);
        BorderPane.setMargin(btn, DEFAULT_INSETS); // this was changed from 20 to 10, shouldnt cause issues? hopefully?

        Scene popScene = new Scene(root, (float) SCENE_WIDTH/2, (float) SCENE_HEIGHT/6);
        popUp.setScene(popScene);
        popUp.setTitle("Game Over");
        popUp.show();

        btn.setOnMousePressed(me -> {
            popUp.close();
            chooseDiff();
        });
    }

    /**
     * Sets up buttons and labels for the difficulty select popup, then calls their display methods.
     */
    public void chooseDiff() {
        Stage diffPop = new Stage();

        Label label = new Label();
        label.setText("Choose Difficulty");
        label.setTextFill(Color.WHITE);
        label.setFont(MAIN_FONT);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.BOTTOM_CENTER);
        label.setPadding(new Insets(20, 0, 0, 0));

        // create and style buttons
        Button veryEasy = new Button("Very Easy");
        Button easy = new Button("Easy");
        Button normal = new Button("Normal");
        Button hard = new Button("Hard");
        Button veryHard = new Button("Very Hard");
        veryEasy.setStyle(BUTTON_STYLE);
        easy.setStyle(BUTTON_STYLE);
        normal.setStyle(BUTTON_STYLE);
        hard.setStyle(BUTTON_STYLE);
        veryHard.setStyle(BUTTON_STYLE);

        HBox buttonBox = new HBox();
        buttonBox.getChildren().addAll(veryEasy, easy, normal, hard, veryHard);
        buttonBox.setBackground(new Background(
                new BackgroundFill(GREEN_BACKGROUND, new CornerRadii(6.0), Insets.EMPTY)));

        diffPopUp(buttonBox, label, diffPop);
        diffListener(veryEasy, easy, normal, hard, veryHard, diffPop);
    }

    /**
     * Draws the difficulty-select popup.
     * 
     * @param buttonBox
     * @param label
     * @param diffPop
     */
    private void diffPopUp(HBox buttonBox, Label label, Stage diffPop) {
        buttonBox.setPadding(DEFAULT_INSETS);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);

        BorderPane diff = new BorderPane();
        diff.setBackground(new Background(
                new BackgroundFill(GREEN_BACKGROUND, new CornerRadii(6.0), Insets.EMPTY)));
        diff.setTop(label);
        diff.setBottom(buttonBox);
        BorderPane.setAlignment(label, Pos.CENTER);
        BorderPane.setAlignment(buttonBox, Pos.CENTER);

        Scene diffScene = new Scene(diff, (float) SCENE_WIDTH/2, (float) SCENE_HEIGHT/6);
        diffScene.setFill(GREEN_BACKGROUND);
        diffPop.setScene(diffScene);
        diffPop.setTitle("New Game");
        diffPop.show();
    }

    /**
     * Allocates mouse-press responses to each of the difficulty-select buttons.
     * 
     * @param veryEasy
     * @param easy
     * @param normal
     * @param hard
     * @param veryHard
     * @param diffPop
     */
    private void diffListener(Button veryEasy, Button easy, Button normal, Button hard, Button veryHard, Stage diffPop) {
        veryEasy.setOnMousePressed(me -> {
            createController("Very Easy");
            diffPop.close();
        });
        easy.setOnMousePressed(me -> {
            createController("Easy");
            diffPop.close();
        });
        normal.setOnMousePressed(me -> {
            createController("Normal");
            diffPop.close();
        });
        hard.setOnMousePressed(me -> {
            createController("Hard");
            diffPop.close();
        });
        veryHard.setOnMousePressed(me -> {
            createController("Very Hard");
            diffPop.close();
        });
    }

    //////////// ANIMATIONS ////////////

    /**
     * Animates the reveal of a bomb tile by scaling and spinning it.
     * 
     * @param row The row of the bomb to animate.
     * @param col The column of the bomb to animate.
     */
	private void animateBombs(int row, int col) {
		rectGrid[row][col].toFront();
    	ScaleTransition big = new ScaleTransition(Duration.millis(400), rectGrid[row][col]);
    	RotateTransition rt = new RotateTransition(Duration.millis(1000), rectGrid[row][col]);
        rt.setByAngle(360);
        rt.setCycleCount(1);
        big.setByX(.2f);
    	big.setByY(.2f);
    	big.setCycleCount(2);
    	rectGrid[row][col].toFront();
    	big.setAutoReverse(true);
    	big.play();
    	rt.play();
	}

	/**
	 * Animates the reveal of a tile by scaling it up and down in a "popping" motion.
	 * 
	 * @param row The row of the tile to animate.
	 * @param col The column of the tile to animate.
	 */
	private void animateTiles(int row, int col) {
        // make sure this is not already being animated
        Pair<Integer, Integer> coords = new Pair<>(row, col);
        if (animatedTiles.contains(coords)) return;
        animatedTiles.add(coords);

		ScaleTransition big = new ScaleTransition(Duration.millis(300), rectGrid[row][col]);
    	big.setByX(.1f);
    	big.setByY(.1f);
    	big.setAutoReverse(true);
    	big.setCycleCount(2);
    	big.play();
        // after animation, remove it from the animated tiles
        big.setOnFinished(e -> animatedTiles.remove(coords));
	}

    //////////// UPDATE THE DISPLAY ////////////
    
    /**
     * Disables the elements in the scene and pauses the timer
     */
    private void pauseGame() {
    	controller.disableTimer();
    	setBoardOpacity(0.0);
    	setBoardDisabled(true);
    }
    
    /**
     * Re-enables the elements in the scene and
     * unpauses the timer
     */
    private void unpauseGame() {
    	controller.enableTimer();
    	setBoardOpacity(1.0);
    	setBoardDisabled(false);
    }
    
    /**
     * Sets every hexagon's opacity to the value given in the arguments
     * @param opacity the opacity to set each hexagon to, 0.0 being translucent, and 1.0 being fully
     * opaque
     */
    private void setBoardOpacity(double opacity) {
        for (int i = 0; i< rectGrid.length && i < labelGrid.length; i++) {
            for (int j = 0; j < rectGrid[i].length && j < labelGrid[i].length; j++) {
                rectGrid[i][j].setOpacity(opacity);
                labelGrid[i][j].setOpacity(opacity);
            }
        }
    }
    
    /**
     * Sets every hexagon and label to be disabled or enabled, true being disabled
     * @param disabled True if elements are desired to be disabled
     */
    private void setBoardDisabled(boolean disabled) {
    	for (int i = 0; i< rectGrid.length && i < labelGrid.length; i++) {
    		for (int j = 0; j < rectGrid[i].length && j < labelGrid[i].length; j++) {
    			rectGrid[i][j].setDisable(disabled);
                labelGrid[i][j].setDisable(disabled);
    		}
    	}
    }

    /**
     * Updates the view with the new board state, including calling game-over messages if the game has ended.
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
        for (int row = 0; row < board.length; row++)
            for (int col = 0; col < board[row].length; col++) {

                rectGrid[row][col].setFill(board[row][col].getStatus().getColor());

                // Reveals minecount of any guessed tiles
                if (board[row][col].getMineCount() > 0 && board[row][col].getStatus().equals(GUESSED)) {
                    labelGrid[row][col].setText(""+board[row][col].getMineCount());
                    rectGrid[row][col].setFill(MINE_COUNT_TO_COLOR.get(board[row][col].getMineCount()));
                }
                else {
                    labelGrid[row][col].setText("");
                }
            }
    }
}