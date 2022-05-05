package Controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Observer;
import java.util.Random;

import Models.MineSweeperBoard;
import Models.MineSweeperTile;
import Models.ScoreBoard;
import Utils.GUESS_STATUS;
import javafx.util.Pair;

/**
 * This class stores the controller for the Minesweeper game.
 *
 * The controller's responsibility is to organize the updating of a board according to user inputs,
 * and to relay information to and from the model, especially for the purpose of save/load functionality
 * and checking whether the game is over.
 *
 */
public class MineSweeperController implements Serializable {

	private final MineSweeperBoard model;
	private final ScoreBoard scoreBoard;
	private boolean gameOver; // tracks if game is over
	private int numberOfGuesses; // keeps track of the total number of guesses
	private boolean win;
	private MineSweeperTile[][] board;

	/**
	 * Constructor for the controller.
	 *
	 * When initialized, the controller immediately creates a new board with
	 * the size and mine density appropriate for the selected difficulty.
	 *
	 * @param difficulty A string representing the difficulty of the game, which affects board size and mine density.
	 */
	public MineSweeperController(String difficulty) {
		this.model = new MineSweeperBoard(difficulty);
		this.scoreBoard = new ScoreBoard();
		win = true; // keeps track of the total number of guesses
	}

	/**
	 * Constructs a controller from a saved file.
	 *
	 * @param savedController The file in which the saved game is stored.
	 * @throws IOException If file loading failed.
	 * @throws ClassNotFoundException If a MineSweeperTile[][] could not be loaded from the file information.
	 */
	public MineSweeperController(File savedController) throws IOException, ClassNotFoundException {
		this("Normal");
		this.loadGame(savedController);
	}

	/**
	 * This method determines the current status of the clicked tile, and based
	 * on the status parameter, assigns a new status to the tile.
	 *
	 * @param row is the y coord of the tile
	 * @param col is the x coord of the tile
	 * @param status is an enum either GUESSED or FLAGGED, depending on the mouse button clicked.
	 */
	public void updateTileStatus(int row, int col, GUESS_STATUS status) {
		board = model.getBoard();
		// this is a complex guard statement to make sure we are in bounds of the board
		// it is needed from updateTilesAround
		if (board != null && (row >= board.length || row < 0 || col >= board[row].length || col < 0)) return;
		if (gameOver || board == null || board[row] == null) return; // basic guard statements

		//determines if the board is still null (indicates that this is the player's first click of the game.

		if (board[row][col] == null) {
			if (!(status.equals(GUESS_STATUS.FLAGGED))) {
				model.createBoard(row, col, new Random()); // creates the board and places all bombs
				updateTileStatus(row, col, status); // updates the board with the player's click
			}
		}

		/* If the tile is already flagged and the player right clicks again, the tile
		 * if "unflagged" and set back to an "UNGUESSED" status
		 */
		else if (board[row][col].getStatus().equals(GUESS_STATUS.FLAGGED)) {
			if (status.equals(GUESS_STATUS.FLAGGED))
				model.updateTileStatus(row, col, GUESS_STATUS.UNGUESSED);
		}
		/* If the player clicks on a tile that is a bomb, win is set to false and
		 * all bombs are shown.
		 */
		else if (status.equals(GUESS_STATUS.GUESSED) && board[row][col].isBomb()) {
			win = false;
			showAllBombs();
		}
		/* If the tile is set currently as UNGUESSED, it will be changed to either
		 * GUESSED or FLAGGED, depending on which button the player clicked.
		 */
		else if (board[row][col].getStatus().equals(GUESS_STATUS.UNGUESSED)) {
			model.updateTileStatus(row, col, status);
			if (status.equals(GUESS_STATUS.GUESSED)) {
				if (board[row][col].getMineCount() == 0)
					checkAdjacent(row, col);
				numberOfGuesses++;
			}
		}
		/*
		 * Checks if the game is over by checking the number of player clicks
		 * that have not been bombs or flagging clicks.
		 */
		if (numberOfGuesses == (board.length*board[0].length) - model.getNumBombs() && !gameOver) {
			showAllBombs();
		}

	}

	/**
	 * When the user clicks a tile, this method updates all of the tiles around it.
	 *
	 * @param row The row of the tile to update around.
	 * @param col The column of the tile to update around.
	 */
	public void updateTilesAround(int row, int col) {
		if (this.model.getBoard()[row][col] != null)
			for (Pair<Integer, Integer> coord : this.model.getBoard()[row][col].getAdjacentTiles()) {
				if (this.model.getBoard()[row][col].getStatus() != GUESS_STATUS.FLAGGED)
					updateTileStatus(coord.getKey(), coord.getValue(), GUESS_STATUS.GUESSED);
			}
	}

	/**
	 * This method reveals all tiles adjacent to a zero tile.
	 * This method will continue to be called by updateTileStatus() until all chained
	 * zero tiles are revealed, as well as all tiles that are adjacent to that chain.
	 *
	 * @param row The row of the tile to check.
	 * @param col The column of the tile to check.
	 */
	private void checkAdjacent(int row, int col) {
		for (Pair<Integer, Integer> coord : this.model.getBoard()[row][col].getAdjacentTiles()) {
			int tempRow = coord.getKey();
			int tempCol = coord.getValue();
			if (tempRow>= 0 && tempRow < model.getRows() && tempCol >= 0 && tempCol < model.getCols()) {
				if (!(model.getBoard()[tempRow][tempCol].isBomb())){
					updateTileStatus(tempRow, tempCol, GUESS_STATUS.GUESSED);}
			}
		}
	}

	/**
	 *  Shows all bombs by iterating through the board. If the tile returns
	 *  true when isBomb() is called, the enum for the tile is changed to
	 *  BOMB and will display as a bomb on the board when the model notifies.
	 */
	private void showAllBombs() {
		// gets the current board from the model
		board = model.getBoard();

		for (int row = 0; row < board.length; row++) {
			for (int col = 0; col < board[row].length; col++) {
				if (board[row][col].isBomb())
					// if the tile is a bomb, set the color of the tile to a bomb
					model.updateTileStatus(row, col, GUESS_STATUS.BOMB);
			}
		}
		gameIsOver(); // the game is over, so we update the controller to reflect this
	}

	/**
	 * Adds an observer to the model's observer list.
	 *
	 * @param o The observer to be added. Most likely a view object.
	 */
	public void setObserver(Observer o) {
		model.setObserver(o);
	}

	/**
	 * Saves the game by outputting the current state of the board and the instance variables to a file.
	 * @param f - the file to save the game into
	 * @throws IOException If the game fails to save.
	 */
	public void saveGame(File f) throws IOException {
		FileOutputStream fos = new FileOutputStream(f);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		oos.writeObject(this.model.getBoard());
		// We have no need to serialize the model's observers and should not try,
		// but unfortunately that means breaking things up a bit instead of just serializing everything at once.
		oos.writeInt(this.model.getNumBombs());
		oos.writeObject(this.model.getDifficulty());

		oos.writeBoolean(this.gameOver);
		oos.writeInt(this.numberOfGuesses);
		oos.writeBoolean(this.win);
		oos.writeObject(this.board);
		oos.writeInt((int)getSecondsElapsed());

		oos.close();
	}

	/**
	 * Loads the game by reading off parameters from the Controller object stored in the chosen file.
	 * @param f - the file to read the game from
	 * @throws IOException If opening the file fails
	 * @throws ClassNotFoundException If the object we try to load is not a valid MineSweeperTile[][]
	 */
	public void loadGame(File f) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(f);
		ObjectInputStream ois = new ObjectInputStream(fis);

		MineSweeperTile[][] newBoard = (MineSweeperTile[][]) ois.readObject();
		model.setBoard(newBoard);
		model.setNumBombs(ois.readInt());
		model.setDifficulty((String) ois.readObject());
		model.setBoardDimensions(model.getDifficulty());

		// set controller variables for this specific file
		this.gameOver = ois.readBoolean();
		this.numberOfGuesses = ois.readInt();
		this.win = ois.readBoolean();
		this.board = (MineSweeperTile[][]) ois.readObject();
		model.setSecondsElapsed(ois.readInt());
		ois.close();
	}

	/**
	 * This passes the timer creation onto the model
	 * @param updater - the runnable to be called upon updates to the timer
	 */
	public void createTimer(Runnable updater) {
		model.createBoardTimer(updater);
	}

	/**
	 * This kills the timer executor so that it is no longer running.
	 * This allows us to either create a new executor for a new game,
	 * or we can exit the program
	 */
	public void shutdownTimer() {
		model.shutdown();
	}

	/**
	 * This takes a seeded random and uses it to generate a known board
	 * @param row - the starting row
	 * @param col - the starting col
	 * @param random - a seeded random from a testcase
	 */
	public void seedBoardGeneration(int row, int col, Random random) {
		model.createBoard(row, col, random);
	}

	//GETTERS AND SETTERS

	/**
	 * Checks if the game is over
	 * @return true if the game is over, false if we are still playing
	 */
	public boolean isGameOver() {
		return gameOver;
	}

	/**
	 * Sets gameOver to true and calls the model's notify method.
	 */
	public void gameIsOver() {
		gameOver = true;
		if (win) scoreBoard.addNewTime(model.getSecondsElapsed(), model.getDifficulty(), true);
		model.notifyObservers();
	}

	/**
	 * Checks if the user won
	 * @return true if the user won, false if they lost
	 */
	public boolean win() {
		return win;
	}

	/**
	 * Gets the current board for this game
	 * @return The MineSweeperTile[][] array from this controller's current model.
	 */
	public MineSweeperTile[][] getBoard() {
		return this.model.getBoard();
	}

	/**
	 * When called, this method updates the model's time by some number of milliseconds and returns the new time.
	 *
	 * @return The number of seconds that the current game has been going for, as a double.
	 */
	public double getSecondsElapsed() {
		return model.getSecondsElapsed();
	}

	/**
	 * Retrieves the top times from the scoreboard for this game's difficulty.
	 *
	 * @return - a string representing the top times
	 */
	public String[] getTopTimes() {
		Double[] times = scoreBoard.getTopTimes(model.getDifficulty());
		String[] topTimes = new String[times.length];

		for (int i = 0; i < topTimes.length; i++) {
			topTimes[i] = model.getDifficulty() + ": " + times[i];
			if (times[i] == null || times[i] == 0.0)
				topTimes[i] = model.getDifficulty() + ": -.-";
		}
		return topTimes;
	}

	/**
	 * Gets the rows of the current board
	 * @return The number of rows in the model's grid.
	 */
	public int getRows() {
		return model.getRows();
	}

	/**
	 * Gets the cols of the current board
	 * @return The number of columns in the model's grid.
	 */
	public int getCols() {
		return model.getCols();
	}

	/**
	 * This disables the models timer (handled in the model)
	 */
	public void disableTimer() {
		model.disableTimer();
	}

	/**
	 * This enables the models timer (handled in the model)
	 */
	public void enableTimer() {
		model.enableTimer();
	}

	/**
	 * This checks if the game is paused by determining
	 * if the model's timer is equal to 0 (not incrememing)
	 * @return true if the game is paused
	 */
	public boolean isGamePaused() {
		return model.isGamePaused();
	}

	/**
	 * Gets a nice string in the form of "CURRENT/TOTAL" for the number of flags places and mines
	 * @return A string representation of the number of flags and mines on the board
	 */
	public String getMineCount() {
		return model.getNumFlags() + " / " + model.getNumBombs();
	}

	/**
	 * Returns how many bombs are on the current board
	 * @return The number of bombs on the current board
	 */
	public int getBombCount() {
		return model.getNumBombs();
	}

	/**
	 * This is used to test if the scoreboard workd
	 * @return - the scoreboard for testing
	 */
	public ScoreBoard getScoreBoard() {
		return scoreBoard;
	}
}