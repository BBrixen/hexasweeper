package Models;

import java.io.Serializable;
import java.util.*; // TODO: dont import *
import Utils.GUESS_STATUS;
import static View.MineSweeper.COLS;
import static View.MineSweeper.NUM_BOMBS;
import static View.MineSweeper.ROWS;

@SuppressWarnings("deprecation")
public class MineSweeperBoard extends Observable implements Serializable {

	// the minesweeper board as a 2D array
	private MineSweeperTile[][] board;
	private final List<Observer> observers; // do we need a list?
	
	/**
	 * Constructor for MineSweeperBoard model object.
	 */
	public MineSweeperBoard() {
		//initializes the board as a ROWS x COLS 2D array with null pointers for now
		board = new MineSweeperTile[ROWS][COLS];
		observers = new ArrayList<>();
	}
	
	/**
	 * Places all the mines and unmined tiles once the player clicks the
	 * first tile.
	 * 
	 * @param row is the y coord of the first tile clicked
	 * @param col is the x coord of the first tile clicked
	 */
	public void createBoard(int row, int col) {
		/* row and col are for the first clicked tile to
		 * make sure a bomb isn't placed there */
		board[row][col] = new MineSweeperTile(row, col);
		createBombs(row, col); // places all the bombs in the board after the first press

		// now places unguessed tiles without bombs
        for (int r= 0; r < ROWS; r++) 
            for (int c = 0; c < COLS; c++)
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
		while (i < NUM_BOMBS) {
			int row = (int)(Math.random() * ROWS );
			int col = (int)(Math.random() * COLS);

			int diffRow = Math.abs(startRow - row);
			int diffCol = Math.abs(startCol - col); // make sure we start with a 0
			if (board[row][col] != null || (diffRow < 2 && diffCol < 2)) continue;
			
			board[row][col] = new MineSweeperTile(row, col, GUESS_STATUS.UNGUESSED);
			board[row][col].setBomb();
			i++;
		}
	}
}
