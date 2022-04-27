package Models;

import java.io.Serializable;
import java.util.*; // TODO: dont import *
import Utils.GUESS_STATUS;

import static View.MineSweeper.*;

/**
 * This class holds the model for a game of Minesweeper, focusing on the gameplay area itself.
 * 
 * The grid of tiles is stored as a 2D array of MineSweeperTiles here,
 * while the class keeps track of gameplay information that needs to be stored,
 * such as elapsed time and variables that are relevant during setup.
 * The class also updates its observers with any changes to the board state.
 *
 */
@SuppressWarnings("deprecation")
public class MineSweeperBoard extends Observable implements Serializable {

	/**
	 * A 2D array of MindsweeperTiles to store information on the instantaneous board state.
	 */
	private MineSweeperTile[][] board;
	
	private final List<Observer> observers; // do we need a list?
	private int ms_elapsed;
	private final int numBombs;
	private String difficulty;
	private int timeInc;

	private int rows = 16, cols = 24;
	
	/**
	 * The "divider" variables affect the density of mines; for divider N, one in N tiles should be a mine.
	 */
	private static final int VERY_HARD_DIVIDER = 3;
	private static final int HARD_DIVIDER = 4;
	private static final int NORMAL_DIVIVER = 5;
	private static final int EASY_DIVIDER = 8;
	private static final int VERY_EASY_DIVIDER = 20;
	
	/**
	 * Constructor for the MineSweeperBoard model object.
	 * 
	 * @param difficulty A string representing the difficulty of the game, which affects board size and mine density.
	 */
	public MineSweeperBoard(String difficulty) {
		//initializes the board as a ROWS x COLS 2D array with null pointers for now
		int divider = NORMAL_DIVIVER;
		switch (difficulty) {
			case "Very Easy":
				divider = VERY_EASY_DIVIDER;
				cols = 16;
				break;
			case "Easy":
				divider = EASY_DIVIDER;
				cols = 20;
				break;
			case "Hard":
				divider = HARD_DIVIDER;
				rows = 20;
				break;
			case "Very Hard":
				divider = VERY_HARD_DIVIDER;
				rows = 24;
				cols = 30;
				break;
		};

		numBombs = rows * cols / divider;
		board = new MineSweeperTile[rows][cols];
		observers = new ArrayList<>();
		ms_elapsed = -1;
		this.difficulty = difficulty;
		timeInc = DELTA_TIME_MS;
	}
	
	/**
	 * Places all the mines and unmined tiles once the player clicks the
	 * first tile.
	 * 
	 * @param row is the y coord of the first tile clicked
	 * @param col is the x coord of the first tile clicked
	 */
	public void createBoard(int row, int col) {
		// Start the timer
		ms_elapsed = 0;
		/* row and col are for the first clicked tile to
		 * make sure a bomb isn't placed there */
		board[row][col] = new MineSweeperTile(row, col);
		createBombs(row, col); // places all the bombs in the board after the first press

		// now places unguessed tiles without bombs
        for (int r= 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
            	if (board[r][c] == null)
            		board[r][c] = new MineSweeperTile(r, c);

		for (MineSweeperTile[] tileRow : board)
			for (MineSweeperTile tile : tileRow)
				tile.updateCount(board);
    }
	
	/**
	 * Updates the tile to the indicated status parameter.
	 * 
	 * @param row is the y coord of the tile to be updated
	 * @param col is the x coord of the tile to be updated
	 * @param status is the new status of the tile
	 */
	public void updateTileStatus(int row, int col, GUESS_STATUS status) {
		board[row][col].setStatus(status);
		notifyObservers();
	}
	
	/**
	 * Method to return the entire board
	 * @return the board object as a MineSweeperTile 2D array
	 */
	public MineSweeperTile[][] getBoard() {
		return board;
	}
	
	/**
	 * Sets the board to an existing board, from a loaded file.
	 */
	public void setBoard(MineSweeperTile[][] newBoard) {
		board = newBoard;
	}
	
	/**
	 * Adds an observer to notify of changes.
	 * 
	 * @param o The observer to add, presumably a view object.
	 */
	public void addObserver(Observer o) {
		observers.add(o);
	}
	
	/**
	 * This method notifies observers when the board has changed.
	 */
	public void notifyObservers() {
		for (Observer o : observers) {
			o.update(this, this.board);
		}
	}
	
	/**
	 * This method places bombs in random locations once the user clicks on the board for the first time.
	 * 
	 * It will never place them within 2 hexes of the user's first click, to ensure the game is playable
	 * and that the first click gives useful information.
	 * 
	 * @param startRow The row that the user originally clicked.
	 * @param startCol The column that the user originally clicked.
	 */
	public void createBombs(int startRow, int startCol) {
		int i = 0;
		while (i < numBombs) {
			int row = (int)(Math.random() * rows );
			int col = (int)(Math.random() * cols);

			int diffRow = Math.abs(startRow - row);
			int diffCol = Math.abs(startCol - col); // make sure we start with a 0
			if (board[row][col] != null || (diffRow < 2 && diffCol < 2)) continue;
			
			board[row][col] = new MineSweeperTile(row, col, GUESS_STATUS.UNGUESSED);
			board[row][col].setBomb();
			i++;
		}
	}

	/**
	 * Adds a certain number of milliseconds to the elapsed time.
	 * Then calculates the number of seconds that have elapsed in the game, and returns it as a double.
	 * 
	 * @return The number of elapsed seconds, as a double.
	 */
	public double getSecondsElapsed() {
		if (ms_elapsed == -1) return 0;
		ms_elapsed += timeInc;
		return ms_elapsed/1000.0;
	}
	
	public void disableTimer() {
		timeInc = 0;
	}
	
	public void enableTimer() {
		timeInc = DELTA_TIME_MS;
	}
	
	/**
	 * Updates the number of milliseconds elapsed to match an input in seconds.
	 * 
	 * @param elapsed An integer number of seconds to update the timer to.
	 */
	public void setSecondsElapsed(int elapsed) {
		ms_elapsed = (int) (elapsed*1000.0);
	}

	/**
	 * @return This game's difficulty setting, expressed as a string (like "Easy" or "Very Hard").
	 */
	public String getDifficulty() {
		return difficulty;
	}

	/**
	 * Sets the game's difficulty. This setting can't affect a game already in progress,
	 * as the grid and mines have already been fully set by then.
	 * 
	 * @param difficulty A new difficulty to set this game to, expressed as a string (like "Easy" or "Very Hard").
	 */
	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}

	/**
	 * @return The number of rows in the Minesweeper board.
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * @return The number of columns in the Minesweeper board.
	 */
	public int getCols() {
		return cols;
	}

	/**
	 * Returns the numBombs variable, which stores the number of bombs on the board.
	 * 
	 * Note that this is the intended number of bombs to place, before game setup,
	 * and not the number that actually *were* placed (if, somehow, it might be different).
	 * 
	 * @return The number of bombs on the Minesweeper board.
	 */
	public int getNumBombs() {
		return numBombs;
	}
	
}
