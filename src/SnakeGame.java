import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by Adriaan on 2016/09/20
 */
public class SnakeGame extends JPanel
{
    //constant variables
    private static final int DOT_SIZE = 15;
    private static final int boarderHeight = 425;
    private static final int boarderWidth = 425;
    private static final int ALL_DOTS = 600;
    private static final int randomPos = 27; //seed value for randoms

    //pulic static vars
    public static int noInputsNeurons = 11;
    public static int noHiddenNeurons = 5;
    public static int noOutputNeurons = 4;
    public static int minSearchSpace = -10000;
    public static int maxSearchSpace = 10000;

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
    private static int snakeLength;
    private static int maxSnakeLength;
    private static int iterationCounter;
    private static int applesEaten;
    public static int stepsTaken;
    public static int stepsTakenSinceLastFood;

    //mins and maxes for normalization
    private static double maxX;
    private static double maxY;
    private static double minY;
    private static double minX;


    //images to use
    private static Image imgBody;
    private static Image imgApple;
    private static Image imgHead;

    //positions of things
    private static XYPair headBody;
//    private static XYPair midBody;
//    private static XYPair tailBody;
    private static XYPair foodXY;
    private static boolean leftDirFree;
    private static boolean rightDirFree;
    private static boolean upDirFree;
    private static boolean downDirFree;

    //timer
    private static long timeSinceLastFood;
    private static long elapsedTimeSinceLastFood;
    private static long gameTime;
    private static long elapsedGameTime;
    private static boolean invalidMove;

    public static SnakeGame snakeGame;

//    private Timer timer;
//    private final int DELAY = 140;

    //constructor
    public SnakeGame()
    {
        //initialize variables
        inGame = true;
        snakeLength = 3;
        headBody = new XYPair(0,0);
//        midBody = new XYPair(0,0);
//        tailBody = new XYPair(0,0);
        foodXY = new XYPair(0,0);

        //put the key listener in
        addKeyListener(new TAdapter());

        setBackground(Color.black);
        setFocusable(true);
        setPreferredSize(new Dimension(boarderWidth, boarderHeight));
        iterationCounter = 0;
        applesEaten = 0;
        stepsTaken = 0;
        stepsTakenSinceLastFood = 0;
        invalidMove = false;
        initializeImages();
        initializeGame();
    }

    public static SnakeGame getClassInstance()
    {
        if (snakeGame == null)
        {
            snakeGame = new SnakeGame();
            return snakeGame;
        }
        return snakeGame;
}

    public void runGame() throws InterruptedException
    {
        //console lets us see the result from each iteration
       // Console console = new Console();
        //the first while loop runs on the iteration of each game. at the end of each iteration info is passed back and fourth to the GA.
        GeneticTraining ga = new GeneticTraining(50); //takes in the population size
        //set up the NN with the info from the GA
        neuralNetWork NN = new neuralNetWork();
        Chromosone c;
        iterationCounter = 0;
        maxSnakeLength = 3;
        while (iterationCounter < 100000)
        {
            c = ga.getCurChromosone();
            NN.setWeightsOfNN(c.getvWeights(), c.getwWeights());

            //restart stall counter.
            restartFoodTimer();
            restartGameTimer();
            snakeLength = 3;
            applesEaten = 0;
            if (iterationCounter % 20000 == 0)
                System.out.println("Max length achieved so far: " + maxSnakeLength);

            //this while loop runs the actual game within each iteration.
            while (inGame)
            {
                if (iterationCounter % 20000 == 0 || snakeLength > 25)
                    TimeUnit.MILLISECONDS.sleep(100); //so we can see what the snake is doing. reduce this to make the snake go faster.


//                if (getElapsedTimeSinceLastFood() > 100)
//                    inGame = false; //go to the next game iteration if the game stalls.
//                if (getElapsedTimeSinceLastFood() > 1000 && (iterationCounter % 1000 != 0))
//                    inGame = false; //go to the next game iteration if the game stalls.
//                else if (getElapsedTimeSinceLastFood() > 10000 && iterationCounter % 1000 == 0)
//                    inGame = false;
                if (invalidMove)
                    inGame = false;

                double[] inputs = getInputs(); //get new inputs for the NN for the next move.
                int curDir = 0;
                if (leftDirection)
                    curDir = 1;
                else if (rightDirection)
                    curDir = 0;
                else if (upDirection)
                    curDir = 3;
                else if (downDirection)
                    curDir = 2;
                int Move = NN.getNextMove(curDir, inputs); //calculate the next move.

                managemove(Move); //change the direction

                handleMovement(); //actually move the snake.
                checkEatenApple(); //did the snake find food? if so increase its length and reset the stall counter.
                if (stepsTakenSinceLastFood > 130)
                {
                    inGame = false;
                    c.stalled = true;
                }
                checkForCollision(); //did the snake collide with anything? wall or body.
                if (iterationCounter % 20000 == 0 || snakeLength > 25)
                    repaint(); //refresh screen.
            }
            c.setFitness(getElapsedGameTime(), snakeLength);
            //use the console to update what happened in the current iteration.
            //console.addToConsole("Iteation: " + iterationCounter + "  Length: " + snakeLength);
            System.out.println("Iteation: " + iterationCounter + "  Length: " + snakeLength);
            if (snakeLength > maxSnakeLength)
                maxSnakeLength = snakeLength;
            iterationCounter++;
            //if ((iterationCounter % ga.populationSize == 0) && (iterationCounter != 0))
            if (iterationCounter % ga.populationSize == 0)
                ga.Train();
            //move onto the next chromosone. this wraps around to the beginning once the population size is reached.
            ga.incCurChromosone();
            //initialize the next iteration
            initializeGame();
        }

    }

    private void managemove(int Move)
    {

        if ((Move == 0) && (rightDirection)) //move left
        {
            invalidMove = true;
        }
        else if ((Move == 1) && (leftDirection)) //move left
        {
            invalidMove = true;
        }
        else if ((Move == 2) && (downDirection)) //move left
        {
            invalidMove = true;
        }
        else if ((Move == 3) && (upDirection)) //move left
        {
            invalidMove = true;
        }

        // this is tricky
        //if the NN decides the snake should go left when it was moving in a right direction (illegal move) I can't just stop it from making that move, because on the next iteration, the NN will get the same inputs and then make the same decision as the last illegal move.
        if ((Move == 0) && (!rightDirection)) //move left
        {
            leftDirection = true;
            upDirection = false;
            downDirection = false;
        }
        else if ((Move == 1) && (!leftDirection)) //move right
        {
            rightDirection = true;
            upDirection = false;
            downDirection = false;
        }
        else if ((Move == 2) && (!downDirection)) //move up
        {
            upDirection = true;
            rightDirection = false;
            leftDirection = false;
        }
        else if ((Move == 3) && (!upDirection))//move down
        {
            downDirection = true;
            rightDirection = false;
            leftDirection = false;
        }
    }

    //start with a length of 3. place the food on the board.
    // -> we don't have a check if the food is on the body at this point -> we need to ensure that the food does not intercept the body on initialization.
    private void initializeGame()
    {
        inGame = true;
        dots = 3;
//        rightDirection = true;
        leftDirection = false;
        rightDirection = false;
        upDirection = false;
        downDirection = false;
        stepsTaken = 0;
        stepsTakenSinceLastFood = 0;
        invalidMove = false;
        //place the width and height of the pixels for each "dot"/length of body. the paint method will take care of the rest.
//        for (int i = 0; i <= dots -1; i++)
//        {
//            x[i] = 50 - i * 15;
//            y[i] = 50;
//        }

        //snake needs to start in a random pos
        //pick direction
        int dir = randomInt0and3() + 1;
        int headpos = randomInt0and25();
        if (dir == 1) //going right
        {
            rightDirection = true;
            x[0] = headpos;
            y[0] = headpos;
            x[1] = headpos - 15;
            y[1] = headpos;
            x[2] = headpos - 30;
            y[2] = headpos;
//            for (int i = 0; i <= dots -1; i++)
//            {
//                x[i] = headpos - i * 15;
//                y[i] = headpos;
//            }
        }
        else if (dir == 2) //going left
        {
            leftDirection = true;
            x[0] = headpos;
            y[0] = headpos;
            x[1] = headpos + 15;
            y[1] = headpos;
            x[2] = headpos + 30;
            y[2] = headpos;
//            for (int i = 0; i <= dots -1; i++)
//            {
//                x[i] = headpos + i * 15;
//                y[i] = headpos;
//            }
        }
        else if (dir == 3) //going up
        {
            upDirection = true;
            x[0] = headpos;
            y[0] = headpos;
            x[1] = headpos;
            y[1] = headpos + 15;
            x[2] = headpos;
            y[2] = headpos + 30;
//            for (int i = 0; i <= dots -1; i++)
//            {
//                x[i] = headpos;
//                y[i] = headpos + i * 15;
//            }
        }
        else if (dir == 4) //going down
        {
            downDirection = true;
//            for (int i = 0; i <= dots -1; i++)
//            {
//                x[i] = headpos;
//                y[i] = headpos - i * 15;
//            }
            x[0] = headpos;
            y[0] = headpos;
            x[1] = headpos;
            y[1] = headpos - 15;
            x[2] = headpos;
            y[2] = headpos - 30;
        }
        placeApple();
        updatePostitions();
    }

    public int randomInt0and25()
    {
        Random r = new Random();
        int min = 3;
        int max = 21;
        int randomVal = r.nextInt((max - min)) + min;
        return (randomVal * 15) + 5;
    }

    public int randomInt0and3()
    {
        Random r = new Random();
        int min = 0;
        int max = 4;
        int randomVal = r.nextInt((max - min)) + min;
        return randomVal;
    }

    //pop up message method - currently not being used.
    public static void infoBox(String infoMessage)
    {
        JOptionPane.showMessageDialog(null, infoMessage, "Snake", JOptionPane.INFORMATION_MESSAGE);
    }

    //resets the timer if the snake finds food.
    private static void restartFoodTimer()
    {
        timeSinceLastFood = System.currentTimeMillis();
    }

    //used to check if the snake is stalling.
    private static long getElapsedTimeSinceLastFood()
    {
        elapsedTimeSinceLastFood = (new Date()).getTime() - timeSinceLastFood;
        return elapsedTimeSinceLastFood;
    }

    private static void restartGameTimer()
    {
        gameTime = System.currentTimeMillis();
    }

    private static long getElapsedGameTime()
    {
        elapsedGameTime = (new Date()).getTime() - gameTime;
        return elapsedGameTime;
    }

    //this is what gets passed to the NN for it to make a decision
//    public double[] getInputs()
//    {
//        double inputs[] = new double[15];
//        inputs[0]  = headBody.getX();
//        inputs[1]  = headBody.getY();
//        inputs[2]  = midBody.getX();
//        inputs[3]  = midBody.getY();
//        inputs[4]  = tailBody.getX();
//        inputs[5]  = tailBody.getY();
//        inputs[6]  = foodX;
//        inputs[7]  = foodY;
//        double distToFoodX = Math.abs(headBody.getX() - foodX);
//        inputs[8]  = distToFoodX;
//        double distToFoodY = Math.abs(headBody.getY() - foodY);
//        inputs[9]  = distToFoodY;
//        double L = 0.0;
//        double R = 0.0;
//        double U = 0.0;
//        double D = 0.0;
//        if (leftDirFree)
//            L = 1.0;
//        if (rightDirFree)
//            R = 1.0;
//        if (upDirFree)
//            U = 1.0;
//        if (downDirFree)
//            D = 1.0;
//        inputs[10] = L;
//        inputs[11] = R;
//        inputs[12] = U;
//        inputs[13] = D;
//        inputs[14] = -1.0;
//
//        normalizeInputs(inputs);
//
//        return inputs;
//    }
    public double[] getInputs()
    {
        double inputs[] = new double[11];
        inputs[0]  = headBody.getX();
        inputs[1]  = headBody.getY();
        inputs[2]  = foodX;
        inputs[3]  = foodY;
        double distToFoodX = Math.abs(headBody.getX() - foodX);
        inputs[4]  = distToFoodX;
        double distToFoodY = Math.abs(headBody.getY() - foodY);
        inputs[5]  = distToFoodY;
        double L = 0.0;
        double R = 0.0;
        double U = 0.0;
        double D = 0.0;
        if (leftDirFree)
            L = 1.0;
        if (rightDirFree)
            R = 1.0;
        if (upDirFree)
            U = 1.0;
        if (downDirFree)
            D = 1.0;
        inputs[6] = L;
        inputs[7] = R;
        inputs[8] = U;
        inputs[9] = D;
        inputs[10] = -1.0;

        normalizeInputs(inputs);

        return inputs;
    }

    private void normalizeInputs(double[] inputs)
    {
        maxX = -1;
        maxY = -1;
        minX = 999999;
        minY = 999999;
        for (int i = 0; i <= 5; i++)
        {
            if (i % 2 == 0)
            {
                if (inputs[i] > maxX)
                    maxX = inputs[i];
                if (inputs[i] < minX)
                    minX = inputs[i];
            }
            else
            {
                if (inputs[i] > maxY)
                    maxY = inputs[i];
                if (inputs[i] < minY)
                    minY = inputs[i];
            }
        }
        for (int i = 0; i <= 5; i++)
        {
            double numerator;
            double demoninator;
            double normalized;
            if (i % 2 == 0)
            {
                numerator = inputs[i] - minX;
                demoninator = maxX - minX;
                normalized = numerator / demoninator;
                inputs[i] = normalized;
            }
            else
            {
                numerator = inputs[i] - minY;
                demoninator = maxY - minY;
                normalized = numerator / demoninator;
                inputs[i] = normalized;
            }
        }
    }

    //for the NN inputs
    private static void updatePostitions() {
        //get the length of the snake
        int sizeXandY = 0;

        for (int i : x)
            if (i != 0)
                sizeXandY++;
            else
                break;
        //set snake body info
        headBody.setX(x[0]);
        headBody.setY(y[0]);
        Double halfPos = Math.ceil(sizeXandY / 2);
//        midBody.setX(x[halfPos.intValue()]);
//        midBody.setY(y[halfPos.intValue()]);
//        tailBody.setX(x[sizeXandY - 1]);
//        tailBody.setY(y[sizeXandY - 1]);

        foodXY.setX(foodX);
        foodXY.setY(foodY);

        //available spaces the snake can more. true means the space is free, and the head can move there.
        leftDirFree = true;
        rightDirFree = true;
        upDirFree = true;
        downDirFree = true;
        //check the direction the snake is moving and rule out the opposite direction
        if (leftDirection)
            rightDirFree = false;
        else if (rightDirection)
            leftDirFree = false;
        else if (upDirection)
            downDirFree = false;
        else if (downDirection)
            upDirFree = false;
        //rule out boundary movements
        if (x[0] == 5)
            leftDirFree = false;
        else if (x[0] == 410)
            rightDirFree = false;
        if (y[0] == 5)
            upDirFree = false;
        else if (y[0] == 410)
            downDirFree = false;
        //rule out moving into the body.
        //check to the left
        int tempX = x[0];
        int tempY = y[0];
        if (leftDirFree) {
            int tempXX = tempX - DOT_SIZE;
            for (int i = dots; i > 0; i--)
                if ((i > 0) && (tempXX == x[i]) && (tempY == y[i])) {
                    leftDirFree = false;
                    break;
                }
        }
        if (rightDirFree) {
            int tempXX = tempX + DOT_SIZE;
            for (int i = dots; i > 0; i--)
                if ((i > 0) && (tempXX == x[i]) && (tempY == y[i])) {
                    rightDirFree = false;
                    break;
                }
        }
        if (upDirFree) {
            int tempYY = tempY - DOT_SIZE;
            for (int i = dots; i > 0; i--)
                if ((i > 0) && (tempX == x[i]) && (tempYY == y[i])) {
                    upDirFree = false;
                    break;
                }
        }
        if (downDirFree) {
            int tempYY = tempY + DOT_SIZE;
            for (int i = dots; i > 0; i--)
                if ((i > 0) && (tempX == x[i]) && (tempYY == y[i]))
                {
                    downDirFree = false;
                    break;
                }
        }
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

        stepsTaken++;
        stepsTakenSinceLastFood++;
        updatePostitions();

    }

    //we only need to check where the head is, as the body can't go where the head hasn't been
    private static void checkForCollision() {

        for (int i = dots; i > 0; i--)
            if ((i > 3) && (x[0] == x[i]) && (y[0] == y[i]))
                inGame = false;

        if (y[0] >= boarderHeight)
            inGame = false;


        if (y[0] < 0)
            inGame = false;


        if (x[0] >= boarderWidth)
            inGame = false;


        if (x[0] < 0)
            inGame = false;
    }

    //what to do when the game is over --> need to pass results back to NN -> probably via an interface.
    private void gameOver(Graphics g)
    {
        //send info to GA.
        ArrayList<Integer> GAInfo = new ArrayList<>();
        //GAInfo.add()
    }

    //have we eaten an apple? if so, increase length and place a new apple.
    //again, need to have check that the apple does not spawn on the body.
    private static void checkEatenApple()
    {
        if ((x[0] == foodX) && (y[0] == foodY))
        {
            dots++;
            snakeLength++;
            applesEaten++;
            placeApple();
            restartFoodTimer();
            stepsTakenSinceLastFood = 0;
        }
    }

    //random location for the apple.
    private static void placeApple()
    {
        int R = (int) (Math.random() * randomPos);
        foodX = (R * DOT_SIZE + 5);
        R = (int) (Math.random() * randomPos);
        foodY = (R * DOT_SIZE + 5);
        while (checkIfFoodPlacedOnBody())
        {
            R = (int) (Math.random() * randomPos);
            foodX = (R * DOT_SIZE + 5);
            R = (int) (Math.random() * randomPos);
            foodY = (R * DOT_SIZE + 5);
        }
    }

    private static boolean checkIfFoodPlacedOnBody()
    {
        for (int i = dots; i > 0; i--)
        {
            if (foodX == x[i] && foodY == y[i])
                return true;
        }
        return false;
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

    //this is not used anymore as snake controls itself.
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
                handleMovement();
                checkForCollision();
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
