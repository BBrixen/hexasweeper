# MineSweeper

This implementation of the game of Minesweeper uses an interesting hexagonal grid.

To play the game, run MineSweeper.class after compiling, or run MineSweeper.java directly through Eclipse.
You will be prompted to select a difficulty; higher difficulties of the game have larger boards with more densely packed mines.
At the top of the board is a timer for the current game; the pause/play button at the bottom will stop the timer but also obscure the screen, to prevent cheating.

Left-clicking a hexagon will reveal the tile underneath, which will display the number of adjacent mines.
The first click is guaranteed to have no adjacent mines, and will thus reveal more nearby tiles in a chain.

Right-clicking a hexagon will mark it with a flag, a useful indicator that you believe a mine might be there.
The number of flags you've placed is tracked at the top of the screen, next to the number of mines.

Double-clicking an already-revealed hexagon will forcibly reveal every adjacent tile that is not flagged.
Be sure that you've flagged the correct positions where mines are, or you're very likely to set them off in the process!

You may save your games to .txt files anywhere on your computer, and then load from them later.
Doing this preserves the state of that game's timer, and all of the progress you've made.

At the left side of the screen is a scoreboard for the current difficulty of the game you're playing.
The top five scores for each difficulty are saved in the file "scores.txt", which is in the same directory as this readme.

### Notes

Creating testcases for this was extremely difficult. 
This is primarily due to the fact that the board is completely randomized after the first click, and afterwards it relies mainly on educated gameplay. 
Thus, our testcases did not cover as many branches inside the model as we would hope. 
Most of our testing was done by us, by playing the game. 
Obviously this is not ideal, but also creating intelligent testcases to properly guess which tiles are which seemed far too difficult. 

There are 2 error messages for saving and loading. These probably dont look pretty and should hopefully never trigger (especially the saving error). 
Loading should trigger if the user attempts to load an invalid file