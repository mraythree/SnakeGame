import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

/**
 * Created by Adriaan on 2016/11/11.
 */
public class GeneticTraining
{
    public int populationSize, hallOfFameSize, tournamentSize;
    ArrayList<Chromosone> chromosones;
    public int curChromosone;
    public double mutationRate;

    //this gets called once. this is tricky.
    //in order to evaluate the fitness function, we need to play a game of snake.
    //this means, that for every chromosone in the population we have to play a game of snake before we do anything with the population.
    public GeneticTraining(int populationSize)
    {
        this.populationSize = populationSize;
        chromosones = new ArrayList<>();
        Chromosone c;
        for (int i = 0; i <= populationSize -1; i++)
        {
            c = new Chromosone(true);
            chromosones.add(c);
        }
        curChromosone = 0;
        hallOfFameSize = 2;
        tournamentSize = 3;
        mutationRate = 10;
    }

    //once a population of chomozones have been evaluated, do the GA.
    public void Train()
    {
        //comparator is what we use to sort the list. we want to sort them in order of highest to lowest fitness.
        Comparator<Chromosone> comp = new Comparator<Chromosone>()
        {
            @Override
            public int compare(Chromosone o1, Chromosone o2)
            {
                if (o1.getFitness() < o2.getFitness())
                    return 1;
                if (o1.getFitness() > o2.getFitness())
                    return -1;
                return 0;
            }
        };

        Collections.sort(chromosones, comp);
        ArrayList<Chromosone> nextGen = new ArrayList<>(); //this will eventually be the new population of chromozones.
        for (int i = 0; i< hallOfFameSize; i++) //let the first fittest individuals take the first slots to the halloffamesize counter.
        {
            nextGen.add(chromosones.get(i).clone());
        }

        //cross and mutate a child from the best 2 (this assumes that the halloffamesize is 2.
        Chromosone c1 = nextGen.get(0);
        Chromosone c2 = nextGen.get(1);
        Chromosone cross = crossover(c1, c2);
        cross = mutate(cross);
        //cross = mutatePositive(cross);
        nextGen.add(cross);

        //do tournament selection for the rest of the population.
        for (int i = 0; i< populationSize-(hallOfFameSize+1); i++)
        {
            c1 = doTournamentSelection(chromosones, comp);
            c2 = doTournamentSelection(chromosones, comp);
            cross = crossover(c1, c2);
            cross = mutate(cross);
            //cross = mutatePositive(cross);
            nextGen.add(cross);
        }
        chromosones = nextGen;
    }

    private Chromosone doTournamentSelection(ArrayList<Chromosone> chromosones, Comparator<Chromosone> comp)
    {
        Chromosone out = null;
        double lowestFitness = -9999999;
        ArrayList<Chromosone> tournamentChromosones = new ArrayList<>();
        for (int i = 0; i < tournamentSize; i++)
        {
            int randI = randomInt1andPopSize();
            Chromosone temp = chromosones.get(randI);
            tournamentChromosones.add(temp);
        }

        Collections.sort(tournamentChromosones, comp);
        out = tournamentChromosones.get(0);
        return out;
    }

    private Chromosone mutate(Chromosone c)
    {
        double diff;
        Random R = new Random();
        //v
        for (int i = 0; i < SnakeGame.noHiddenNeurons; i++)
        {
            for (int j = 0; j < SnakeGame.noInputsNeurons; j++)
            {
                if (randomInt1andPopSize() > populationSize - mutationRate)
                {
                    if (randomInt1andPopSize() > populationSize/2) //50% chance
                    {
                        if (j > 5)
                            diff = randomGauss();
                        else
                            //diff = R.nextDouble() * minSearchSpace;
                            diff = c.getvWeights()[i][j] + R.nextDouble() * SnakeGame.minSearchSpace/50;
                    }
                    else
                    {
                        if (j > 5)
                            diff = randomGauss();
                        else
                            //diff = R.nextDouble() * maxSearchSpace;
                            diff = c.getvWeights()[i][j] + R.nextDouble() * SnakeGame.maxSearchSpace/50;
                    }
                    c.vWeights[i][j] = diff;
                }
            }
        }

        //w
        for (int i = 0; i < SnakeGame.noOutputNeurons; i++)
        {
            for (int j = 0; j < SnakeGame.noHiddenNeurons; j++)
            {
                if (randomInt1andPopSize() > populationSize - mutationRate)
                {
                    if (randomInt1andPopSize() > populationSize/2)
                    {
                        if (i > 2)
                            diff = randomGauss();
                        else
                            //diff = R.nextDouble() * minSearchSpace;
                            diff = c.getwWeights()[i][j] + R.nextDouble() * SnakeGame.minSearchSpace/50;
                    }
                    else
                    {
                        if (i > 2)
                            diff = randomGauss();
                        else
                            //diff = R.nextDouble() * maxSearchSpace;
                            diff = c.getwWeights()[i][j] + R.nextDouble() * SnakeGame.maxSearchSpace/50;
                    }
                    c.wWeights[i][j] = diff;
                }
            }
        }

        return c;
    }

    //get a value around 1 to move in.
    public double randomGauss()
    {
        Random r = new Random();
        double desiredStandardDeviation = 0.05;
        double desiredMean = 1;
        return r.nextGaussian()*desiredStandardDeviation+desiredMean;
    }

    public Chromosone crossover(Chromosone c1, Chromosone c2)
    {
        Chromosone out = new Chromosone();
        double[][] newVWeights = new double[SnakeGame.noHiddenNeurons][SnakeGame.noInputsNeurons];
        double[][] newWWeights = new double[SnakeGame.noOutputNeurons][SnakeGame.noHiddenNeurons];
        //v
        for (int i = 0; i < SnakeGame.noHiddenNeurons; i++)
        {
            for (int j = 0; j < SnakeGame.noInputsNeurons; j++)
            {
                int r = randomInt1andPopSize();
                if (r % 2 == 0)
                    newVWeights[i][j] = c1.getvWeights()[i][j];
                else
                    newVWeights[i][j] = c2.getvWeights()[i][j];
            }
        }
        //w
        for (int i = 0; i < SnakeGame.noOutputNeurons; i++)
        {
            for (int j = 0; j < SnakeGame.noHiddenNeurons; j++)
            {
                int r = randomInt1andPopSize();
                if (r % 2 == 0)
                    newWWeights[i][j] = c1.getwWeights()[i][j];
                else
                    newWWeights[i][j] = c2.getwWeights()[i][j];
            }
        }
        out.setvWeights(newVWeights);
        out.setwWeights(newWWeights);
        return out;
    }

    public int randomInt1andPopSize()
    {
        Random r = new Random();
        int min = 0;
        int max = populationSize;
        int randomVal = r.nextInt((max - min)) + min;
        return randomVal;
    }

    public Chromosone getCurChromosone()
    {
        return chromosones.get(curChromosone);
    }

    public void incCurChromosone()
    {
        if (curChromosone < populationSize-1)
            curChromosone++;
        else
            curChromosone = 0;
    }




}
