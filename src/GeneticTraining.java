import java.util.ArrayList;

/**
 * Created by Adriaan on 2016/11/11.
 */
public class GeneticTraining
{
    public int populationSize;
    ArrayList<Chromosone> chromosones;
    public int curChromosone;

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
            c = new Chromosone();
            chromosones.add(c);
        }
        curChromosone = 0;
    }

    public Chromosone getCurChromosone()
    {
        return chromosones.get(curChromosone);
    }




}
