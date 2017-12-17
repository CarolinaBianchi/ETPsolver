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
import optimization.domain.Timeslot;

/**
 *
 * @author Carolina Bianchi
 */
public class DeepDiveAnnealing extends SingleSolutionMetaheuristic {

    private final double initTemperature;
    private double temperature;
    private final int ITER_PER_TEMPERATURE;
    private final int NUM_ITER; // number of moves per iter
    private final int MINUTES = 1;
    private final int MAX_MILLIS = MINUTES * 60 * 1000;
    private long startTime, elapsedTime, lastResetTime;
    /*
    In the second part of the available time, I check every few seconds if there has been an acceptable improvement.
     */
    private int checkObjFun;    // The objFun last time i check
    private Random rg = new Random();
    private int tmax;
    private int plateauCounter;
    private int currentBest, overallBest;
    private double k;

    public DeepDiveAnnealing(Optimizer optimizer, Schedule initSolution) {
        super(optimizer, initSolution);
        initSolution.optimizeTimeslotOrder();
        checkObjFun = initSolution.getCost();
        currentBest = checkObjFun;
        overallBest = checkObjFun;
        tmax = initSolution.getTmax();
        initTemperature = checkObjFun/tmax;
        temperature = initTemperature;
        NUM_ITER = tmax;
        ITER_PER_TEMPERATURE = tmax/2;//tmax;
        plateauCounter = 0;
        k = 0.998;
        System.out.println("Real initial obj fun:" + checkObjFun);
        
    }

    @Override
    void improveInitialSol() {
        
        System.out.println("Beginning the deep dive annealing!");
        startTime = System.currentTimeMillis();

        while (elapsedTime < MAX_MILLIS) {
            for (int i = 0; i < ITER_PER_TEMPERATURE; i++) {
                optimizeTimeslotOrder();
                optimizeExamPosition();
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
        int sourceI, destI;
        Exam exam;
        int delta;
        for (int iter = 0; iter < NUM_ITER; iter++) {
            sourceI = rg.nextInt(tmax);
            destI = rg.nextInt(tmax);
            exam = initSolution.getTimeslot(sourceI).getRandomExam();
            delta = CostFunction.getExamMovePenalty(exam, sourceI, destI, initSolution);
            // If I accept the difference in the new and old obj function, I move the exam.
            if (accept(delta)) {
                initSolution.move(exam, sourceI, destI);
            }
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
        }

    }

    /**
     * Decides whether to accept the current change.
     *
     * @param delta
     * @return
     */
    private boolean accept(int delta) {        
        if (delta < 0) {
            checkIfBest();
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
                temperature *= 500*rg.nextDouble(); //100 current best
                currentBest = -1;
                System.out.println("Breathing... New dive!");
                lastResetTime = System.currentTimeMillis() - startTime;
            }
        } else {
            plateauCounter = 0;
        }
        
        checkObjFun = actualObjFun;
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
}   