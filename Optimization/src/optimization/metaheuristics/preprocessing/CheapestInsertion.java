/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.metaheuristics.preprocessing;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import optimization.Optimizer;
import optimization.domain.CostFunction;
import optimization.domain.Schedule;
import optimization.domain.Timeslot;

/**
 *
 * @author Carolina Bianchi
 */
public class CheapestInsertion extends PreprocessingMetaheuristic {

    private Map<String, Integer> costMap;
    private Random rg;

    public CheapestInsertion(Optimizer optimizer, Schedule initSolution, int MAX_MILLIS) {
        super(optimizer, initSolution, MAX_MILLIS);
        rg = new Random(System.currentTimeMillis());
        computeCostMap();
    }

    @Override
    void preprocess() {
        optimizeTimeslotOrder();
        trySomeSwap();
        mySolution = initSolution;
    }

    /**
     * Create the best possible timeslot order without moving any exam
     * singularly.
     */
    private void optimizeTimeslotOrder() {
        initSolution.setTimeslots(optimizeTimeslotOrder(initSolution.getTimeslots()));
        initSolution.computeCost(costMap);
    }

    /**
     * Create the best possible timeslot order without moving any exam
     * singularly.
     *
     * @param timeslots
     * @return
     */
    private Timeslot[] optimizeTimeslotOrder(Timeslot[] timeslots) {
        int length = timeslots.length;

        // Base of the recursive method
        if (length == 1) {
            return timeslots;
        }

        // Generate a set of timeslots of size length-1 and another one of size length
        Timeslot[] optimizedTimeslots = new Timeslot[length - 1];

        System.arraycopy(timeslots, 0, optimizedTimeslots, 0, length - 1);

        // Recursively call optimizeTimeslotOrder to obtain the best possible 
        // set of timeslots of size equals to (length-1)
        optimizedTimeslots = optimizeTimeslotOrder(optimizedTimeslots);

        timeslots = getOptimalTimeslotPlacement(optimizedTimeslots, timeslots[length - 1]);

        return timeslots;
    }

    private Timeslot[] getOptimalTimeslotPlacement(Timeslot[] timeslots, Timeslot toAdd) {
        int added = 0;
        int best = -1;
        int length = timeslots.length + 1;
        Timeslot[] newTimeslots = new Timeslot[length];
        Timeslot[] optimizedTimeslots = new Timeslot[length];
        Timeslot auxToAdd;

        // The first loop is used to determine the position of toAdd in timeslots
        for (int i = 0; i < length; i++) {
            auxToAdd = toAdd.clone();

            // The inner loop builds the set optimizedTimeslots, placing toAdd in
            // i position. Variable added is used to be able to correctly visit 
            // the set timeslots
            for (int j = 0; j < length; j++) {
                if (i == j) {
                    auxToAdd.setTimeslotID(j);
                    newTimeslots[j] = auxToAdd;
                    added = 1;
                } else {
                    newTimeslots[j] = timeslots[j - added].clone();
                    newTimeslots[j].setTimeslotID(j);
                }
            }
            // Check if the cost of optimizedTimeslots is the best found so far;
            int currentCost = CostFunction.getCost(newTimeslots, costMap);
            if (currentCost < best || best < 0) {
                optimizedTimeslots = newTimeslots.clone();
                best = currentCost;
            }
            added = 0;
        }
        return optimizedTimeslots;
    }

    /**
     * Tries to swap the timeslots to get an even better solution.
     */
    private void trySomeSwap() {
        int tj, tk, tmax = this.initSolution.getTmax();
        for (int i = 0; i < 1000; i++) {
            tj = rg.nextInt(tmax);
            tk = rg.nextInt(tmax);
            if (CostFunction.getTimeslotSwapPenalty(tj, tk, initSolution) < 0) {
                initSolution.swapTimeslots(tj, tk);
            }
        }
        initSolution.computeCost();
    }

    /**
     * Creates a cost map that maps a pair of timeslots to their cost
     * (calculated as if they were adjacent in the schedule).
     *
     * @param timeslots
     */
    private void computeCostMap() {
        this.costMap = new HashMap<>();
        int mutualCost;
        Timeslot[] timeslots = initSolution.getTimeslots();
        for (Timeslot t1 : timeslots) {
            for (Timeslot t2 : timeslots) {
                mutualCost = CostFunction.getMutualTimeslotsCost(t1, t2);
                costMap.put(Schedule.getTimeslotPairHash(t1, t2, false), mutualCost);
                costMap.put(Schedule.getTimeslotPairHash(t1, t2, true), mutualCost);
            }
        }
    }

}
