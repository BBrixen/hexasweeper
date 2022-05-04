package Utils;

import org.junit.jupiter.api.Test;

import Controllers.MineSweeperController;
import Models.MineSweeperBoard;
import Models.MineSweeperTile;
import Utils.GUESS_STATUS.*;

public class Tests {

    @Test
    void testBoardCreation() {
    	MineSweeperController controller = new MineSweeperController("Normal");
    	assert(controller.getCols() == 24);
    	assert(controller.getRows() == 16);
    	MineSweeperTile[][] board = controller.getBoard();
    	for (int i = 0; i < board.length; i++) {
    		for (int j = 0; j < board[i].length; j++) {
    			assert(board[i][j] == null);
    		}
    	}
    }
    
    @Test
    void testBoardVeryEasy() {
    	MineSweeperController controller = new MineSweeperController("Very Easy");
    	assert(controller.getCols() == 16);
    	assert(controller.getRows() == 16);
    	MineSweeperTile[][] board = controller.getBoard();
    	for (int i = 0; i < board.length; i++) {
    		for (int j = 0; j < board[i].length; j++) {
    			assert(board[i][j] == null);
    		}
    	}
    }
    
    @Test
    void testBoardHard() {
    	MineSweeperController controller = new MineSweeperController("Hard");
    	assert(controller.getCols() == 24);
    	assert(controller.getRows() == 20);
    	MineSweeperTile[][] board = controller.getBoard();
    	for (int i = 0; i < board.length; i++) {
    		for (int j = 0; j < board[i].length; j++) {
    			assert(board[i][j] == null);
    		}
    	}
    }
    
    @Test
    void testBoardVeryHard() {
    	MineSweeperController controller = new MineSweeperController("Very Hard");
    	assert(controller.getCols() == 30);
    	assert(controller.getRows() == 24);
    	MineSweeperTile[][] board = controller.getBoard();
    	for (int i = 0; i < board.length; i++) {
    		for (int j = 0; j < board[i].length; j++) {
    			assert(board[i][j] == null);
    		}
    	}
    }
    
    @Test
    void testBoardInitialization() {
    	MineSweeperController controller = new MineSweeperController("Normal");
    	assert(controller.getCols() == 24);
    	assert(controller.getRows() == 16);
    	MineSweeperTile[][] board = controller.getBoard();
    	controller.updateTileStatus(0, 0, GUESS_STATUS.GUESSED);
    	for (int i = 0; i < board.length; i++) {
    		for (int j = 0; j < board[i].length; j++) {
    			assert(board[i][j].getStatus() != GUESS_STATUS.BOMB);
    		}
    	}
    }
    
    @Test
    void testBombCount() {
    	MineSweeperController controller = new MineSweeperController("Normal");
    	assert(controller.getCols() == 24);
    	assert(controller.getRows() == 16);
    	MineSweeperTile[][] board = controller.getBoard();
    	controller.updateTileStatus(0, 0, GUESS_STATUS.GUESSED);
    	int numBombs = 0;
    	for (int i = 0; i < board.length; i++) {
    		for (int j = 0; j < board[i].length; j++) {
    			if (board[i][j].isBomb()) {
    				numBombs++;
    			}
    		}
    	}
    	assert (numBombs == controller.getBombCount());
    }
    
    @Test
    void testTile() {
    	MineSweeperTile tile = new MineSweeperTile(0, 0, GUESS_STATUS.UNGUESSED);
    	tile.setBomb();
    	assert(tile.isBomb());
    	tile.setStatus(GUESS_STATUS.FLAGGED);
    	assert(tile.getStatus() == GUESS_STATUS.FLAGGED);
    }
    @Test
    void testPausing() {
    	MineSweeperController controller = new MineSweeperController("Normal");
    	controller.disableTimer();
    	assert(controller.isGamePaused());
    	controller.enableTimer();
    	assert(!controller.isGamePaused());
    	
    }
    
    @Test
    void testTimerCreation() {
    	// Nothing to really assert, just ensure no runtime errors
    	MineSweeperController controller = new MineSweeperController("Easy");
    	assert(controller.getCols() == 20);
    	assert(controller.getRows() == 16);
    	controller.createTimer(() -> {return;});
    	controller.enableTimer();
    	
    }

}
