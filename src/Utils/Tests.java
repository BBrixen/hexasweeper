package Utils;

import Models.ScoreBoard;
import javafx.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import Controllers.MineSweeperController;
import Models.MineSweeperTile;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static Utils.GUESS_STATUS.*;


public class Tests {

	@Test
	void testBoardNormal() {
		MineSweeperController controller = new MineSweeperController("Normal");
		assert(controller.getCols() == 24);
		assert(controller.getRows() == 16);
		MineSweeperTile[][] board = controller.getBoard();
		for (MineSweeperTile[] tiles : board) {
			for (MineSweeperTile tile : tiles) {
				Assertions.assertNull(tile);
			}
		}
	}

	@Test
	void testBoardVeryEasy() {
		MineSweeperController controller = new MineSweeperController("Very Easy");
		Assertions.assertEquals(controller.getCols(),16);
		Assertions.assertEquals(controller.getRows(), 16);
		MineSweeperTile[][] board = controller.getBoard();
		for (MineSweeperTile[] tiles : board) {
			for (MineSweeperTile tile : tiles) {
				Assertions.assertNull(tile);
			}
		}
	}

	@Test
	void testBoardHard() {
		MineSweeperController controller = new MineSweeperController("Hard");
		Assertions.assertEquals(controller.getCols(), 24);
		Assertions.assertEquals(controller.getRows(), 20);
		MineSweeperTile[][] board = controller.getBoard();
		for (MineSweeperTile[] tiles : board) {
			for (MineSweeperTile tile : tiles) {
				Assertions.assertNull(tile);
			}
		}
	}

	@Test
	void testBoardVeryHard() {
		MineSweeperController controller = new MineSweeperController("Very Hard");
		Assertions.assertEquals(controller.getCols(), 30);
		Assertions.assertEquals(controller.getRows(), 24);
		MineSweeperTile[][] board = controller.getBoard();
		for (MineSweeperTile[] tiles : board) {
			for (MineSweeperTile tile : tiles) {
				Assertions.assertNull(tile);
			}
		}
	}

	@Test
	void testBoardInitialization() {
		MineSweeperController controller = new MineSweeperController("Normal");
		Assertions.assertEquals(controller.getCols(), 24);
		Assertions.assertEquals(controller.getRows(), 16);
		MineSweeperTile[][] board = controller.getBoard();
		controller.updateTileStatus(0, 0, GUESSED);
		for (MineSweeperTile[] tiles : board) {
			for (MineSweeperTile tile : tiles) {
				Assertions.assertNotEquals(tile.getStatus(), GUESS_STATUS.BOMB);
			}
		}
	}

	@Test
	void testBombCount() {
		MineSweeperController controller = new MineSweeperController("Normal");
		Assertions.assertEquals(controller.getCols(), 24);
		Assertions.assertEquals(controller.getRows(), 16);
		MineSweeperTile[][] board = controller.getBoard();
		controller.updateTileStatus(0, 0, GUESSED);
		int numBombs = 0;
		for (MineSweeperTile[] tiles : board) {
			for (MineSweeperTile tile : tiles) {
				if (tile.isBomb()) {
					numBombs++;
				}
			}
		}
		assert (numBombs == controller.getBombCount());
	}

	@Test
	void testTile() {
		MineSweeperTile tile = new MineSweeperTile(0, 0, UNGUESSED);
		tile.setBomb();
		Assertions.assertTrue(tile.isBomb());
		tile.setStatus(FLAGGED);
		Assertions.assertSame(tile.getStatus(), FLAGGED);
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
		Assertions.assertEquals(20, controller.getCols());
		Assertions.assertEquals(controller.getRows(), 16);
		controller.createTimer(() -> {
		});
		controller.enableTimer();

	}

	int SEED = 57;
	@Test
	void testNumberTiles() {
		// this will check that the number of tiles generated is what we expect
		Random random = new Random(SEED);
		MineSweeperController controller = new MineSweeperController("Normal");
		controller.seedBoardGeneration(0, 0, random);
		MineSweeperTile[][] board = controller.getBoard();

		// inset by 2 because i dont want to deal with out of bounds checking
		for (int row = 2; row < controller.getRows()-2; row ++) {
			for (int col = 2; col < controller.getCols()-2; col ++) {
				MineSweeperTile tile = board[row][col];
				if (tile == null) continue;
				int numExpected = tile.getMineCount();
				int actual = 0;

				for (Pair<Integer, Integer> coord : tile.getAdjacentTiles()) {
					int r = coord.getKey();
					int c = coord.getValue();
					if (board[r][c] != null && board[r][c].isBomb()) actual ++;
				}

				Assertions.assertEquals(numExpected, actual);
			}
		}
	}

	@Test
	void testScoreBoard() {
		MineSweeperController controller = new MineSweeperController("Very Easy");
		ScoreBoard board = controller.getScoreBoard();
		board.addNewTime(5.75, "Very Easy", true);

		Double[] times = board.getTopTimes("Very Easy");
		boolean contains = false;
		for (Double time : times)
			if (time == 5.75) {
				contains = true;
				break;
			}

		Assertions.assertTrue(contains);
	}

	@Test
	void testNumberFlags() {
		// this will check that the number of flags we place
		Random random = new Random(SEED);
		MineSweeperController controller = new MineSweeperController("Normal");
		controller.seedBoardGeneration(0, 0, random);
		controller.updateTileStatus(4, 4, FLAGGED);
		controller.updateTileStatus(3, 6, FLAGGED);
		controller.updateTileStatus(2, 2, FLAGGED);
		Assertions.assertEquals(controller.getMineCount(), "3 / 76");
	}

	@Test
	void testSecondsElapsed() {
		// make sure time increases correctly
		Random random = new Random(SEED);
		MineSweeperController controller = new MineSweeperController("Normal");
		controller.seedBoardGeneration(0, 0, random);

		controller.enableTimer();
		Assertions.assertEquals(0.01, controller.getSecondsElapsed());
		Assertions.assertEquals(0.02, controller.getSecondsElapsed());
		controller.shutdownTimer();
		Assertions.assertFalse(controller.isGameOver());

	}

	@Test
	void testClickTileAndLose() {
		// tests the different outcomes that we expect when clicking on a tile
		Random random = new Random(SEED);
		MineSweeperController controller = new MineSweeperController("Normal");
		controller.seedBoardGeneration(0, 0, random);

		MineSweeperTile[][] board = controller.getBoard();
		boolean canClickBomb = false; // we set this to true so we can end the game and test losing conditions
		boolean clickedBomb = false;

		// inset by 2 because i dont want to deal with out of bounds checking
		for (int row = 2; row < controller.getRows()-2; row ++) {
			for (int col = 2; col < controller.getCols()-2; col ++) {
				MineSweeperTile tile = board[row][col];
				if (tile == null) continue;

				if (canClickBomb && board[row][col].isBomb()) {
					controller.updateTileStatus(row, col, GUESSED);
					Assertions.assertTrue(controller.isGameOver());
					Assertions.assertFalse(controller.win());
					clickedBomb = true;

				} else if (board[row][col].getStatus() == UNGUESSED) {
					controller.updateTileStatus(row, col, FLAGGED);
					Assertions.assertTrue(board[row][col].getStatus() == UNGUESSED
							|| board[row][col].getStatus() == FLAGGED);

					controller.updateTileStatus(row, col, FLAGGED);
					Assertions.assertEquals(UNGUESSED, board[row][col].getStatus());
					canClickBomb = true;
				}
			}
		}

		Assertions.assertTrue(canClickBomb);
		Assertions.assertTrue(clickedBomb);
	}

	@Test
	void testSaveLoad() throws IOException, ClassNotFoundException {
		Random random = new Random(SEED);
		MineSweeperController controller = new MineSweeperController("Normal");
		controller.seedBoardGeneration(0, 0, random);
		controller.updateTileStatus(4, 4, FLAGGED);
		controller.updateTileStatus(3, 6, FLAGGED);
		controller.updateTileStatus(2, 2, FLAGGED);
		Assertions.assertEquals(controller.getMineCount(), "3 / 76");

		File testFile = new File("TESTING_FILE.txt");
		controller.saveGame(testFile);

		MineSweeperController controller2 = new MineSweeperController(testFile);
		controller2.enableTimer();

		MineSweeperTile[][] board = controller.getBoard();
		MineSweeperTile[][] board2 = controller2.getBoard();

		for (int row = 0; row < controller.getRows(); row ++) {
			for (int col = 0; col < controller.getCols(); col ++) {
				// if EVERY tile has the same status, then everything must be equal
				Assertions.assertEquals(board[row][col].getStatus(), board2[row][col].getStatus());
			}
		}
	}

	@Test
	void testUpdateTilesAround() {
		// try the double click ability
		Random random = new Random(SEED);
		MineSweeperController controller = new MineSweeperController("Normal");
		controller.seedBoardGeneration(0, 0, random);
		MineSweeperTile[][] board = controller.getBoard();
		// 2, 7 has only 1 bomb around it at 1, 7

		MineSweeperTile tile = board[2][7];
		Assertions.assertFalse(tile.isBomb());
		Assertions.assertTrue(board[1][7].isBomb());

		// all around should be unguessed, exce
		for (Pair<Integer, Integer> coords : tile.getAdjacentTiles()) {
			int row = coords.getKey();
			int col = coords.getValue();
			Assertions.assertEquals(board[row][col].getStatus(), UNGUESSED);
		}

		// double click
		controller.updateTileStatus(1, 7, FLAGGED);
		controller.updateTilesAround(2, 7);

		// now all the sourrounding should be guessed
		for (Pair<Integer, Integer> coords : tile.getAdjacentTiles()) {
			int row = coords.getKey();
			int col = coords.getValue();
			if (row == 1 && col == 7) {
				Assertions.assertEquals(board[row][col].getStatus(), FLAGGED);
			} else {
				Assertions.assertEquals(board[row][col].getStatus(), GUESSED);
			}
		}
	}

}