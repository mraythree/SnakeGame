import jxl.Workbook;
import jxl.write.*;

import javax.swing.*;
import java.awt.*;
import java.awt.Label;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
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
    private static final int populationSize = 50;
    private static final int boarderHeight = 425;
    private static final int boarderWidth = 425;
    private static final int ALL_DOTS = 600;
    private static final int randomPos = 27; //seed value for randoms
    private static final int noStepsTakenTillStallAllowed = 100;

    //pulic static vars
    public static int noInputsNeurons = 15;
    public static int noHiddenNeurons = 7;
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
    private static XYPair midBody;
    private static XYPair tailBody;
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
    public static boolean btnStoppedPushed;

    private static WritableSheet writableSheet;
    private static WritableWorkbook writableWorkbook;
    private static Console console;
    //constructor
    public SnakeGame() throws IOException, WriteException {
        //initialze Excel
        btnStoppedPushed = false;
        String path = "C:\\Users\\Adriaan\\Desktop\\WRCI Results.xls";
        File exlFile = new File(path);
        writableWorkbook = Workbook.createWorkbook(exlFile);
        writableSheet = writableWorkbook.createSheet("Sheet1", 0);
        jxl.write.Label label = new jxl.write.Label(0, 0, "WRCI Results");
        writableSheet.addCell(label);
        //initialize variables
        inGame = true;
        snakeLength = 3;
        headBody = new XYPair(0,0);
        midBody = new XYPair(0,0);
        tailBody = new XYPair(0,0);
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

    //used by the Main class.
    public static SnakeGame getClassInstance()
    {
        if (snakeGame == null)
        {
            try {
                //console lets us see the result from each iteration
                snakeGame = new SnakeGame();
                console = new Console();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (WriteException e) {
                e.printStackTrace();
            }
            return snakeGame;
        }
        return snakeGame;
}

    public void runGame() throws InterruptedException
    {
        //iteration of each game. at the end of each iteration info is passed back and fourth to the GA.
        GeneticTraining ga = new GeneticTraining(populationSize); //takes in the population size
        //set up the NN with the info from the GA
        neuralNetWork NN = new neuralNetWork();
        Chromosone c; //this will be used to keep track of the current chromozone we are on.
        iterationCounter = 0;
        maxSnakeLength = 3; //always starts with length 3.
        while (iterationCounter < 200001 && !btnStoppedPushed)
        {
            c = ga.getCurChromosone();
            NN.setWeightsOfNN(c.getvWeights(), c.getwWeights());

            //restart stall counter. --> these have been depricated. working with number of steps is better.
            restartFoodTimer();
            restartGameTimer();
            snakeLength = 3;
            applesEaten = 0;
            //gives us updates in console.
            if (iterationCounter % 20000 == 0)
                System.out.println("Max length achieved so far: " + maxSnakeLength);

            //this while loop runs the actual game within each iteration.
            while (inGame)
            {
                //every 20 thousand iterations you can see how the snake is doing.
                //if the snake reachers a length greater than 25, we will also see what it is doing.
                //reduce the sleep time, to make the snake move faster.
                if (iterationCounter % 20000 == 0 || snakeLength > 29)
                    TimeUnit.MILLISECONDS.sleep(100);

                //this is also deprecated, as invalid moves should not be made. leave just in case.
                if (invalidMove)
                    inGame = false;

                //we want to know which direction we are going in, so that if the NN tries to go that way, we say "NO BITCH", and take the next best move. the game stalls if the NN tries to go in the opposite direction as this is not allowed, but the inputs don't change, so the NN will continuing trying.
                int currentOppositeDir = 0;
                if (leftDirection)
                    currentOppositeDir = 1;
                else if (rightDirection)
                    currentOppositeDir = 0;
                else if (upDirection)
                    currentOppositeDir = 3;
                else if (downDirection)
                    currentOppositeDir = 2;
                double[] inputs = getInputs(); //get new inputs for the NN for the next move.
                int Move = NN.getNextMove(currentOppositeDir, inputs); //calculate the next move.

                managemove(Move); //find out in which way the NN wants us to go.

                handleMovement(); //actually move the snake.
                checkEatenApple(); //did the snake find food? if so increase its length and reset the stall counter.

                //did the snake just learn how to survive without the need to find food? kill it dead. game stalled.
                //this value might need to be changed to something dynamic, as when the size increases, it might need more steps.
                if (stepsTakenSinceLastFood > noStepsTakenTillStallAllowed)
                {
                    inGame = false;
                    c.stalled = true;
                }
                checkForCollision(); //did the snake collide with anything? wall or body? if so, kill it.
                if (iterationCounter % 20000 == 0 || snakeLength > 29) //we don't show what is going on the screen in "simulation mode", takes too long.
                    repaint(); //refresh screen.
            }
            //set the chromosone's fitness. //use of time has been depricated.
            c.setFitness(getElapsedGameTime(), snakeLength);
            //use the console to update what happened in the current iteration.
            //console.addToConsole("Iteation: " + iterationCounter + "  Length: " + snakeLength);
            System.out.println("Iteation: " + iterationCounter + "  Length: " + snakeLength);
            if (snakeLength > maxSnakeLength)
            {
                maxSnakeLength = snakeLength;
                console.addToConsole("New Highscoore of: " + maxSnakeLength + ", in iteration: " + iterationCounter);
            }



            iterationCounter++;
            //if we have been through a population of chromosone's its time to do crossovers and mutations, etc.
            if (iterationCounter % ga.populationSize == 0)
                ga.Train();
            //move onto the next chromosone. this wraps around to the beginning once the population size is reached.
            ga.incCurChromosone();
            //initialize the next iteration
            initializeGame();
        }
        console.addToConsole("Game Stopped!");

        //write to excel
        writeExcelResults(ga);
        try {
            writableWorkbook.write();
            writableWorkbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    private void writeExcelResults(GeneticTraining ga)
    {
        jxl.write.Label label;
        try {
            // LABEL: (COLOUMN, ROW, STRING);
            //NN Details
            label = new jxl.write.Label(0, 2, "NN Details");
            writableSheet.addCell(label);
            label = new jxl.write.Label(0, 3, "Input Neurons");
            writableSheet.addCell(label);
            label = new jxl.write.Label(0, 4, "Hidden Neurons");
            writableSheet.addCell(label);
            label = new jxl.write.Label(0, 5, "Output Neurons");
            writableSheet.addCell(label);
            label = new jxl.write.Label(1, 3, String.valueOf(noInputsNeurons));
            writableSheet.addCell(label);
            label = new jxl.write.Label(1, 4, String.valueOf(noHiddenNeurons));
            writableSheet.addCell(label);
            label = new jxl.write.Label(1, 5, String.valueOf(noOutputNeurons));
            writableSheet.addCell(label);

            //Game Details
            label = new jxl.write.Label(3, 2, "Game Details");
            writableSheet.addCell(label);
            label = new jxl.write.Label(3, 3, "No Steps taken until stall");
            writableSheet.addCell(label);
            label = new jxl.write.Label(3, 2, "No Iterations over all chromosomes");
            writableSheet.addCell(label);
            label = new jxl.write.Label(3, 2, "Max Snake Length Achieved");
            writableSheet.addCell(label);
            label = new jxl.write.Label(4, 3, String.valueOf(noStepsTakenTillStallAllowed));
            writableSheet.addCell(label);
            label = new jxl.write.Label(4, 2, String.valueOf(iterationCounter));
            writableSheet.addCell(label);
            label = new jxl.write.Label(4, 2, String.valueOf(maxSnakeLength));
            writableSheet.addCell(label);

            //GA Details
            label = new jxl.write.Label(6, 2, "GA Details");
            writableSheet.addCell(label);
            label = new jxl.write.Label(6, 3, "Population Size");
            writableSheet.addCell(label);
            label = new jxl.write.Label(6, 4, "Mutation Rate");
            writableSheet.addCell(label);
            label = new jxl.write.Label(6, 5, "Hall Of Fame Size");
            writableSheet.addCell(label);
            label = new jxl.write.Label(6, 6, "Tournament Size");
            writableSheet.addCell(label);
            label = new jxl.write.Label(6, 7, "Min Search Space");
            writableSheet.addCell(label);
            label = new jxl.write.Label(6, 8, "Max Search Space");
            writableSheet.addCell(label);
            label = new jxl.write.Label(7, 3, String.valueOf(populationSize));
            writableSheet.addCell(label);
            label = new jxl.write.Label(7, 4, String.valueOf(ga.mutationRate));
            writableSheet.addCell(label);
            label = new jxl.write.Label(7, 5, String.valueOf(ga.hallOfFameSize));
            writableSheet.addCell(label);
            label = new jxl.write.Label(7, 6, String.valueOf(ga.tournamentSize));
            writableSheet.addCell(label);
            label = new jxl.write.Label(7, 7, String.valueOf(minSearchSpace));
            writableSheet.addCell(label);
            label = new jxl.write.Label(7, 8, String.valueOf(maxSearchSpace));
            writableSheet.addCell(label);
        }
        catch (WriteException e)
        {
            e.printStackTrace();
        }
    }

    private void managemove(int Move)
    {
        //check for invalid moves. this should never happen now.
        if ((Move == 0) && (rightDirection)) //move left
        {
            invalidMove = true;
        }
        else if ((Move == 1) && (leftDirection)) //move right
        {
            invalidMove = true;
        }
        else if ((Move == 2) && (downDirection)) //move down
        {
            invalidMove = true;
        }
        else if ((Move == 3) && (upDirection)) //move up
        {
            invalidMove = true;
        }

       //set the new direction
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

    //start with a length of 3. place the food on the board. food will never be placed somewhere on the snake's body.
    private static void initializeGame()
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
        //always start in the centre of the board.
        int headpos = (13* 15) + 5;
        //int headpos = randomInt0and25();
        if (dir == 1) //going right
        {
            rightDirection = true;
            x[0] = headpos;
            y[0] = headpos;
            x[1] = headpos - 15;
            y[1] = headpos;
            x[2] = headpos - 30;
            y[2] = headpos;
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
        }
        else if (dir == 4) //going down
        {
            downDirection = true;
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

    //used to find a random pos on the board.
    public static int randomInt0and25()
    {
//        Random r = new Random();
//        int min = 3;
//        int max = 21;
//        int randomVal = r.nextInt((max - min)) + min;
        int randomVal = 13;
        return (randomVal * 15) + 5;
    }

    //use for a random starting direction.
    public static int randomInt0and3()
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
//        double inputs[] = new double[11];
//        inputs[0]  = headBody.getX();
//        inputs[1]  = headBody.getY();
//        inputs[2]  = foodX;
//        inputs[3]  = foodY;
//        double distToFoodX = Math.abs(headBody.getX() - foodX);
//        inputs[4]  = distToFoodX;
//        double distToFoodY = Math.abs(headBody.getY() - foodY);
//        inputs[5]  = distToFoodY;
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
//        inputs[6] = L;
//        inputs[7] = R;
//        inputs[8] = U;
//        inputs[9] = D;
//        inputs[10] = -1.0;
//
//        normalizeInputs(inputs);
//
//        return inputs;
//    }
    public double[] getInputs()
    {
        double inputs[] = new double[15];
        inputs[0]  = headBody.getX();
        inputs[1]  = headBody.getY();
        inputs[2]  = midBody.getX();
        inputs[3]  = midBody.getY();
        inputs[4]  = tailBody.getX();
        inputs[5]  = tailBody.getY();
        inputs[6]  = foodX;
        inputs[7]  = foodY;
        double distToFoodX = Math.abs(headBody.getX() - foodX);
        inputs[8]  = distToFoodX;
        double distToFoodY = Math.abs(headBody.getY() - foodY);
        inputs[9]  = distToFoodY;
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
        inputs[10] = L;
        inputs[11] = R;
        inputs[12] = U;
        inputs[13] = D;
        inputs[14] = -1.0;

        normalizeInputs(inputs);

        return inputs;
    }

    //we are using a sigmoid activation function. all inputs must be normalized.
    //x and y are normalized to their own x and y mins and maxes. this ensure that their distances don't affect each other.
    private void normalizeInputs(double[] inputs)
    {
        maxX = -1;
        maxY = -1;
        minX = 999999;
        minY = 999999;
        for (int i = 0; i <= 9; i++)
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
        for (int i = 0; i <= 9; i++)
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

    //take the snake's current pos for everything that we need for the NN inputs
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
        Double halfPos = snakeLength / 2.0;
        String temp = String.valueOf(halfPos);
        if (temp.contains(".5"))
            halfPos = halfPos + 0.5;
        int hp = Integer.valueOf(halfPos.intValue()) - 1; //indexes start at a different places
        int tempX = x[hp];
        int tempY = y[hp];
        midBody.setX(x[hp]);
        midBody.setY(y[hp]);
        tailBody.setX(x[snakeLength]);
        tailBody.setY(y[snakeLength]);

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
        tempX = x[0];
        tempY = y[0];
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

    //this is not used for anything at the moment.
    private void gameOver(Graphics g)
    {

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

    //random location for the apple, that is not on the snake's body
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

    //this is not used anymore as snake controls itself. if you want to be an asshole you can interrupt the snake's game. but maybe let him, you know? do his thang.
    private class TAdapter extends KeyAdapter
    {
        //what to do when a key is pressed.
        //will we eaten an apple?
        //will we hit the body or wall?
        //okay, cool, time to move.
        //repaint -> update the board.
        public void keyPressed() {

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
