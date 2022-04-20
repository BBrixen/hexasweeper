package Models;

import java.io.Serializable;

import Utils.GUESS_STATUS;

public class MineSweeperTile implements Serializable {

	private static final long serialVersionUID = 100L;
	
	
	private GUESS_STATUS status;
    private int mineCount; // the number of mines around this
    private final int row, col;
    // indicates if the tile contains a bomb,
    // and whether or not the number of bombs around it should be shown or hidden
    private boolean bomb, displayNum;

    public MineSweeperTile(int row, int col, GUESS_STATUS status) {
    	this.row = row;
    	this.col = col;
    	this.status = status;
        displayNum = false;
        mineCount = 0;

        // now we calculate the number of bombs around this tile
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
    	int[][] adjEven = {{0, -1},{0, 1},{1, -1},{1, 0},{-1, -1},{-1, 0}};
		if (row%2 == 0) 
			adj = adjEven;
		for (int i = 0; i < adj.length; i++) {
			if (row+adj[i][0] >= 0 && row+adj[i][0] < board.length && col+adj[i][1] >= 0 && col+adj[i][1] < board.length) {
				if (board[row+adj[i][0]][col+adj[i][1]].isBomb()) {
				board[row][col].addMineCount();}
			}
		}
    }

	public void setStatus(GUESS_STATUS status) {
    	this.status = status;
    }
    
    public GUESS_STATUS getStatus() {
    	return status;
    }
    
    public int getRow() {
    	return row;
    }
    
    public int getCol() {
    	return col;
    }
    
    public boolean isBomb() {
    	return bomb;
    }
    
    public void setBomb() {
    	bomb = true;
        mineCount = 0; // 0 means it wont be displayed
        displayNum = false;
    }

    public boolean isDisplayNum() {
        return displayNum;
    }

    public void setDisplayNum(boolean displayNum) {
        this.displayNum = displayNum;
    }

    public String getMineLabel() {
        // TODO: this conditional is turned off for testing, turn it back on later for proper minesweeper displays
//        if (! displayNum || mineCount == 0)
//            return ""; // we dont want to display it
        return ""+mineCount;
    }
    public int getMineCount() {
    	return mineCount;
    }
    
    public void addMineCount() {
    	mineCount = mineCount + 1;
    }
}
