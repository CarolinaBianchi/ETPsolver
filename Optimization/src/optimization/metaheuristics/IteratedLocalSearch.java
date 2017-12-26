/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.metaheuristics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import optimization.Cloner;
import optimization.Optimizer;
import optimization.domain.CostFunction;
import optimization.domain.Exam;
import optimization.domain.Schedule;
import optimization.domain.Timeslot;

/**
 *
 * @author roby
 */
public class IteratedLocalSearch extends SingleSolutionMetaheuristic {

    private Optimizer optimizer;
    private Schedule initSolution;
    private Schedule currentSolution;
    private Schedule currentBestSolution;
    private int initCost;
    private int currentBestCost;
    private int checkObjFun;
    private int numIteration = 5;
    private long elapsedTime;
    private final long MINUTES = 2;
    private final long MAX_MILLIS = MINUTES * 60 * 1000;
    private Random random = new Random();
    private int tmax;

    public IteratedLocalSearch(Optimizer optimizer, Schedule initSolution) {
        super(optimizer, initSolution);
        this.optimizer = optimizer;
        this.initSolution = Cloner.clone(initSolution);
        this.currentSolution = initSolution;
        this.currentBestSolution = Cloner.clone(currentSolution);
        this.initCost = CostFunction.getCost(initSolution);
        this.checkObjFun = this.initCost;
        this.currentBestCost = this.initCost;
        this.tmax = initSolution.getTmax();
    }

    @Override
    void improveInitialSol() {

        long startTime = System.currentTimeMillis();

        while (elapsedTime < MAX_MILLIS) {
            localSearch();
            elapsedTime = System.currentTimeMillis() - startTime;
        }

        mySolution = currentBestSolution;
        System.out.println("IteratedLocalSearch:" + (int) currentBestSolution.getCost() + "\t");
    }

    public void localSearch() {

        int sourceTimeslot, destTimeslot, numExams, examMovePenalty;
        Exam exam;

        for (int i = 0; i < tmax; i++) {

            sourceTimeslot = random.nextInt(tmax); //***
            if (!currentSolution.getTimeslot(sourceTimeslot).isFree()) {

                numExams = currentSolution.getTimeslot(sourceTimeslot).getNExams();

                for (int j = 0; (j < numExams); j++) {

                    exam = currentSolution.getTimeslot(sourceTimeslot).getRandomExam(); //***
                    destTimeslot = random.nextInt(tmax); //getDestTimeslot(sourceTimeslot, tmax);
                    examMovePenalty = CostFunction.getExamMovePenalty(exam, sourceTimeslot, destTimeslot, currentSolution);

                    if (examMovePenalty < 0 && currentSolution.move(exam, sourceTimeslot, destTimeslot)) {
                        checkIfBest();
                        
                        System.out.println("-c \t" + currentSolution.getCost());
                        return;
                    }
                }
            }
        }
        // If i get here, it means that I have found no improving moves.
        disturbance();
    }

    /**
     * Returns the destination timeslot for this move. The destination is
     * adjacent to the source, hence if the source is the first timeslot, the
     * destination will be the second one, while if the source is the last
     * timeslot, the destination will be the penultimate one. Otherwise the
     * destination is either one timeslot befor/after the source.
     *
     * @param sourceTimeslot
     * @param tmax
     * @return
     */
    private int getDestTimeslot(int sourceTimeslot, int tmax) {
        // If it is the first timeslot, the destination timeslot will be the second one
        if (sourceTimeslot == 0) {
            return 1;

            // If the source is the last timeslot, the dest will be the penultimate one
        } else if (sourceTimeslot == tmax - 1) {
            return sourceTimeslot - 1;
        }

        // Otherwise it will be a random timeslot right before or after the source.
        return (random.nextBoolean()) ? sourceTimeslot + 1 : sourceTimeslot - 1;
    }

    /**
     * Adds some noise to the solution.
     */
    public void disturbance() {
        for (int i = 0; i < numIteration; i++) {
            currentSolution.randomMove();
            //currentSolution.randomSwap();
        }
        
        //timeslotSwap();
    }
    
    public void timeslotSwap() {
        int delta, i, j;
        
        i = random.nextInt(tmax);
        j = random.nextInt(tmax);
        delta = CostFunction.getTimeslotSwapPenalty(i, j, initSolution);
        if (delta<0) {
            initSolution.swapTimeslots(i, j);
            checkIfBest();
        }
    }

    private void checkIfBest() {
        if (currentSolution.getCost() < currentBestSolution.getCost()) {
            currentBestSolution = Cloner.clone(currentSolution);
            System.out.println("-b \t" + currentBestSolution.getCost() + "\t");
        }    
    }
    
    

}
