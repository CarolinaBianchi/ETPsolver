/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.metaheuristics;

import fileutils.SolutionWriter;
import java.util.Date;
import java.util.Random;
import optimization.Optimizer;
import optimization.domain.CostFunction;
import optimization.domain.Exam;
import optimization.domain.Schedule;
import optimization.domain.Timeslot;

/**
 *
 * @author Carolina Bianchi
 */
public class SimulatedAnnealing extends SingleSolutionMetaheuristic {

    private double temperature;
    private final double ALPHA = 0.90;
    private final int NUM_ITER;
    private final int NUM_MOV;
    private final int MINUTES = 3;
    private Random rg = new Random();
    int tmax;

    public SimulatedAnnealing(Optimizer optimizer, Schedule initSolution) {
        super(optimizer, initSolution);
        System.out.println("new simulated annealing");
        initSolution.setCost(CostFunction.getCost(initSolution.getTimeslots()));
        temperature = CostFunction.getCost(initSolution.getTimeslots())/2;
        tmax = initSolution.getTmax();
        NUM_ITER = (int) (tmax);
        NUM_MOV = NUM_ITER;
    }

    @Override
    void improveInitialSol() {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;

        while (elapsedTime < MINUTES * 60 * 1000) {
            for (int i = 0; i < NUM_ITER; i++) {
                optimizeTimeslotOrder();
                optimizeExamPosition();
            }
            temperature = temperature * ALPHA;
            System.out.println("temperature : " + (int) this.temperature + "\t cost:" + initSolution.getCost());
            elapsedTime = (new Date()).getTime() - startTime;
        }

        mySolution=initSolution;
    }

    private void optimizeTimeslotOrder() {
        int delta, i, j;
        for (int iter = 0; iter < NUM_ITER; iter++) {
            i = rg.nextInt(tmax);
            j = rg.nextInt(tmax);
            delta = swapTimeslots(i, j);
            if (!accept(delta)) {
                swapTimeslots(i, j);
            }
        }
    }

    private int swapTimeslots(int i, int j) {
        int oldCost = initSolution.getCost();
        initSolution.swapTimeslots(i, j);
        return initSolution.getCost() - oldCost;
    }

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

    private int moveExam(Exam exam, Timeslot source, Timeslot dest) {

        int oldCost = initSolution.getCost();
        initSolution.move(exam, source, dest);
        return initSolution.getCost() - oldCost;
    }

    private boolean accept(int delta) {
        if (delta < 0) {
            return true;
        }
        return Math.exp(-delta / temperature) > rg.nextDouble();
    }
}
