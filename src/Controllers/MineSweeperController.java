package Controllers;

import java.util.Observer;
import Models.MineSweeperBoard;
import Models.MineSweeperTile;
import Utils.GUESS_STATUS;
import static View.MineSweeper.COLS;
import static View.MineSweeper.NUM_BOMBS;
import static View.MineSweeper.ROWS;

public class MineSweeperController {

	private final MineSweeperBoard model;
	private boolean gameOver; // tracks if game is over
	private int numberOfGuesses; // keeps track of the total number of guesses
	private boolean win;
	
	/**
	 * Contructor for the controller.
	 * numBombs is used as a parameter for constructing the model.
	 */
	public MineSweeperController() {
		this.model = new MineSweeperBoard();
		win = true; // keeps track of the total number of guesses
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
		// the 2nd line is a guard clause for updating tiles off the board,
		// it is needed for the double click (reveal cleared tile)
		MineSweeperTile[][] board = model.getBoard();
		if (gameOver ||
				(board != null && (row >= board.length || row < 0 || col >= board[row].length || col < 0))) return;


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
		if (numberOfGuesses == (board.length*board[0].length) - NUM_BOMBS) {
			showAllBombs();
			gameIsOver();
		}

	}

	public void updateTilesAround(int row, int col, GUESS_STATUS status) {
		// TODO: make this better
		int[][] adj = {{0, -1},{0, 1},{1, 0},{1, 1},{-1, 0},{-1, 1}};
		int[][] adjEven = {{0, -1},{0, 1},{1, -1},{1, 0},{-1, -1},{-1, 0}};
		if (row%2 == 0)
			adj = adjEven;
		for (int i = 0; i < adj.length; i++) {
			updateTileStatus(row + adj[i][0], col + adj[i][1], status);
		}
	}
	
    /* I did some strange math here. adj is cardinal directions for
	 * coord pairs for all adjacent tiles in odd rows. 
	 * adjEven is for even rows. I'm sure there is a cleaner/
	 * more logical way to do this. I'll try to format it differently.
	 * This method is used to reveal adjacent tiles. updateAdjacentTiles()
	 * will continue to call this method automatically until there are no
	 * longer adjacent tiles with a mineCount of 0.
	 */
	private void checkAdjacent(int row, int col) {
		int[][] adj = {{0, -1},{0, 1},{1, 0},{1, 1},{-1, 0},{-1, 1}};
    	int[][] adjEven = {{0, -1},{0, 1},{1, -1},{1, 0},{-1, -1},{-1, 0}};
		if (row%2 == 0) 
			adj = adjEven;
		for (int i = 0; i < adj.length; i++) {
			checkNonBomb(row, adj[i][0], col, adj[i][1]);
		}	
	}

	/* Checks if adjacent tiles are bombs. If they are not, 
	 * the tiles are revealed. 
	 */
	private void checkNonBomb(int r, int a, int c, int b) {
		if (r+a >= 0 && r+a < ROWS && c+b >= 0 && c+b < COLS) {
			if (!(model.getBoard()[r+a][c+b].isBomb())){
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
		MineSweeperTile[][] board = model.getBoard();

		for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board.length; col++) {
            	if (board[row][col].isBomb())
            		// if the tile is a bomb, set the color of the tile to a bomb
					model.updateTileStatus(row, col, GUESS_STATUS.BOMB);
			}
      	}
		gameIsOver(); // the game is over, so we update the controller to reflect this
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
