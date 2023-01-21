# Chess against engine

Play chess locally.

![Example usage](https://github.com/loloof64/ChessAgainstEngine/blob/master/screenshot.png?raw=true)

## Usage

* first we must configure an UCI engine : Stockfish is a good and free candidate
* in the options page, you can also choose the engine thinking time when game is not stopwatched
* for the game, you can start with the standard position, with a custom position, or with a position from a game in a PGN file

* once in the game page, you have nearly full control over it :
  * you can toggle board orientation with up-down arrows icon from the app bar
  * you can choose at any time that the computer play one/both side(s) by clicking on the matching checkboxes
  * you can activate time : each time you activate the time, clock is restarted with the values you gave for the time
  * you can choose that black has a different time than white
  * you can choose to stop current game
  * you can show the engine score evaluation (with a checkbox)
* when game is finished, you also have the option of saving the game in a pgn file, and you can also navigate into the game history

## Caveats

* If your PGN has a tag `Setup` instead of `SetUp`, then it won't be parsed correctly
* When failing to import a PGN, we can't know on which game the error has been reported

## Credits

* Free Serif font download from [Fonts2U](https://fr.fonts2u.com/download/free-serif.police).
* Chess vectors have been downloaded at [Wikimedia Commons](https://commons.wikimedia.org/wiki/Category:SVG_chess_pieces) and designed by CBurnett.
* Icon file has been downloaded from [Freepik](https://www.freepik.com) : https://www.freepik.com/free-vector/chess-game-isometric-concept_6883519.htm.
* Using some of [Google Material icons](https://fonts.google.com/icons) 
* Using some code from the [Accompanist](https://github.com/google/accompanist) project.
* Using code from [a Gist](https://gist.github.com/vganin/a9a84653a9f48a2d669910fbd48e32d5).
* Using code from [Chesspresso project](https://github.com/BernhardSeybold/Chesspresso).
