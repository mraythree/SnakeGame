import java.util.Random;

/**
 * Created by Adriaan on 2016/11/10.
 */
public class neuralNetWork
{
    int noInputsNeurons, noOutputNeurons, noHiddenNeurons;
    public double[][] wWeights;
    public double[][] vWeights;

    public neuralNetWork()
    {
        noInputsNeurons = 15; //head x and y, mid x and y, tail x and y, is space free in the direction left, right up and down, food x and y, distance to food x and y, bias;
        noHiddenNeurons = 5;
        noOutputNeurons = 4;
        vWeights = new double[noHiddenNeurons][noInputsNeurons]; //2D array for weights going from every input node, to every hidden node.
        wWeights = new double[noOutputNeurons][noHiddenNeurons]; //2D array for weights going from every hidden node to every output node

        setWeightsOfNN();
    }

    //will take the weights from the GA and set them here.
    public void setWeightsOfNN(double[][] inputtedVWeights, double[][]  inputtedWWeights)
    {
        //give initial weights
        vWeights = inputtedVWeights;
        wWeights = inputtedWWeights;
    }

    //takes the inputs and then uses the NN to get a move out.
    public int getNextMove(double[] inputs)
    {
        double highestActual = 0;
        int toOutput = 0;
        for (int j = 0; j <= noOutputNeurons - 1; j++)
        {
            double actual = calcuateFNetHiddenAndOutput(inputs).storedOutputNets[j];
            if (actual > highestActual)
            {
                highestActual = actual;
                toOutput = j;
            }
        }
        return toOutput;
    }

    //for testing purposes
    public void setWeightsOfNN()
    {
        //give initial weights
        for (int i = 0; i<= noInputsNeurons -1; i++) //set the last one to be the bias
            for (int j = 0; j <= noHiddenNeurons-1; j++)
                vWeights[j][i] = randomDouble();

        for (int j = 0; j <= noHiddenNeurons-1; j++)
        {
            for (int k = 0; k<= noOutputNeurons -1; k++)
                wWeights[k][j] = randomDouble();
        }
    }

    //based on the weights, calculate the NN;
    private storedNetClass calcuateFNetHiddenAndOutput(double[] inputPattern)
    {
        double[] hiddenFNets = new double[noHiddenNeurons];
        double[] outputFNets = new double[noOutputNeurons];

        double outputNet, hiddenNet;
        for (int k = 0; k<= noOutputNeurons -1; k++)
        {
            outputNet = 0.0;
            for (int j = 0; j<= noHiddenNeurons-1; j++)
            {
                hiddenNet = 0.0;
                for (int i = 0; i<= noInputsNeurons-1; i++)
                {
                    hiddenNet = hiddenNet + (vWeights[j][i] * inputPattern[i]);
                }
                if (j == (noHiddenNeurons -1)) //bias for hidden layer
                    hiddenFNets[j] = -1.0;
                else
                    hiddenFNets[j] = getActivationFunctionValue(hiddenNet, "sig");

                outputNet = outputNet + (wWeights[k][j] * hiddenFNets[j]);
            }
            outputFNets[k] = getActivationFunctionValue(outputNet, "sig");
        }
        return new storedNetClass(hiddenFNets, outputFNets);
    }


    public double randomDouble()
    {
        Random r = new Random();
        double min = -1 / Math.sqrt(noInputsNeurons);
        double max = 1/ Math.sqrt(noInputsNeurons);
        double randomVal = (r.nextDouble() * (max - min)) + min;
        return randomVal;
    }

    private double getActivationFunctionValue(double in, String activFunc)
    {
        double result;
        if (activFunc.equals("lin"))                //linear
        {
            return in;
        }
        else //if (activFunc.equals("sig"))         //sigmoid
        {
            result = 1.0 / (1.0 + (Math.exp(-in)));
            return result;
        }
    }
}
