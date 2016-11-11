import javax.swing.*;

/**
 * Created by Adriaan on 2016/09/20.
 */
public class Main extends JFrame
{
    public static SnakeGame snakeGame;
    public Main()
    {
        snakeGame = SnakeGame.getClassInstance();
        add(snakeGame);
        setResizable(false); //we don't want to let the user resize the window
        pack(); //take the dimensions of the frame we are loading
        setTitle("Snake Being Trained"); //we should be the generation number in the title.
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args)
    {
        JFrame ex = new Main();
        ex.setVisible(true);
        try
        {
            snakeGame.runGame();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
