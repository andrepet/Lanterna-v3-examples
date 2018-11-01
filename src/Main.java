import com.googlecode.lanterna.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ThreadLocalRandom;


/*
 More tutorials here:
 https://github.com/mabe02/lanterna/tree/master/src/test/java/com/googlecode/lanterna/tutorial
 https://code.google.com/archive/p/lanterna/wikis/UsingTerminal.wiki
 https://code.google.com/archive/p/lanterna/wikis/UsingScreen.wiki
*/

public class Main {
    public static void main(String[] args) {
        try {
            testPuttingCharactersOnTerminal(); // LESSON 1
//            testReadingFromTerminal(); // LESSON 2
//            movingInsideTheTerminal(); // LESSON 3
//            testColorsAndGraphics(); // LESSON 4
//            testRandomColors(); // LESSON 5
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            System.out.println("DONE!");
        }
    }

    // LESSON 1
    public static void testPuttingCharactersOnTerminal() throws IOException {
        // Create a "factory object" (it's a design-pattern) that can create a terminal for us
        DefaultTerminalFactory terminalFactory =
                new DefaultTerminalFactory(System.out, System.in, Charset.forName("UTF8"));
        Terminal terminal = terminalFactory.createTerminal(); // most terminal methods can throw IOException

        // Write out a couple of 'X'
        for (int column = 0; column < 5; column++) {
            terminal.setCursorPosition(column, 0); // go to position(column, row)
            terminal.putCharacter('X');
        }

        // Write out a couple of 'O'
        for (int row = 2; row < 6; row++) {
            terminal.setCursorPosition(2, row); // go to position(column, row)
            terminal.putCharacter('O');
        }


    }

    // LESSON 2
    public static void testReadingFromTerminal() throws IOException, InterruptedException {
        // If we are going to use the standard output/input and use UTF8, we don't need to send it in as parameters
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Terminal terminal = terminalFactory.createTerminal();

        // This is one way you can write a message on the terminal:
        String message = "Press a key and see how lanterna reads it. Press ESCAPE or \'q\' to exit";
        for (int i = 0; i < message.length(); i++) {
            terminal.setCursorPosition(i, 0);
            terminal.putCharacter(message.charAt(i));
        }


        boolean continueReadingInput = true;
        while (continueReadingInput) {
            // Let the main-thread read the currently typed character from the terminal
            // If there was no key pressed, the main-thread will sleep for 5 millisecs.
            // After 5 millisecs, it will read from the terminal input again.
            // OBS! If we do not let the main-thread sleep, the CPU will work too hard!
            KeyStroke keyStroke = null;
            do {
                Thread.sleep(5); // might throw InterruptedException
                keyStroke = terminal.pollInput();
            } while (keyStroke == null);

            KeyType type = keyStroke.getKeyType();
            Character c = keyStroke.getCharacter(); // used Character instead of char because it might be null

            System.out.println("keyStroke.getKeyType(): " + type
                    + " keyStroke.getCharacter(): " + c);

            if (type == KeyType.Escape) {
                continueReadingInput = false;
            } else if (type == KeyType.EOF) {
                // if you close the terminal window
                continueReadingInput = false;
            } else if (c == Character.valueOf('q')) {
                // character 'c' might be null, that's why we check it against a boxed-value
                continueReadingInput = false;
            }

            // try to add your own case here!
            // For example, how do you check if the user pressed the 'UpArrow' key?
            // How do you check that the user pressed 's'?
        }

        // close the terminal when we are done
        terminal.close();
    }

    // LESSON 3
    public static void movingInsideTheTerminal() throws IOException, InterruptedException {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Terminal terminal = terminalFactory.createTerminal();

        // hide the terminal cursor
        terminal.setCursorVisible(false);

        // place three markers 'X', 'O' and a 'Z' on the screen at some positions.
        // Illustrates three ways to handle positions in lanterna
        terminal.setCursorPosition(5, 17);
        terminal.putCharacter('X');

        int markColumn = 3;
        int markRow = 1;
        terminal.setCursorPosition(markColumn, markRow);
        terminal.putCharacter('O');

        TerminalPosition terminalPosition = new TerminalPosition(8, 9);
        terminal.setCursorPosition(terminalPosition);
        terminal.putCharacter('Z');


        // You will be a 'block' (unicode 2588), starting at position (5,5)
        int x = 5;
        int y = 5;
        final char c = '\u2588';
        terminal.setCursorPosition(x, y);
        terminal.putCharacter(c);

        // read from keyboard input, same as previous lesson
        boolean continueReadingInput = true;
        while (continueReadingInput) {
            KeyStroke keyStroke = null;
            do {
                Thread.sleep(5);
                keyStroke = terminal.pollInput();
            } while (keyStroke == null);

            // Control the block using the arrow keys and switch-case.
            // When moving, we need to "erase" the previous position of the block.
            // We do it by putting SPACE on the previous position.
            // OBS! There exist the method "terminal.clearScreen()" that clears the screen. What are pros/cons using it?

            // To correctly update the drawing of the terminal we need to call terminal.flush() after the update
            // What happens if you catch one of the markers 'X', 'O' or 'Z'?

            boolean updatePosition = false;
            int oldX = x;
            int oldY = y;
            switch (keyStroke.getKeyType()) {
                case ArrowDown:
                    y += 1;
                    updatePosition = true;
                    break;
                case ArrowUp:
                    y -= 1;
                    updatePosition = true;
                    break;
                case ArrowRight:
                    x += 1;
                    updatePosition = true;
                    break;
                case ArrowLeft:
                    x -= 1;
                    updatePosition = true;
                    break;
            }
            if (updatePosition) {
                terminal.setCursorPosition(oldX, oldY);
                terminal.putCharacter(' ');
                terminal.setCursorPosition(x, y);
                terminal.putCharacter(c);
            }
            terminal.flush(); // don't forget to flush to update the terminal
        }
    }

    // LESSON 4
    public static void testColorsAndGraphics() throws IOException {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Terminal terminal = terminalFactory.createTerminal();

        TerminalPosition startPosition = terminal.getCursorPosition();

        TerminalPosition positionMessageA = startPosition.withRelativeColumn(3).withRelativeRow(2);
        terminal.setCursorPosition(positionMessageA);

        terminal.setBackgroundColor(TextColor.ANSI.BLUE);
        terminal.setForegroundColor(TextColor.ANSI.YELLOW);
        terminal.flush();
        String messageA = "Yellow and blue";
        for (char c : messageA.toCharArray()) {
            terminal.putCharacter(c);
        }
        terminal.flush();

        TerminalPosition positionMessageB = positionMessageA.withRelativeRow(1);
        terminal.setCursorPosition(positionMessageB);

        terminal.enableSGR(SGR.BOLD);
        String messageB = "Bold message";
        for (char c : messageB.toCharArray()) {
            terminal.putCharacter(c);
        }

        terminal.resetColorAndSGR();
        terminal.enableSGR(SGR.BLINK);
        terminal.setCursorPosition(terminal.getCursorPosition().withColumn(0).withRelativeRow(1));
        String messageDone = "Done\n";
        for (char c : messageDone.toCharArray()) {
            terminal.putCharacter(c);
        }
        terminal.flush();
    }



    public static void testRandomColors() throws IOException, InterruptedException {
        // In this lesson, we will use a 30 x 20 board and generate new colors random colors
        // inside it every time the user presses ENTER
        // In this example, the board is index as board[COLUMN][ROW] - you can choose your own way of indexing the board
        // as long as everyone in the group agrees on the standard!

        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Terminal terminal = terminalFactory.createTerminal();

        final int columns = 30;
        final int rows = 20;

        // We can store RGB values inside an object of the class TextColor
        TextColor[][] board = new TextColor[columns][rows];

        String message = "Press \'ENTER\' to start.\nPress \'ENTER\' for a new board.";
        for (char c : message.toCharArray()) {
            terminal.putCharacter(c);
        }

        // read from keyboard input, same as previous lesson
        boolean continueReadingInput = true;
        do {
            KeyStroke keyStroke;
            do {
                Thread.sleep(5);
                keyStroke = terminal.pollInput();
            } while (keyStroke == null);

            if (keyStroke.getKeyType() == KeyType.Escape) {
                continueReadingInput = false;
            }

            // fill the board with random colors
            final int colorRange = 256; // a color channel goes from 0-255;
            for (int col = 0; col < columns; col++) {
                for (int row = 0; row < rows; row++) {
                    int red = ThreadLocalRandom.current().nextInt(colorRange);
                    int green = ThreadLocalRandom.current().nextInt(colorRange);
                    int blue = ThreadLocalRandom.current().nextInt(colorRange);
                    TextColor textColor = new TextColor.RGB(red, green, blue);
                    board[col][row] = textColor;
                }
            }

            // draw the board on the terminal
            for (int col = 0; col < columns; col++) {
                for (int row = 0; row < rows; row++) {
                    TextColor textColor = board[col][row];
                    terminal.setCursorPosition(col, row);
                    terminal.setForegroundColor(textColor);
                    terminal.putCharacter('\u2588');
                }
            }

            terminal.flush(); // don't forget to flush to update the terminal

        } while (continueReadingInput);

        terminal.close();

    }
}



