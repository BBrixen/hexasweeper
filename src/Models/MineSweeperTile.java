package Models;

import Utils.GUESS_STATUS;
import javafx.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class stores all of the information about a single tile on the Minesweeper board.
 * 
 * A tile needs to know whether it has been guessed, unguessed, flagged, or has a bomb revealed,
 * which is stored in its GUESS_STATUS variable. It also needs to know how many adjacent bombs there are,
 * so it can reveal the number if it is clicked on. Finally, it stores its own coordinates
 * and whether it has a bomb on itself.
 *
 */
public class MineSweeperTile implements Serializable{

    private GUESS_STATUS status;
    
    /**
     * The number of tiles adjacent to this one that contain mines.
     */
    private int mineCount;
    private final int row, col;
    
    /** Indicates if the tile contains a bomb,
     *  and thus whether or not the number of bombs around it should be shown.
     */
    private boolean bomb;

    /**
     * Creates a new tile at a given coordinate and with a certain status.
     * 
     * @param row The row to place this tile at.
     * @param col The column to place this tile at.
     * @param status The guess status to give this tile when placing it.
     */
    public MineSweeperTile(int row, int col, GUESS_STATUS status) {
    	this.row = row;
    	this.col = col;
    	this.status = status;
        mineCount = 0;
    }
    
    /**
     * Constructor without a guess status, which defaults to creating an unguessed tile.
     * 
     * @param row The row to place this tile at.
     * @param col The column to place this tile at.
     */
    public MineSweeperTile(int row, int col) {
    	this(row, col, GUESS_STATUS.UNGUESSED);
	}

    /**
     * Counts up tiles which are adjacent to this one, and adds one to the mineCount variable
     * each time one of them contains a mine.
     * 
     * @param board The entire MineSweeperTile[][] array with the current board state,
     */
    public void updateCount(MineSweeperTile[][] board) {
    	for (Pair <Integer, Integer> pair : getAdjacentTiles()) {
            int row = pair.getKey();
            int col = pair.getValue();

            if (row >= 0 && row < board.length && col  >= 0
                    && col < board[row].length && board[row][col].isBomb())
                board[this.row][this.col].addMineCount();
        }
    }

    /**
     * Adds one to the labeled number of adjacent mines.
     */
    public void addMineCount() {
        mineCount = mineCount + 1;
    }

    // GETTERS AND SETTERS

    /**
     * Returns the coordinates of all tiles adjacent to this one.
     * These coordinates CAN be outside the bounds of the board,
     * so it is best to always check whether or not they are in bounds.
     *
     * @return A list of (x, y) coordinate pairs that the surrounding tiles have, expressed as an ArrayList of Integer Pairs.
     */
    public ArrayList<Pair<Integer, Integer>> getAdjacentTiles() {
        // creating variables
        ArrayList<Pair<Integer, Integer>> adjacents = new ArrayList<>();
        int[][] adj = {{0, -1},{0, 1},{1, 0},{1, 1},{-1, 0},{-1, 1}};
        if (row%2 == 0)
            adj = new int[][]{{0, -1}, {0, 1}, {1, -1}, {1, 0}, {-1, -1}, {-1, 0}};

        // filling and returning variables
        for (int[] tup : adj)
            adjacents.add(new Pair<>(row + tup[0], col + tup[1]));
        return adjacents;
    }

    /**
     * Sets the current status of this specific tile
     * @param status The new guess status of this tile (guessed, unguessed, flagged, mine revealed).
     */
	public void setStatus(GUESS_STATUS status) {
    	this.status = status;
    }
    
	/**
     * Gets the current status of this specific tile
	 * @return The guess status of this tile (guessed, unguessed, flagged, mine revealed).
	 */
    public GUESS_STATUS getStatus() {
    	return status;
    }
    
    /**
     * Checks if this tile is a bomb
     * @return true if this is a bomb, false otherwise
     */
    public boolean isBomb() {
    	return bomb;
    }
    
    /**
     * Mark that this tile contains a mine.
     * 
     * This method also sets the mineCount to 0, since the number of adjacent mines should never be displayed on a mine tile.
     */
    public void setBomb() {
    	bomb = true;
        mineCount = 0; // 0 because we dont count the number of bombs around other bombs
    }

    /**
     * Gets the number of bombs around this tile
     * @return The number of adjacent mines.
     */
    public int getMineCount() {
    	return mineCount;
    }
}
