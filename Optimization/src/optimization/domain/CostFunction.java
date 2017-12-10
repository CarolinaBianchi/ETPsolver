/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.domain;


/**
 *
 * @author Carolina Bianchi
 */
public interface CostFunction {

    static final int[] COST = {0, 16, 8, 4, 2, 1};

    /**
     * Returns the cost of a certain timeslot confliguration. (NB it still has
     * to be weighted by the total number of students).
     *
     * @param timeslots
     * @return
     */
    static public int getCost(Timeslot[] timeslots) {

        int penalty = 0;
        for (int i = 1; i <= 5; i++) {
            for (int t = 0; t < timeslots.length-i; t++) {
                for (Exam e : timeslots[t].getExams()) {
                    penalty += timeslots[t + i].conflictWeight(e.getConflictingExams2()) * COST[i];
                }
            }
        }
        return penalty;
    }
    
    /**
     * Returns the cost of a certain schedule. (NB it still has
     * to be weighted by the total number of students).
     *
     * @param schedule the schedule
     * @return
     */
    static public int getCost(Schedule schedule) {
        return getCost(schedule.getTimeslots());
    }

}
