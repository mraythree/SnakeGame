import java.util.Random;

/**
 * Created by Adriaan on 2016/11/12.
 */
public class Chromosone
{
    double[][] vWeights, wWeights;
    double fitness;
    double minSearchSpace, maxSearchSpace;

    public Chromosone()
    {
        this.minSearchSpace = SnakeGame.minSearchSpace;
        this.maxSearchSpace = SnakeGame.maxSearchSpace;
        initializeWeights();
    }

    public void initializeWeights()
    {
        //give initial weights
        for (int i = 0; i<= SnakeGame.noInputsNeurons -1; i++) //set the last one to be the bias
            for (int j = 0; j <= SnakeGame.noHiddenNeurons-1; j++)
                vWeights[j][i] = randomDoubleInSearchSpace();

        for (int j = 0; j <= SnakeGame.noHiddenNeurons-1; j++)
        {
            for (int k = 0; k<= SnakeGame.noOutputNeurons -1; k++)
                wWeights[k][j] = randomDoubleInSearchSpace();
        }
    }

    public double randomDoubleInSearchSpace()
    {
        Random r = new Random();
        double min = minSearchSpace;
        double max = maxSearchSpace;
        double randomVal = r.nextDouble()*(max - min) + min;
        return randomVal;
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

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getMinSearchSpace() {
        return minSearchSpace;
    }

    public void setMinSearchSpace(double minSearchSpace) {
        this.minSearchSpace = minSearchSpace;
    }

    public double getMaxSearchSpace() {
        return maxSearchSpace;
    }

    public void setMaxSearchSpace(double maxSearchSpace) {
        this.maxSearchSpace = maxSearchSpace;
    }
}
