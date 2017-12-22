/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.metaheuristics;

import java.util.Random;
import optimization.Cloner;
import optimization.Optimizer;
import optimization.domain.CostFunction;
import optimization.domain.Exam;
import optimization.domain.Schedule;

/**
 * 
 * @author Flavio Lorenzo
 */
public class DeepDiveAnnealingV2 extends SingleSolutionMetaheuristic {

    private final int MINUTES = 3;
    private final int MAX_MILLIS = MINUTES * 60 * 1000;
    // Respectively, the time the metaheuristic starts, the elapsed time since 
    // the metaheuristic start and how much time has passed since the last reset
    private long startTime, elapsedTime, lastResetTime;
    private int actualObjFun, checkObjFun;    // The objFun last time i check
    private Random rg = new Random();
    private int tmax;
    private int currentBest, overallBest;
    
    // Simulated annealing parameters and variables
    private final double initTemperature; // The initial temperature
    private double temperature; // The current temperature
    private final int ITER_PER_TEMPERATURE; // Just as the name says
    private final int NUM_ITER; // number of moves per iter
    private int plateauCounter; // The reset counter
    private double k; // The coefficient for which the temperature is multiplied 
                      // (hence, decreased) after ITER_PER_TEMPERATURE iterations.
    private final int ENHANCEMENT_LIMIT = 1000; // The limit of the temperature
                      // enhancement performed at each reset
    private final double DELTA_RANGE = 0.001; // The range of obj funct variations
                      // that increment the plateauCounter

    
    // Tabu Search parameters and variables
    private TabuList examTabuList, timeslotTabuList;
    private final int TABU_EXAM_START_SIZE = 10; // The starting size for the
                           // exam tabu list
    private final int TABU_TIMESLOT_START_SIZE = 5; // The starting size for the
                           // timeslot tabu list
    private final int MAX_MOVES = 5; // Maximum number of exam moves allowed 
                            // before choosing the best one (if any).
    private final int MAX_SWAPS = 3; // Maximum number of timeslot swaps allowed 
                            //before choosing the best one (if any).
    private int num_moves = 2; // Initial number of exam moves before choosing 
                            // the best one
    private int num_swaps = 1; // Initial number of timeslot swaps before choosing 
                            // the best one
    
    // Iterated Local Search parameters and variables
    final private int NUM_DISTURBANCE_ITERATIONS = 50; // Number of disturbance
                            // moves performed


    public DeepDiveAnnealingV2(Optimizer optimizer, Schedule initSolution) {
        super(optimizer, initSolution);
        actualObjFun = initSolution.getCost();
        checkObjFun = actualObjFun;
        currentBest = actualObjFun;
        overallBest = actualObjFun;
        tmax = initSolution.getTmax();
        
        // Settings for tabu search
        this.examTabuList = new TabuList(TABU_EXAM_START_SIZE);
        this.timeslotTabuList = new TabuList(TABU_TIMESLOT_START_SIZE);
        
        // Settings for simulated annealing
        initTemperature = checkObjFun/tmax;
        temperature = initTemperature;
        NUM_ITER = tmax;
        ITER_PER_TEMPERATURE = tmax/2;//tmax;
        plateauCounter = 0;
        k = 0.998;   
    }

    @Override
    void improveInitialSol() {
        
        System.out.println("Beginning the deep dive annealing!");
        startTime = System.currentTimeMillis();

        while (elapsedTime < MAX_MILLIS) {
            for (int i = 0; i < ITER_PER_TEMPERATURE; i++) {
                optimizeExamPosition();
                optimizeTimeslotOrder();
            }
            elapsedTime = System.currentTimeMillis() - startTime;
            checkPlateau();
            updateTemperature();
            //System.out.println("temperature : " + (int) this.temperature + " current:" + initSolution.getCost() + " best:" + mySolution.getCost());
            //System.out.println("Elapsed time: " + elapsedTime/1000 + " seconds.");
        }

    }

    /**
     * Moves some exam. The move is always accepted if it improves the objective
     * function, otherwise it is accepted with a probability that depends on the
     * current temperature.
     */
    private void optimizeExamPosition() {
        for (int iter = 0; iter < NUM_ITER; iter++) {
            tabuMove();
            actualObjFun = initSolution.getCost();
        }
    }
    
    /**
     * Changes the order of the timeslots. When the change improves the
     * solution, it is always accepted, otherwise it is accepted with a certain
     * probability, depending on the current temperature.
     */
    private void optimizeTimeslotOrder() {
        timeslotTabuMove();
        
        /* Regular function mode
        
        int delta, i, j;
        
        i = rg.nextInt(tmax);
        j = rg.nextInt(tmax);
        delta = CostFunction.getTimeslotSwapPenalty(i, j, initSolution);
        if (accept(delta)) {
            initSolution.swapTimeslots(i, j);
            checkIfBest();
        }*/
        
        actualObjFun = initSolution.getCost();
    }


    /**
     * Checks if the new solution is better than the best found sofar.
     */
    private synchronized void checkIfBest() {
        int cost = initSolution.getCost();
        boolean isBest = false;
        if ( cost < overallBest) {
            mySolution = Cloner.clone(initSolution);
            overallBest = cost;
            isBest = true;
        } else if( ( currentBest < 0 || cost < currentBest ) && getTimeFromReset()>0.5 ) {
            isBest = true;
        }
        if( isBest ) {
            plateauCounter = 0;
            currentBest = cost;
            System.out.println("New Best! c: " + currentBest + " - a: " + overallBest + " -t: " + (int) temperature);
        }
    }
    
    /**
     * Returns the time from the last reset in seconds
     * @return The time from the last reset in seconds
     */
    private long getTimeFromReset() {
        return ( elapsedTime - lastResetTime ) / 1000;
    }
    
    //******METHODS FOR SIMULATED ANNEALING******************
    
    
    /**
     * Decides whether to accept the current change.
     *
     * @param delta
     * @return
     */
    private boolean accept(int delta) {        
        if (delta < 0) {
            return true;
        }
        
        return Math.exp(-delta / temperature) > rg.nextDouble();
    }
    
    /**
     * Updates the temperature according to an anti logarithmic decay.
     *
     * @param maxMillis
     * @param elapsedTime
     */
    private void updateTemperature() {
        temperature = Math.max(temperature*k, 1);
    }

    /**
     * Checks if I'm in a plateau. Every time delta below a certain threshold (that is,
     * the variation that occurred in the previous iteration isn't significant ).
     * i increment a counter by one and i set it to zero as soon as delta is above the 
     * selected threshold. If counter reaches a threshold Th (there haven't been significant
     * variations in the last Th iterations) i try to enhance the temperature. 
     * In the meanwhile, a number of changes occurs:
     *      - Counter is set to zero
     *      - Temperature is enhanced randomly
     *      - The currentBest is reset
     *      - Keep track of the time the reset occurs
     *      - Update the number of moves and swaps performed in the tabu search algorithm.
     */
    private void checkPlateau() {        
        // This delta keeps track of the variation between the actual obj function
        // and the previous one.
        double delta = (double) checkObjFun/actualObjFun;
        
        // If the variation isn't significant, update the counter.
        if ( getTimeFromReset() > 1 && delta > 1-DELTA_RANGE && delta < 1+DELTA_RANGE ) {
            plateauCounter++;
            
            // If counter reached the threshold, reset the temperature.
            if (plateauCounter == 100) {
                plateauCounter = 0; // Reset the counter
                temperature *= ENHANCEMENT_LIMIT*rg.nextDouble(); // Enhance temperature
                currentBest = -1; // Reset the current best
                System.out.println("Breathing... New dive!"); // Notify the new "dive"
                lastResetTime = System.currentTimeMillis() - startTime; // Keep track of last reset time
                // If allowed, update the number of moves and swaps performed in the tabu search algorithm.
                if(num_moves<MAX_MOVES) num_moves++;
                if(num_swaps<MAX_SWAPS) num_swaps++;
                
                // Create some disturbance
                //disturbance();
            }
        } else {
            plateauCounter = 0;
        }
        
        checkObjFun = actualObjFun;
    }
    
    
    //******METHODS FOR TABU LIST LOGIC******************
   
    private void tabuMove() {
        Exam ex, bestEx;
        int sourceIndex, destIndex, bestSourceIndex, bestDestIndex;
        int penalty, bestPenalty;
        boolean allowMove = true;
        bestPenalty = Integer.MAX_VALUE;
        bestEx = null;
        bestSourceIndex = 0;
        bestDestIndex = 0;

        for( int i = 0; i<num_moves; i++) {
            sourceIndex = rg.nextInt(tmax);
            destIndex = rg.nextInt(tmax);
            ex = initSolution.getTimeslot(sourceIndex).getRandomExam();

            penalty = CostFunction.getExamMovePenalty(ex, sourceIndex, destIndex, initSolution);
            
            if (!accept(penalty) || !initSolution.isFeasibleMove(ex, destIndex) || ( examTabuList.moveIsTabu(ex, sourceIndex, destIndex) && (this.actualObjFun+penalty >= currentBest) )) {
                allowMove = false;
            }
            
            if( allowMove && penalty<bestPenalty) {
                //System.out.println("Accept: " + penalty);
                bestPenalty = penalty;
                bestSourceIndex = sourceIndex;
                bestDestIndex = destIndex;
                bestEx = ex;
            }

            allowMove = true;
        }

        if(bestEx != null) {
            //System.out.println("Executing");
            executeBestMove(bestEx, bestSourceIndex, bestDestIndex);
        }

    }
    
    private void executeBestMove(Exam e, int src, int dest) {
        initSolution.move(e, src, dest);
        examTabuList.updateTabuList(e, dest, src);
        
        checkIfBest();
    }
    
    private void timeslotTabuMove() {
        int sourceIndex, destIndex, bestSourceIndex, bestDestIndex;
        int penalty, bestPenalty;
        boolean allowMove = true;
        bestPenalty = Integer.MAX_VALUE;
        bestSourceIndex = tmax;
        bestDestIndex = tmax;

        for( int i = 0; i<num_moves; i++) {
            sourceIndex = rg.nextInt(tmax);
            destIndex = rg.nextInt(tmax);

            penalty = CostFunction.getTimeslotSwapPenalty(sourceIndex, destIndex, initSolution);
            
            if (!accept(penalty) || ( timeslotTabuList.moveIsTabu(sourceIndex, destIndex) && (actualObjFun+penalty >= currentBest) )) {
                allowMove = false;
            }
            
            if( allowMove && penalty<bestPenalty) {
                //System.out.println("Accept: " + penalty);
                bestPenalty = penalty;
                bestSourceIndex = sourceIndex;
                bestDestIndex = destIndex;
            }

            allowMove = true;
        }

        if(bestSourceIndex != tmax) {
            //System.out.println("Executing");
            executeBestSwap(bestSourceIndex, bestDestIndex);
        }
    }

    private void executeBestSwap(int src, int dest) {
        initSolution.swapTimeslots(src, dest);
        timeslotTabuList.updateTabuList(dest, src);

        checkIfBest();
    }
    
    //******METHODS FOR ITERATED LOCAL SEARCH LOGIC******************
    
    /**
     * Adds some noise to the solution.
     */
    public void disturbance() {
        for (int i = 0; i < NUM_DISTURBANCE_ITERATIONS; i++) {
            initSolution.randomMove();
        }
        
        this.checkIfBest();
    }
}   