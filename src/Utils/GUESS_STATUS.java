package Utils;

import javafx.scene.paint.Color;

/**
 * @author Bennett Brixen
 *
 * This enum represents the current status of a single position on the board.
 * It can be UNGUESSED, for tiles that have not been revealed;
 * GUESSED, for tiles that have been revealed and are to display either a number of adjacent bombs or a zero (blank);
 * FLAGGED, for tiles that have been flagged;
 * or BOMB, for tiles with a revealed bomb (bombs are only revealed at the end of the game).
 *
 * It also stores a JavaFX Color to use when displaying this tile.
 */
public enum GUESS_STATUS {
	
    UNGUESSED(Color.GREY),
    GUESSED(Color.WHITE),
    FLAGGED(Color.GREEN),
    BOMB(Color.BLACK);
    private final Color color;

    /**
     * Creates a GUESS_STATUS.
     * 
     * @param color The Color to use for this guess status.
     */
    GUESS_STATUS(Color color) {
        this.color = color;
    }

    /**
     * @return The Color associated with this guess status.
     */
    public Color getColor() {
        return color;
    }
}
