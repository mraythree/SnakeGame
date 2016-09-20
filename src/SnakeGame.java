import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created by Adriaan on 2016/09/20.
 */
public class SnakeGame extends JPanel
{
    //constant variables
    private static final int DOT_SIZE = 10;
    private static final int boarderHeight = 400;
    private static final int boarderWidth = 400;
    private static final int ALL_DOTS = 900;
    private static final int randomPos = 29; //seed value for randoms

    //size of board
    private static final int x[] = new int[ALL_DOTS];
    private static final int y[] = new int[ALL_DOTS];

    //global variables
    private static boolean leftDirection;
    private static boolean rightDirection;
    private static boolean upDirection;
    private static boolean downDirection;
    private static boolean inGame;

    private static int dots;
    private static int foodX;
    private static int foodY;

    //images to use
    private static Image imgBody;
    private static Image imgApple;
    private static Image imgHead;

//    private Timer timer;
//    private final int DELAY = 140;

    //constructor
    public SnakeGame()
    {
        //initialize variables
        leftDirection = false;
        rightDirection = false;
        upDirection = false;
        downDirection = false;
        inGame = true;

        //put the key listener in
        addKeyListener(new TAdapter());

        setBackground(Color.black);
        setFocusable(true);
        setPreferredSize(new Dimension(boarderWidth, boarderHeight));

        initializeImages();
        initializeGame();
    }

    //start with a length of 3. place the food on the board.
    // -> we don't have a check if the food is on the body at this point -> we need to ensure that the food does not intercept the body on initialization.
    private void initializeGame()
    {
        dots = 3;
        //place the width and height of the pixels for each "dot"/length of body. the paint method will take care of the rest.
        for (int i = 0; i <= dots -1; i++)
        {
            x[i] = 50 - i * 10;
            y[i] = 50;
        }
        
        placeApple();

//        timer = new Timer(DELAY, this);
//        timer.start();
    }

    //drawing method that handles the changing and updting of pixels
    //called by the "repaint()" method.
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if (inGame) {

            g.drawImage(imgApple, foodX, foodY, this);

            for (int z = 0; z < dots; z++) {
                if (z == 0) {
                    g.drawImage(imgHead, x[z], y[z], this);
                } else {
                    g.drawImage(imgBody, x[z], y[z], this);
                }
            }
            Toolkit.getDefaultToolkit().sync();
        }
        else
        {
            gameOver(g);
        }
    }

    //handle the moving of the snake.
    private static void handleMovement()
    {
        //shift the tail up.
        for (int i = dots; i > 0; i--)
        {
            x[i] = x[(i - 1)];
            y[i] = y[(i - 1)];
        }

        //move the head
        if (leftDirection)
            x[0] -= DOT_SIZE;


        if (rightDirection)
            x[0] += DOT_SIZE;


        if (upDirection)
            y[0] -= DOT_SIZE;


        if (downDirection)
            y[0] += DOT_SIZE;

    }

    //we only need to check where the head is, as the body can't go where the head hasn't been
    private static void checkForCollision() {

        for (int i = dots; i > 0; i--)
            if ((i > 4) && (x[0] == x[i]) && (y[0] == y[i]))
                inGame = false;

        if (y[0] >= boarderHeight)
            inGame = false;


        if (y[0] < 0)
            inGame = false;


        if (x[0] >= boarderWidth)
            inGame = false;


        if (x[0] < 0)
            inGame = false;

//        if(!inGame)
//            timer.stop();


    }

    //what to do when the game is over --> need to pass results back to NN -> probably via an interface.
    private void gameOver(Graphics g)
    {

    }

    //have we eaten an apple? if so, increase length and place a new apple.
    //again, need to have check that the apple does not spawn on the body.
    private static void checkEatenApple() {

        if ((x[0] == foodX) && (y[0] == foodY))
        {
            dots++;
            placeApple();
        }
    }

    //random location for the apple.
    private static void placeApple()
    {
        int R = (int) (Math.random() * randomPos);
        foodX = (R*DOT_SIZE);
        R = (int) (Math.random() * randomPos);
        foodY = (R*DOT_SIZE);
    }

    //load the dots that will be drawn on the paint method.
    private static void initializeImages()
    {
        ImageIcon tempIcon = new ImageIcon("body.png");
        imgBody = tempIcon.getImage();
        tempIcon = new ImageIcon("head.png");
        imgHead = tempIcon.getImage();
        tempIcon = new ImageIcon("apple.png");
        imgApple = tempIcon.getImage();
    }

    private class TAdapter extends KeyAdapter
    {
        //what to do when a key is pressed.
        //will we eaten an apple?
        //will we hit the body or wall?
        //okay, cool, time to move.
        //repaint -> update the board.
        public void keyPressed()
        {
            if (inGame)
            {
                checkEatenApple();
                checkForCollision();
                handleMovement();
            }
            repaint();
        }

        @Override
        public void keyPressed(KeyEvent e)
        {
            super.keyPressed(e);

            int key = e.getKeyCode();

            if ((key == KeyEvent.VK_LEFT) && (!rightDirection)) {
                leftDirection = true;
                upDirection = false;
                downDirection = false;
                keyPressed();
            }

            if ((key == KeyEvent.VK_RIGHT) && (!leftDirection)) {
                rightDirection = true;
                upDirection = false;
                downDirection = false;
                keyPressed();
            }

            if ((key == KeyEvent.VK_UP) && (!downDirection)) {
                upDirection = true;
                rightDirection = false;
                leftDirection = false;
                keyPressed();
            }

            if ((key == KeyEvent.VK_DOWN) && (!upDirection)) {
                downDirection = true;
                rightDirection = false;
                leftDirection = false;
                keyPressed();
            }
        }
    }
}
