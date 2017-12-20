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
public class DeepDiveAnnealingv2 extends SingleSolutionMetaheuristic {

    private final int MINUTES = 3;
    private final int MAX_MILLIS = MINUTES * 60 * 1000;
    private long startTime, elapsedTime, lastResetTime;
    private int checkObjFun;    // The objFun last time i check
    private Random rg = new Random();
    private int tmax;
    private int currentBest, overallBest;
    
    // Simulated annealing parameters and variables
    private final double initTemperature;
    private double temperature;
    private final int ITER_PER_TEMPERATURE;
    private final int NUM_ITER; // number of moves per iter
    private int plateauCounter;
    private double k;
    
    // Tabu Search parameters and variables
    private TabuList tabuList;
    private final int TABU_START_SIZE = 10;
    private final int MAX_MOVES = 5;
    private int NUM_MOVES = 1;


    public DeepDiveAnnealingv2(Optimizer optimizer, Schedule initSolution) {
        super(optimizer, initSolution);
        checkObjFun = initSolution.getCost();
        currentBest = checkObjFun;
        overallBest = checkObjFun;
        tmax = initSolution.getTmax();
        
        // Settings for tabu search
        this.tabuList = new TabuList(TABU_START_SIZE);
        
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
        }
    }
    
    /**
     * Changes the order of the timeslots. When the change improves the
     * solution, it is always accepted, otherwise it is accepted with a certain
     * probability, depending on the current temperature.
     */
    private void optimizeTimeslotOrder() {
        int delta, i, j;
        
        i = rg.nextInt(tmax);
        j = rg.nextInt(tmax);
        delta = CostFunction.getTimeslotSwapPenalty(i, j, initSolution);
        if (accept(delta)) {
            initSolution.swapTimeslots(i, j);
            checkIfBest();
        }
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
     * Checks if I'm in a plateau. When <code>checkIntervalLength</code> time
     * has passed from the last time I checked, I check if the improvement of
     * the obj function is satisfying (&lt-1%...), if it's not, the temperature
     * is increased back to the initial temperature (may be too much).
     */
    private void checkPlateau() {
        int actualObjFun = initSolution.getCost();

        double delta = (double) checkObjFun/actualObjFun;
        if ( getTimeFromReset() > 1 && delta < 1.001 && delta > 0.999 ) {
            plateauCounter++;
            if (plateauCounter == 100) {
                plateauCounter = 0;
                temperature *= 1000*rg.nextDouble(); //100 current best
                currentBest = -1;
                System.out.println("Breathing... New dive!");
                lastResetTime = System.currentTimeMillis() - startTime;
                if(NUM_MOVES<MAX_MOVES) NUM_MOVES++;
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

        for( int i = 0; i<NUM_MOVES; i++) {
            sourceIndex = rg.nextInt(tmax);
            destIndex = rg.nextInt(tmax);
            ex = initSolution.getTimeslot(sourceIndex).getRandomExam();

            penalty = CostFunction.getExamMovePenalty(ex, sourceIndex, destIndex, initSolution);
            
            if (!accept(penalty) || !initSolution.isFeasibleMove(ex, destIndex) || ( tabuList.moveIsTabu(ex, sourceIndex, destIndex) && (this.checkObjFun+penalty >= currentBest) )) {
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
        tabuList.updateTabuList(e, dest, src);
        
        checkIfBest();
    }
}   