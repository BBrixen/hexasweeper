package Controllers;

import java.util.Observer;

import Models.MineSweeperBoard;
import Models.MineSweeperTile;
import Utils.GUESS_STATUS;

import static View.MineSweeper.COLS;
import static View.MineSweeper.NUM_BOMBS;
import static View.MineSweeper.ROWS;

public class MineSweeperController {

	private MineSweeperBoard model;
	// tracks if game is over
	private boolean gameOver;
	// keeps track of the total number of guesses
	private int numberOfGuesses;
	// total number of bombs in the board
	private final int NUM_BOMBS;
	private final int ROWS;
	private final int COLS;
	private boolean win;
	private MineSweeperTile[][] board;
	
	/**
	 * Contructor for the controller.
	 * numBombs is used as a parameter for constructing the model.
	 * @param numBombs 
	 * @param cols 
	 * @param rows 
	 */
	public MineSweeperController(int rows, int cols, int numBombs) {
		NUM_BOMBS = numBombs;
		ROWS = rows;
		COLS = cols;
		this.model = new MineSweeperBoard(ROWS, COLS, NUM_BOMBS);
		// will change to "false" if a bomb is clicked
		win = true;
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
		if (!gameOver) { // first determines if the game is over
			board = model.getBoard(); // gets the board from the model
			
			/* determines if the board is still null (indicates that this is the player's
			 * first click of the game.
			 */
			if (board[row][col] == null) {
				model.createBoard(row, col); // creates the board and places all bombs
				updateTileStatus(row, col, status); // updates the board with the player's click 
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
			if (numberOfGuesses == (board.length*board.length) - NUM_BOMBS) {
				showAllBombs();
				gameIsOver();
			}
		}
		}
	
	private void checkAdjacent(int row, int col) {
		int[] adjR = {0,0,1,1,-1,-1};
		int[] adjC = {-1,1,0,1,0,1};
		if (row%2 == 0) {
			adjC[2] = -1;
			adjC[3] = 0;
			adjC[4] = -1;
			adjC[5] = 0;
			}
		for (int i = 0; i < adjR.length; i++) {
			checkNonBomb(row, adjR[i], col, adjC[i]);
		}	
	}

	private void checkNonBomb(int r, int a, int c, int b) {
		if (r+a >= 0 && r+a < ROWS && c+b >= 0 && c+b < COLS) {
			if (!(board[r+a][c+b].isBomb())){
				updateTileStatus(r+a, c+b, GUESS_STATUS.GUESSED);}
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
            for (int col = 0; col < board.length; col++) {
            	// checks if the tile is a bomb
            	if (board[row][col].isBomb())
            		// sets the bomb tile color if true
					model.updateTileStatus(row, col, GUESS_STATUS.BOMB);
		}
      } // sets gameOver to true
		gameIsOver();
		
	}

	/**
	 * Adds an observer to the model's observer list
	 * @param o is the observer to be added. Most likely a view object
	 */
	public void addObserver(Observer o) {
		model.addObserver(o);
	}
	
	/**
	 * Checks if the game is over.
	 * @return boolean indicating if the game is over
	 */
	public boolean isGameOver() {
		return gameOver;
	}
	
	/**
	 * Sets gameOver to true and call's the model's notify method.
	 */
	public void gameIsOver() {
		gameOver = true;
		model.notifyObservers();
	}

	/**
	 * Returns a boolean indicating if the player won the game
	 * @return boolean if the player won
	 */
	public boolean win() {
		return win;
	}

}
