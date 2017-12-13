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
 * Class that implements the Simulated Annealing metaheuristics. The temperature
 * of the system is decreased in the first half of the available time.
 *
 * @author Carolina Bianchi
 */
public class SimulatedAnnealing extends SingleSolutionMetaheuristic {

    private final double initTemperature;
    private double temperature;
    private final int NUM_ITER;
    private final int ITER_PER_TEMPERATURE;
    private final int NUM_MOV;
    private final int MINUTES = 3;
    private Random rg = new Random();
    int tmax;

    public SimulatedAnnealing(Optimizer optimizer, Schedule initSolution) {
        super(optimizer, initSolution);
        System.out.println("new simulated annealing");
        //initSolution.setCost(CostFunction.getCost(initSolution.getTimeslots()));
        initTemperature = initSolution.getCost(); //CostFunction.getCost(initSolution.getTimeslots());
        temperature = initTemperature;
        tmax = initSolution.getTmax();
        NUM_ITER = tmax;
        ITER_PER_TEMPERATURE = tmax;
        NUM_MOV = NUM_ITER;
    }

    @Override
    void improveInitialSol() {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        long maxMillis = MINUTES * 60 * 1000;

        while (elapsedTime < maxMillis) {
            for (int i = 0; i < ITER_PER_TEMPERATURE; i++) {
                optimizeTimeslotOrder();
                optimizeExamPosition();
            }
            elapsedTime = System.currentTimeMillis() - startTime;
            updateTemperature(maxMillis, elapsedTime);
            checkIfBest();
            System.out.println((int) this.temperature + "\t" + initSolution.getCost() + "\t" + mySolution.getCost());
        
            //System.out.println("temperature : " + (int) this.temperature + "\t current:" + initSolution.getCost() + "\t best:" + mySolution.getCost());
        }

    }

    /**
     * Changes the order of the timeslots. When the change improves the
     * solution, it is always accepted, otherwise it is accepted with a certain
     * probability, depending on the current temperature.
     */
    private void optimizeTimeslotOrder() {
        int delta, i, j, k;
        for (int iter = 0; iter < NUM_ITER ; iter++) {
            i = rg.nextInt(tmax);
            j = rg.nextInt(tmax);
            delta = swapTimeslots(i, j);
            if (!accept(delta)) {
                swapTimeslots(i, j);
            }
        }
        /*for (int iter = 0; iter < NUM_ITER / 2; iter++) {
            boolean clockwise = rg.nextBoolean();
            i = rg.nextInt(tmax);
            j = rg.nextInt(tmax);
            k = rg.nextInt(tmax);
            delta = swapTimeslots(i, j, k, clockwise);
            if (!accept(delta)) {
                swapTimeslots(i, j, k, !clockwise);
            }
        }*/
    }

    /**
     * Swaps 2 timeslots.
     *
     * @param i
     * @param j
     * @return the difference between the old and new cost.
     */
    private int swapTimeslots(int i, int j) {
        int oldCost = initSolution.getCost();
        initSolution.swapTimeslots(i, j);
        return initSolution.getCost() - oldCost;
    }

    /**
     * Swaps 3 timeslots clockwise/counterclockwise.
     *
     * @param i
     * @param j
     * @param k
     * @param clockwise
     * @return
     */
    private int swapTimeslots(int i, int j, int k, boolean clockwise) {
        int oldCost = initSolution.getCost();
        initSolution.swapTimeslots(i,j);
        initSolution.swapTimeslots(j, k);
        if(!clockwise){
            initSolution.swapTimeslots(i,j);
        initSolution.swapTimeslots(j, k);
        }
        return initSolution.getCost() - oldCost;
    }

    /**
     * Moves some exams. The move is always accepted if it improves the
     * objective function, otherwise it is accepted with a probability that
     * depends on the current temperature.
     */
    private void optimizeExamPosition() {
        Timeslot source, dest;
        Exam exam;
        int delta;
        for (int iter = 0; iter < NUM_MOV; iter++) {
            source = initSolution.getRandomTimeslot();
            dest = initSolution.getRandomTimeslot();
            exam = source.getRandomExam();
            delta = moveExam(exam, source, dest);
            if (!accept(delta)) {
                moveExam(exam, dest, source);
            }
        }
    }

    /**
     * Moves a single exam, from its source to the destination.
     *
     * @param exam
     * @param source
     * @param dest
     * @return the difference of cost of the current schedule and the old
     * schedule.
     */
    private int moveExam(Exam exam, Timeslot source, Timeslot dest) {
        int oldCost = initSolution.getCost();
        initSolution.move(exam, source, dest);
        return initSolution.getCost() - oldCost;
    }

    /**
     * Checks if the current solution is the best found so far.
     */
    private void checkIfBest() {
        if (initSolution.getCost() < mySolution.getCost()) {
            mySolution = Cloner.clone(initSolution);
        }
    }

    /**
     * Decides wether to accept the current change.
     *
     * @param delta
     * @return
     */
    private boolean accept(int delta) {
        return (delta < 0) || (Math.exp(-delta / temperature)) > rg.nextDouble();
    }

    /**
     * Updates the temperature according to an anti logarithmic decay.
     *
     * @param maxMillis
     * @param elapsedTime
     */
    private void updateTemperature(long maxMillis, long elapsedTime) {
        if (elapsedTime > maxMillis / 2) {
            return;
        }
        double offset = initTemperature / (Math.log(maxMillis / 2 + 1));
        temperature = Math.max(initTemperature / (Math.log(elapsedTime + 1)) - offset, 1);
    }
}
