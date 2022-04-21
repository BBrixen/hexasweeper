package Models;

import Utils.GUESS_STATUS;

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
    	int[][] adj = {{0, -1},{0, 1},{1, 0},{1, 1},{-1, 0},{-1, 1}};
		if (row%2 == 0)
            adj = new int[][]{{0, -1}, {0, 1}, {1, -1}, {1, 0}, {-1, -1}, {-1, 0}};

        for (int[] ints : adj)
            if (row + ints[0] >= 0 && row + ints[0] < board.length && col + ints[1] >= 0
                    && col + ints[1] < board.length && board[row + ints[0]][col + ints[1]].isBomb())
                board[row][col].addMineCount();
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
