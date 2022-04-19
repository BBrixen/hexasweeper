package Models;

import Utils.GUESS_STATUS;

public class MineSweeperTile {

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

    
    /* I did some strange math here. adjR and adjC together make
	 * a coord pair for all adjacent tiles. I'm sure there is a cleaner/
	 * more logical way to do this. I'll try to format it differently.
	 * This method is used to update each tile's mineCount.
	 * 
	 * (I used similar math in the controller in the checkAdjacent() method)
	 */
    public void updateCount(MineSweeperTile[][] board) {
    	int[] adjR = {0,0,1,1,-1,-1};
		int[] adjC = {-1,1,0,1,0,1};
		if (row%2 == 0) {
			adjC[2] = -1;
			adjC[3] = 0;
			adjC[4] = -1;
			adjC[5] = 0;
			}
		for (int i = 0; i < adjR.length; i++) {
			if (row+adjR[i] >= 0 && row+adjR[i] < board.length && col+adjC[i] >= 0 && col+adjC[i] < board.length) {
				if (board[row+adjR[i]][col+adjC[i]].isBomb()) {
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
