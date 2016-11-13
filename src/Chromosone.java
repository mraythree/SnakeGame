import java.util.Random;

/**
 * Created by Adriaan on 2016/11/12.
 */
public class Chromosone
{
    double[][] vWeights, wWeights;
    double fitness;
    double minSearchSpace, maxSearchSpace;
    long timeTaken;
    double snakeLength;
    int stepsTaken;
    boolean stalled;

    public Chromosone()
    {
        this.minSearchSpace = SnakeGame.minSearchSpace;
        this.maxSearchSpace = SnakeGame.maxSearchSpace;
        stalled = false;
        //initializeWeights();
    }

    public Chromosone(boolean firstRun)
    {
        this.minSearchSpace = SnakeGame.minSearchSpace;
        this.maxSearchSpace = SnakeGame.maxSearchSpace;
        if (firstRun)
            initializeWeights();
        stalled = false;
    }

    public void initializeWeights()
    {
        //give initial weights
        vWeights = new double[SnakeGame.noHiddenNeurons][SnakeGame.noInputsNeurons]; //2D array for weights going from every input node, to every hidden node.
        wWeights = new double[SnakeGame.noOutputNeurons][SnakeGame.noHiddenNeurons]; //2D array for weights going from every hidden node to every output node
        for (int i = 0; i<= SnakeGame.noInputsNeurons -1; i++) //set the last one to be the bias
            for (int j = 0; j <= SnakeGame.noHiddenNeurons-1; j++)
                vWeights[j][i] = randomDouble();

        for (int j = 0; j <= SnakeGame.noHiddenNeurons-1; j++)
        {
            for (int k = 0; k<= SnakeGame.noOutputNeurons -1; k++)
                wWeights[k][j] = randomDouble();
        }
    }

    //so we are using the time taken and the snake's length as the FF.
    //this means that we are encouraging the snake to stay alive, but are rewarding it more for finding the food.
    public void setFitness(long timeTaken, double snakeLength)
    {
        //this.timeTaken = timeTaken;
        this.snakeLength = snakeLength;
        this.stepsTaken = SnakeGame.stepsTaken;
        double out;
        if (stalled)
            out = (stepsTaken/4) + (snakeLength * 1000000.0);
        else
            out = (stepsTaken/2) + (snakeLength * 1000000.0);

        //double out = timeTaken + (snakeLength * 10000000);
        //double out = snakeLength * 1.0;
        stalled = false;
        fitness = out;
    }

    public Chromosone clone()
    {
        Chromosone out = new Chromosone();
        double[][] newVWeights = new double[SnakeGame.noHiddenNeurons][SnakeGame.noInputsNeurons];
        double[][] newWWeights = new double[SnakeGame.noOutputNeurons][SnakeGame.noHiddenNeurons];

        for (int i = 0; i < SnakeGame.noHiddenNeurons; i++)
            newVWeights[i] = getvWeights()[i].clone();
        out.setvWeights(newVWeights);

        for (int i = 0; i < SnakeGame.noOutputNeurons; i++)
            newWWeights[i] = getwWeights()[i].clone();
        out.setwWeights(newWWeights);
        out.setFitness(timeTaken, fitness);
        return out;
    }

    public double getFitness()
    {
        return fitness;
    }

    public double randomDouble()
    {
        Random r = new Random();
        double min = -1 / Math.sqrt(SnakeGame.noInputsNeurons);
        double max = 1/ Math.sqrt(SnakeGame.noInputsNeurons);
        double randomVal = (r.nextDouble() * (max - min)) + min;
        return randomVal;
    }

    public double randomDoubleInSearchSpace()
    {
        Random r = new Random();
        double min = minSearchSpace;
        double max = maxSearchSpace;
        double randomVal = r.nextDouble()*(max - min) + min;
        return randomVal;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(long timeTaken) {
        this.timeTaken = timeTaken;
    }

    public double[][] getvWeights() {
        return vWeights;
    }

    public void setvWeights(double[][] vWeights) {
        this.vWeights = vWeights;
    }

    public double[][] getwWeights() {
        return wWeights;
    }

    public void setwWeights(double[][] wWeights) {
        this.wWeights = wWeights;
    }
}
