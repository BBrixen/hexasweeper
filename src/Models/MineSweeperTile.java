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

        // now we calculate the number of bombs around this tile
    }
    
    public MineSweeperTile(int row, int col) {
    	this(row, col, GUESS_STATUS.UNGUESSED);
	}

    public void updateCount(MineSweeperTile[][] board) {
        // TODO: this will count the number of bombs around this tile on the board
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
}
