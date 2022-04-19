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
    }
    
    public MineSweeperTile(int row, int col) {
    	this.row = row;
    	this.col = col;
    	status = GUESS_STATUS.UNGUESSED;
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
    }

}
