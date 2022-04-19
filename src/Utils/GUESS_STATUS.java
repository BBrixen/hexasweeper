package Utils;

import javafx.scene.paint.Color;

/**
 * @author Bennett Brixen
 *
 * This represents the current status of a single position on the board
 * It can either be UNGUESSED/SHIP, which means this position has not been guessed
 * or it can be HIT/MISS which is the result of guessing in that location
 *
 * This also stores a javafx background for the color of this position
 */
public enum GUESS_STATUS {

    /**
     * The defined COORD_STATUSes
     *
     * UNGUESSED/SHIP => has not been guessed
     * MISS/HIT => result of guess
     *
     * the results follow this pattern:
     * UNGUESSED -> MISS
     * SHIP -> HIT
     */
    UNGUESSED(Color.GREY),
    GUESSED(Color.WHITE),
    FLAGGED(Color.GREEN),
    BOMB(Color.BLACK);
    private final Color color;

    /**
     * This creates a GUESS_STATUS
     * @param color - the color, stored as a javafx color
     */
    GUESS_STATUS(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
