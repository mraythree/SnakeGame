# SnakeGame
A classic game of snake that is controlled by a neural network (NN) and trained using a genetic algorithm (GA)

Written in Java, the objective of the game is to maximize the snake's length without running into it's own body or the boundry wall. 
The game is run using a neural network made up 15 input neurons, 7 hidden layer neurons and 4 output neurons. 

--------------------------------------------
Input neurons consist of (note, all values are taken as x and y PIXEL coordinates):

0: position of head x

1: position of head y 

2: position of midpoint of body x

3: position of midpoint of body y

4: position of tail x

5: position of tail y

6: position of food x

7: position of food y

8: distance to food x

9: distance to food y

10: is the block on the left of the head free? (1 or 0)

11: is the block on the right of the head free? (1 or 0)

12: is the block on the up of the head free? (1 or 0)

13: is the block on the down of the head free? (1 or 0)

14: bias (-1)

(note the bias is -1, as the formula in the activation function is given as a negative

Output neruons are:

0: move left

1: move right

2: move down

3: move up

--------------------------------------------

Training of the neural network happens through a genetic algorithm through the updating of weights that are optimized around a fitness function.
Random weights are initially generated for the population size of the genetic alorithm (GA) and then a game of snake is played by the NN for every chromozone in the population
At the end of each playing cycle, the GA performs cross overs and mutations around a fitness function in order to try and better each chromozone's score. 
The fitness function is: 
  if (stalled)
     out = (stepsTaken/4) + (snakeLength * 100000.0);
  else
     out = (stepsTaken/2) + (snakeLength * 100000.0);

It should be noted, that the stalled value is 1, when a game ends because the snake has not found food with a set number of moves. 
