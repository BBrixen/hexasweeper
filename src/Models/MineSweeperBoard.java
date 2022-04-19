package Models;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import Utils.GUESS_STATUS;
import static View.MineSweeper.COLS;
import static View.MineSweeper.NUM_BOMBS;
import static View.MineSweeper.ROWS;

@SuppressWarnings("deprecation")
public class MineSweeperBoard extends Observable {

	// the minesweeper board as a 2D array
	private MineSweeperTile[][] board;
	private List<Observer> observers;
	
	/**
	 * Constructor for MineSweeperBoard model object.
	 * @param numBombs 
	 * @param cols 
	 * @param rows 
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
		createBombs(); // places all the bombs in the board first

		// now places unguessed tiles without bombs
        for (int r= 0; r < ROWS; r++) 
            for (int c = 0; c < COLS; c++) {
            	if (board[r][c] == null)
            		board[r][c] = new MineSweeperTile(r, c);
			}

		for (MineSweeperTile[] tileRow : board) {
			for (MineSweeperTile tile : tileRow) {
				tile.updateCount(board);
			}
		}updateAllMineCounts();
    }

	/**
	 * This creates a grid of strings representing the mine count on each tile. We
	 * @return
	 */
	public String[][] generateCounts() {
		String[][] mineCounts = new String[ROWS][COLS];
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				mineCounts[row][col] = board[row][col].getMineLabel();
			}
		}
		return mineCounts;
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
	public void createBombs() {
		Random rand = new Random();
		int i = 0;

		while (i < NUM_BOMBS) {
			int r = rand.nextInt(ROWS);
			int c = rand.nextInt(COLS);

			if (board[r][c] != null)
				continue;
			board[r][c] = new MineSweeperTile(r, c, GUESS_STATUS.UNGUESSED);
			board[r][c].setBomb();
			i++;

		}
	} 
	
	/* I did some strange math here. adjR and adjC together make
	 * a coord pair for all adjacent tiles. I'm sure there is a cleaner/
	 * more logical way to do this. I'll try to format it differently.
	 * This method is used to update each tile's mineCount.
	 * 
	 * (I used similar math in the controller in the checkAdjacent() method)
	 */
	public void updateAllMineCounts() {
		for (int row = 0; row < ROWS; row++) 
			for (int col = 0; col < COLS; col++) {
				int[] adjR = {0,0,1,1,-1,-1};
				int[] adjC = {-1,1,0,1,0,1};
				if (row%2 == 0) {
					adjC[2] = -1;
					adjC[3] = 0;
					adjC[4] = -1;
					adjC[5] = 0;
					}
				for (int i = 0; i < adjR.length; i++) {
					updateCount(row, adjR[i], col, adjC[i]);
				}
			}
	}

	/* Adds one to the parent tile's mineCount if the adjacent
	 * tile contains a bomb.
	 */
	private void updateCount(int r, int a, int c, int b) {
		if (r+a >= 0 && r+a < ROWS && c+b >= 0 && c+b < COLS) {
			if (board[r+a][c+b].isBomb()) {
			board[r][c].addMineCount();}
		}
		
	}
}
