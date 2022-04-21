package Models;

import Utils.GUESS_STATUS;
import javafx.util.Pair;

import java.util.ArrayList;

public class MineSweeperTile {

    private GUESS_STATUS status;
    private int mineCount; // the number of mines around this
    private final int row, col;
    // indicates if the tile contains a bomb,
    // and whether or not the number of bombs around it should be shown or hidden
    private boolean bomb;

    public MineSweeperTile(int row, int col, GUESS_STATUS status) {
    	this.row = row;
    	this.col = col;
    	this.status = status;
        mineCount = 0;
    }
    
    public MineSweeperTile(int row, int col) {
    	this(row, col, GUESS_STATUS.UNGUESSED);
	}

    
    /* I did some strange math here. adj is cardinal directions for
	 * coord pairs for all adjacent tiles in odd rows. 
	 * adjEven is for even rows. I'm sure there is a cleaner/
	 * more logical way to do this. I'll try to format it differently.
	 * This method is used to update each tile's mineCount.
	 * 
	 * (I used similar math in the controller in the checkAdjacent() method)
	 */
    public void updateCount(MineSweeperTile[][] board) {
    	for (Pair <Integer, Integer> pair : getAdjacentTiles()) {
            int row = pair.getKey();
            int col = pair.getValue();

            if (row >= 0 && row < board.length && col  >= 0
                    && col < board.length && board[row][col].isBomb())
                board[this.row][this.col].addMineCount();
        }
    }

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

	public void setStatus(GUESS_STATUS status) {
    	this.status = status;
    }
    
    public GUESS_STATUS getStatus() {
    	return status;
    }
    
    public boolean isBomb() {
    	return bomb;
    }
    
    public void setBomb() {
    	bomb = true;
        mineCount = 0; // 0 because we dont count the number of bombs around other bombs
    }

    public int getMineCount() {
    	return mineCount;
    }
    
    public void addMineCount() {
    	mineCount = mineCount + 1;
    }
}
