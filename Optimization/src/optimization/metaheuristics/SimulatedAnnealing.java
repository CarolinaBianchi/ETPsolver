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
 * <pre>
 * Class that implements the Simulated Annealing metaheuristics. The algorithm
 * works as a classic simulated annealing algorithm. The crucial part is the
 * temperature dacay schedule, that now works as follows (feel free to modify
 * anything, evidence says it doesnt work so well):
 * 1- The initial temperature is set to the value of the objective function of
 * the initial solution
 * 2- For a fraction of the available time (<code>FASTDECAY_TIME_FRACTION</code>)
 * the temperature is decreased by means of an antilogarithmic schedule, so that
 * after this fraction of time the temperature reaches 0.
 * 3 - Every <code>checkIntervalLength</code> ms the improvement of the cost
 * function is checked, and if it doesnt satisfy a certain constraint, the
 * temperature is increased again to a value = <code> initTemperature * 100</code>. It is then
 * decreased by means of an exponential decay.
 * </pre>
 *
 * @author Carolina Bianchi
 */
public class SimulatedAnnealing extends SingleSolutionMetaheuristic {

    private final double initTemperature;
    private double temperature;
    private final int ITER_PER_TEMPERATURE;
    private final int NUM_ITER; // number of moves per iter
    private final int MINUTES = 1;
    private final int MAX_MILLIS = MINUTES * 60 * 1000;
    private long elapsedTime;
    private final double FASTDECAY_TIME_FRACTION = 1.0 / 5.0; // The fraction of time in which the temperature is decreased fast
    /*
    In the second part of the available time, I check every few seconds if there has been an acceptable improvement.
     */
    private long checkTime = 0; // Last time I checked
    private int checkObjFun;    // The objFun last time i check
    private long checkIntervalLength = 1 * 1000; // I check every 10 sec (it's increased during the algorithm)
    private Random rg = new Random();
    int tmax;

    public SimulatedAnnealing(Optimizer optimizer, Schedule initSolution) {
        super(optimizer, initSolution);
        initSolution.setCost(CostFunction.getCost(initSolution));
        checkObjFun = initSolution.getCost();
        initTemperature = checkObjFun;
        temperature = initTemperature;
        tmax = initSolution.getTmax();
        NUM_ITER = tmax / 2;
        ITER_PER_TEMPERATURE = tmax / 2;//tmax;
    }

    @Override
    void improveInitialSol() {
        long startTime = System.currentTimeMillis();
        long maxMillis = MINUTES * 60 * 1000;
        while (elapsedTime < maxMillis) {
            for (int i = 0; i < ITER_PER_TEMPERATURE; i++) {
                optimizeTimeslotOrder();
                optimizeExamPosition();
                elapsedTime = System.currentTimeMillis() - startTime;
            }
            updateTemperature();
            System.out.println((int) this.temperature + "\t" + initSolution.getCost() + "\t");

            //System.out.println("temperature : " + (int) this.temperature + "\t current:" + initSolution.getCost() + "\t best:" + mySolution.getCost());
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
     * Decides wether to accept the current change.
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
        if (elapsedTime > MAX_MILLIS * FASTDECAY_TIME_FRACTION) {
            checkPlateau();
            return;
        }

        temperature = Math.max(antiLogDecay(initTemperature, elapsedTime, MAX_MILLIS * FASTDECAY_TIME_FRACTION), rg.nextDouble() * 50.0 + 1);
    }

    /**
     * Checks if I'm in a plateau. When <code>checkIntervalLength</code> time
     * has passed from the last time I checked, I check if the improvement of
     * the obj function is satisfying (&lt-1%...), if it's not, the temperature
     * is increased back to the initial temperature (may be too much).
     */
    private void checkPlateau() {
        if (checkTime == 0 || System.currentTimeMillis() - checkTime > checkIntervalLength) {
            checkTime = System.currentTimeMillis();
            double delta = (initSolution.getCost() - checkObjFun) * 1.0 / checkObjFun * 1.0;
            if (delta > -0.01) { // Bad improvement
                temperature = 1024; // add some randomness
            } else {
                checkObjFun = initSolution.getCost();
            }
        } else {
            temperature = Math.max(temperature / 2, 1);
            //temperature = Math.max(antiLogDecay(initTemperature, elapsedTime, MAX_MILLIS * FASTDECAY_TIME_FRACTION), rg.nextDouble() * 50.0);
        }
    }

    /**
     * Returns the value of the function f(x)=K1/log(x+1)-K2. Where K1 =
     * <code>initVal</code> and k2 = f(<code>xStar</code>). f(xStar) = 0.
     *
     * @param initVal
     * @param x
     * @param xStar
     * @return
     */
    private double antiLogDecay(double initVal, double x, double xStar) {
        double offset = initVal / (Math.log(xStar + 1));
        return initVal / (Math.log(x + 1)) - offset;
    }

    /**
     * Checks if the new solution is better than the best found sofar.
     */
    private synchronized void checkIfBest() {
        if (initSolution.getCost() < mySolution.getCost()) {
            mySolution = Cloner.clone(initSolution);
        }
    }
}
