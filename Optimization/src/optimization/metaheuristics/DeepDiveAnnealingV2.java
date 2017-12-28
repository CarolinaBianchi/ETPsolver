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
    private Random rg = new Random(System.currentTimeMillis());
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
    private final int ENHANCEMENT_LIMIT = 750; // The limit of the temperature
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
        initTemperature = checkObjFun / (tmax*4);
        temperature = initTemperature;
        NUM_ITER = tmax;
        ITER_PER_TEMPERATURE = tmax / 2;//tmax;
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
            updateElapsedTime();
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
            tabuEMove();
            actualObjFun = initSolution.getCost();
        }
    }

    /**
     * Changes the order of the timeslots. When the change improves the
     * solution, it is always accepted, otherwise it is accepted with a certain
     * probability, depending on the current temperature.
     */
    private void optimizeTimeslotOrder() {
        tabuTSwap();
        actualObjFun = initSolution.getCost();
    }

    /**
     * Checks if the new solution is better than the best found sofar.
     */
    private synchronized void checkIfBest() {
        int cost = initSolution.getCost();
        boolean isBest = false;
        if (cost < overallBest) {
            mySolution = Cloner.clone(initSolution);
            mySolution.setSSProcessed(true);
            overallBest = cost;
            isBest = true;
        } else if ((currentBest < 0 || cost < currentBest) && getTimeFromReset() > 0.5) {
            isBest = true;
        }
        if (isBest) {
            plateauCounter = 0;
            currentBest = cost;
            //System.out.println("New Best! c: " + currentBest + " - a: " + overallBest + " -t: " + (int) temperature);
        }
        //System.out.println((int) temperature + "\t" + initSolution.getCost() + "\t" + overallBest);
    }

    /**
     * Returns the time from the last reset in seconds
     *
     * @return The time from the last reset in seconds
     */
    private long getTimeFromReset() {
        return (elapsedTime - lastResetTime) / 1000;
    }

    //******METHODS FOR SIMULATED ANNEALING******************
    /**
     * Decides whether to accept the current change.
     *
     * @param delta
     * @return
     */
    private boolean accept(int delta) {
        return delta < 0 || Math.exp(-delta / temperature) > rg.nextDouble();
    }

    /**
     * Updates the temperature according to an anti logarithmic decay.
     *
     * @param maxMillis
     * @param elapsedTime
     */
    private void updateTemperature() {
        temperature = Math.max(temperature * k, 1);
    }

    /**
     * Checks if I'm in a plateau. Every time delta below a certain threshold
     * (that is, the variation that occurred in the previous iteration isn't
     * significant ). i increment a counter by one and i set it to zero as soon
     * as delta is above the selected threshold. If counter reaches a threshold
     * Th (there haven't been significant variations in the last Th iterations)
     * i try to enhance the temperature. In the meanwhile, a number of changes
     * occurs: - Counter is set to zero - Temperature is enhanced randomly - The
     * currentBest is reset - Keep track of the time the reset occurs - Update
     * the number of moves and swaps performed in the tabu search algorithm.
     */
    private void checkPlateau() {
        // This delta keeps track of the variation between the actual obj function
        // and the previous one.
        double delta = (double) checkObjFun / actualObjFun;

        // If the variation isn't significant, update the counter.
        if (getTimeFromReset() > 1 && delta > 1 - DELTA_RANGE && delta < 1 + DELTA_RANGE) {
            plateauCounter++;
            // If I haven't improved much in the last 100 iterations, I turn up the temperature
            if (plateauCounter % 100 == 0) {
                temperature *= ENHANCEMENT_LIMIT * rg.nextDouble(); // Enhance temperature
                currentBest = -1; // Reset the current best
                //System.out.println("Breathing... New dive!"); // Notify the new "dive"
                lastResetTime = System.currentTimeMillis() - startTime; // Keep track of last reset time

                // If allowed, update the number of moves and swaps performed in the tabu search algorithm.
                num_moves = Math.min(num_moves + 1, MAX_MOVES);
                num_swaps = Math.min(num_swaps + 1, MAX_SWAPS);

                // Create some disturbance
                //disturbance();
            }
        } else {
            plateauCounter = 0;
        }

        checkObjFun = actualObjFun;
    }

    /**
     * Returns the milliseconds elapsed from the beginning of the algorithm.
     *
     * @return
     */
    private void updateElapsedTime() {
        this.elapsedTime = System.currentTimeMillis() - startTime;
    }

    //******METHODS FOR TABU LIST LOGIC******************
    /**
     * Moves an exam combining the logic of simulated annealing and tabu search.
     */
    private void tabuEMove() {

        int[] bestExamMove = getBestEMoveInNeighborhood();
        // If I have found a good swap, I execute it.
        if (bestExamMove[2] != tmax) {
            //System.out.println("Executing");
            executeBestEMove(bestExamMove[0], bestExamMove[1], bestExamMove[2]);
        }
    }

    /**
     * Returns the best move found exploring a neighborhood of the current
     * solution.
     *
     * @return an array of 3 int,the first one is the index of the exam in its
     * source timeslot, the second is the index of the source timeslot and the
     * third is the index of the destination timeslot.
     */
    private int[] getBestEMoveInNeighborhood() {
        int srcIndex, destIndex, examIndex, penalty, bestPenalty = Integer.MAX_VALUE;
        Exam exam;
        int[] bestIndexes = {tmax, tmax, tmax};
        for (int i = 0; i < num_moves; i++) {
            srcIndex = rg.nextInt(tmax);
            destIndex = rg.nextInt(tmax);
            if (initSolution.getTimeslot(srcIndex).isFree() || srcIndex == destIndex) {
                break;
            }
            examIndex = rg.nextInt(initSolution.getTimeslot(srcIndex).getNExams());
            exam = initSolution.getTimeslot(srcIndex).getExam(examIndex);

            penalty = CostFunction.getExamMovePenalty(exam, srcIndex, destIndex, initSolution);

            if (isAllowedEMove(exam, srcIndex, destIndex, penalty) && penalty < bestPenalty) {
                //System.out.println("Accept: " + penalty);
                bestPenalty = penalty;
                bestIndexes[0] = examIndex;
                bestIndexes[1] = srcIndex;
                bestIndexes[2] = destIndex;
            }
        }
        return bestIndexes;
    }

    /**
     * Says if moving <code>exam</code> from timeslot <code>srcIndex</code> to
     * timeslot <code>destIndex</code> is allowed. I.e. if
     * <pre>
     * <ul>
     *  <li>If the move is feasible and accepted by the logic of the simulated annealing algorithm AND</li>
     *  <li>If it is not TABU, or it is TABU but leads to the best solution ever found.</li>
     * </ul>
     * </pre> I applied De Morgan's to the original formulation, which was:
     * <code>!(!accept(penalty) || !initSolution.isFeasibleMove(ex, destIndex) || (examTabuList.moveIsTabu(ex, sourceIndex, destIndex) && (this.actualObjFun + penalty >= currentBest)))</code>
     *
     * @param exam
     * @param srcIndex
     * @param destIndex
     * @param penalty
     * @return
     */
    private boolean isAllowedEMove(Exam exam, int srcIndex, int destIndex, int penalty) {
        boolean acceptANDFeasible = accept(penalty) && initSolution.isFeasibleMove(exam, destIndex);
        boolean notTabuORBest = !examTabuList.moveIsTabu(exam, srcIndex, destIndex) || (this.actualObjFun + penalty < currentBest);
        return acceptANDFeasible && notTabuORBest;
    }

    /**
     * Executes the best move found and updates the tabu list.
     *
     * @param src
     * @param dest
     */
    private void executeBestEMove(int examIndex, int src, int dest) {
        Exam e = initSolution.getTimeslot(src).getExam(examIndex);
        initSolution.move(e, src, dest);
        examTabuList.updateTabuList(e, dest, src);
        examTabuList.updateTabuList(e, src, dest);
        checkIfBest();
    }

    /**
     * Tries to swap 2 timeslots by exploring the neighboorhood reachable by a
     * timeslot swap and performing the best move found.
     */
    private void tabuTSwap() {

        int[] bestSwapIndexes = getBestTSwapInNeighborhood();

        if (bestSwapIndexes[0] != tmax) {
            //System.out.println("Executing");
            executeBestSwap(bestSwapIndexes[0], bestSwapIndexes[1]);
        }
    }

    /**
     * Returns the best swap found in a neighborhood of the current solution.
     *
     * @return an array of 2 int, which contains the index of the 2 timeslot to
     * swap.
     */
    private int[] getBestTSwapInNeighborhood() {
        int srcIndex, destIndex, penalty, bestPenalty = Integer.MAX_VALUE;
        int[] bestIndexes = {tmax, tmax};
        for (int i = 0; i < num_moves; i++) {
            srcIndex = rg.nextInt(tmax);
            destIndex = rg.nextInt(tmax);

            penalty = CostFunction.getTimeslotSwapPenalty(srcIndex, destIndex, initSolution);

            if (isAllowedTSwap(srcIndex, destIndex, penalty) && penalty < bestPenalty) {
                //System.out.println("Accept: " + penalty);
                bestPenalty = penalty;
                bestIndexes[0] = srcIndex;
                bestIndexes[1] = destIndex;
            }
        }
        return bestIndexes;
    }

    /**
     * <pre>
     * Says if this swap of 2 timeslots is allowed. i.e. (as far as I understood) if:
     * <ul>
     *  <li> The penalty is accepted from the simulated annealing logic AND one of the
     *  following:</li>
     *      <li> The move is not tabu</li>
     *      <li>OR it is tabu but it leads to the best solution ever found.</li>
     * </ul>
     * </pre> (I applied De Morgan's law to the original formulation of the
     * condition - to make it more readable, I hope- which was:      <code>!(!acceptPenalty || (timeslotTabuList.moveIsTabu(srcIndex,
     * destIndex) && (actualObjFun + penalty >= currentBest)));</code>)
     *
     * @param srcIndex the index of the first timeslot
     * @param destIndex the index of the second timeslot
     * @param penalty the penalty associated to their swap
     * @return
     */
    private boolean isAllowedTSwap(int srcIndex, int destIndex, int penalty) {
        return accept(penalty) && (!timeslotTabuList.moveIsTabu(srcIndex, destIndex) || (actualObjFun + penalty) < currentBest);

    }

    /**
     * Executes the swap found and updates the tabu list.
     *
     * @param src
     * @param dest
     */
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
