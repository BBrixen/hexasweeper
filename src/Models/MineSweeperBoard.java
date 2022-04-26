package Models;

import java.io.Serializable;
import java.util.*; // TODO: dont import *
import Utils.GUESS_STATUS;

import static View.MineSweeper.*;

@SuppressWarnings("deprecation")
public class MineSweeperBoard extends Observable implements Serializable {

	// the minesweeper board as a 2D array
	private MineSweeperTile[][] board;
	private final List<Observer> observers; // do we need a list?
	private int ms_elapsed;
	private final int numBombs;
	private String difficulty;

	private int rows = 16, cols = 24;
	private static final int VERY_HARD_DIVIDER = 3;
	private static final int HARD_DIVIDER = 4;
	private static final int NORMAL_DIVIVER = 5;
	private static final int EASY_DIVIDER = 8;
	private static final int VERY_EASY_DIVIDER = 20;
	
	/**
	 * Constructor for MineSweeperBoard model object.
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
	 * Adds any observers to notify of changes
	 */
	public void addObserver(Observer o) {
		observers.add(o);
	}
	
	/**
	 * This method notifies observers when the board has changed
	 */
	public void notifyObservers() {
		for (Observer o : observers) {
			o.update(this, this.board);
		}
	}
	
	/**
	 * This method places bombs in random locations if the tile is null.
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
	 * Calculates the number of seconds that have elapsed in the game
	 * @return the number of seconds elapsed
	 */
	public double getSecondsElapsed() {
		if (ms_elapsed == -1) return 0;
		ms_elapsed += TIME_INCR;
		return ms_elapsed/1000.0;
	}
	
	/**
	 * Calculates the number of seconds that have elapsed in the game
	 */
	public void setSecondsElapsed(int elapsed) {
		ms_elapsed = (int) (elapsed*1000.0);
	}

	public String getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}

	public int getRows() {
		return rows;
	}

	public int getCols() {
		return cols;
	}

	public int getNumBombs() {
		return numBombs;
	}
}
