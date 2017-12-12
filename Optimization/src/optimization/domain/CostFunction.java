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
            for (int t = 0; t < timeslots.length - i; t++) {
                for (Exam e : timeslots[t].getExams()) {
                    penalty += timeslots[t + i].conflictWeight(e.getConflictingExams2()) * COST[i];
                }
            }
        }
        return penalty;
    }

    /**
     * Returns the cost of a certain schedule. (NB it still has to be weighted
     * by the total number of students).
     *
     * @param schedule the schedule
     * @return
     */
    static public int getCost(Schedule schedule) {
        return getCost(schedule.getTimeslots());
    }

    /**
     * WRONG! .... returns the cost of swapping timeslots at index[0] with the one at
     * index[1]; 
     *
     * @param indexes
     * @param timeslots
     * @return
     */
    static public int getCost(int[] indexes, Timeslot[] timeslots) {
        reorder(indexes);
        int penalty = 0;
        penalty += scanRange(indexes[0], indexes[1], timeslots);
        penalty += scanRange(indexes[1], indexes[0], timeslots);
        penalty += 2 * mutualPenalty(indexes, timeslots);
        return penalty;
    }

    /**
     * WRONG! Calculates the penalty given by virtually moving the timeslot that is at
     * <code>otherIndex</code> to <code>currentIndex</code>. It subtracts the
     * penalty due to the position of the timeslot that was @ currentIndex and
     * adds the penalty due to the fact that it is virtually replaced with the
     * timeslot that was at otherIndex.
     *
     * @param currentIndex
     * @param otherIndex
     * @param timeslots
     * @return
     */
    static public int scanRange(int currentIndex, int otherIndex, Timeslot[] timeslots) {
        int[] range = getRange(currentIndex, timeslots.length);
        int penalty = 0;
        for (int i = range[0]; i < range[1]; i++) {
            int distance = currentIndex - i;
            for (Exam e : timeslots[currentIndex].getExams()) {
                penalty -= timeslots[i].conflictWeight(e.getConflictingExams2()) * COST[Math.abs(distance)];
            }
            for (Exam e : timeslots[otherIndex].getExams()) {
                penalty += timeslots[i].conflictWeight(e.getConflictingExams2()) * COST[Math.abs(distance)];
            }
        }
        return penalty;
    }

    /**
     * Returns the TRUE cost of swapping 2 timeslots. Prints the comparison with
     * the WRONG cost calculated with my wrong method.
     *
     * @param indexes
     * @param schedule
     * @return
     */
    static public int getCost(int[] indexes, Schedule schedule) {
        //return getCost(indexes, schedule.getTimeslots());
        int wrongPenalty = getCost(indexes, schedule.getTimeslots());
        int oldCost = schedule.getCost();
        schedule.swapTimeslots(indexes[0], indexes[1]);
        int newCost = schedule.getCost();
        System.out.println(indexes[0] + "-" + indexes[1] + "\t True penalty" + (newCost - oldCost) + "\t vs S" + wrongPenalty + "\t" + mutualPenalty(indexes, schedule.getTimeslots()));
        return newCost - oldCost;
    }

    /**
     * Orders the indexes so that the first one is less or equal than the second
     * one.
     *
     * @param indexes
     */
    static void reorder(int[] indexes) {
        int tmp;
        if (indexes[0] > indexes[1]) {
            tmp = indexes[0];
            indexes[0] = indexes[1];
            indexes[1] = tmp;
        }

    }

    /**
     * Returns the range of indexes that are 5 timeslots before and after
     * <code>index</code>. It takes care of the timeslots bounds.
     *
     * @param index
     * @param tmax
     * @return
     */
    static int[] getRange(int index, int tmax) {
        int[] range = {Math.max(index - 5, 0), Math.min(index + 5, tmax)};
        return range;
    }

    /**
     * Calculates the mutual penalty between 2 timeslots.
     *
     * @param indexes
     * @param timeslots
     * @return
     */
    public static int mutualPenalty(int[] indexes, Timeslot[] timeslots) {
        int penalty = 0;
        int distance = Math.abs(indexes[0] - indexes[1]);
        if (distance > 5) {
            return 0;
        }
        for (Exam e : timeslots[indexes[0]].getExams()) {
            penalty += timeslots[indexes[1]].conflictWeight(e.getConflictingExams2()) * COST[distance];
        }
        return penalty;
    }

}
