/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.domain;

import java.util.Map;

/**
 *
 * @author Carolina Bianchi
 */
public interface CostFunction {

    static final int[] COST = {0, 16, 8, 4, 2, 1};

    /**
     * Returns the cost of a certain time slot configuration. (NB it still has
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
                    penalty += timeslots[t + i].conflictWeight(e.getConflictingExams()) * COST[i];
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
     * Returns the cost associated to a specific exam
     *
     * @param e The exam for which we want to compute the associated cost.
     * @param src The index of the timeslot associated to the selected exam
     * @param timeslots The starting set of time slots
     * @return The cost associated to the given exam
     */
    static public int getExamCost(Exam e, int src, Timeslot[] timeslots) {
        // Compute the range of time slots that could conflict with the target time slot 
        int[] rangeI = getRange(src, timeslots.length);

        int penalty = 0;
        // For each time slot within the computed range, calculate the penalty due
        // to moving the selected time slot in currentIndex position
        for (int i = rangeI[0]; i <= rangeI[1]; i++) {
            if (i != src) {
                int distance = src - i;
                penalty += timeslots[i].conflictWeight(e.getConflictingExams()) * COST[Math.abs(distance)];
            }
        }
        return penalty;
    }

    /**
     * Returns the cost associated to a specific time slot
     *
     * @param index The index associated to the selected time slot
     * @param timeslots The starting set of time slots
     * @return The cost associated to the given time slot
     */
    static public int getTimeslotCost(int index, Timeslot[] timeslots) {
        int[] range = getRange(index, timeslots.length);
        int cost = 0;

        for (int i = range[0]; i <= range[1]; i++) {
            int distance = index - i;
            for (Exam e : timeslots[index].getExams()) {
                cost += timeslots[i].conflictWeight(e.getConflictingExams()) * COST[Math.abs(distance)];
            }
        }
        return cost;
    }

    /**
     * Returns the cost of swapping an exam from time slot src to time slot
     * dest;
     *
     * @param e The exam we want to move.
     * @param src The index of the current time slot of the given exam.
     * @param dest The index of the time slot where we want to move the selected
     * exam.
     * @param timeslots The starting set of time slots.
     * @return The penalty assigned to a specific move.
     */
    static public int getExamMovePenalty(Exam e, int src, int dest, Timeslot[] timeslots) {
        int penalty = 0;
        // Get the cost of the exam in the current position
        penalty -= getExamCost(e, src, timeslots);
        // Get the cost of the exam in the new position
        penalty += getExamCost(e, dest, timeslots);
        return penalty;
    }

    /**
     * Returns the cost of swapping an exam from time slot src to time slot
     * dest;
     *
     * @param e The exam we want to move.
     * @param src The index of the current time slot of the given exam.
     * @param dest The index of the time slot where we want to move the selected
     * exam.
     * @param schedule The starting schedule.
     * @return The penalty assigned to a specific move.
     */
    static public int getExamMovePenalty(Exam e, int src, int dest, Schedule schedule) {
        return getExamMovePenalty(e, src, dest, schedule.getTimeslots());
    }

    /**
     * Returns the cost of swapping time slot at index i with the one at index
     * j;
     *
     * @param i The index of the first time slot to swap.
     * @param j The index of the second time slot to swap.
     * @param timeslots The starting set of time slots
     * @return The penalty assigned to a specific swap.
     */
    static public int getTimeslotSwapPenalty(int i, int j, Timeslot[] timeslots) {
        int penalty = 0;
        penalty += getNewTimeslotPenalty(i, j, timeslots);
        penalty += getNewTimeslotPenalty(j, i, timeslots);
        return penalty;
    }

    /**
     * Returns the cost of swapping time slot at index i with the one at index
     * j;
     *
     * @param i The index of the first time slot to swap.
     * @param j The index of the second time slot to swap.
     * @param schedule The starting schedule
     * @return The penalty assigned to a specific swap.
     */
    static public int getTimeslotSwapPenalty(int i, int j, Schedule schedule) {
        return getTimeslotSwapPenalty(i, j, schedule.getTimeslots());
    }

    /**
     * Calculates the penalty given by virtually moving the time slot that is at
     * <code>otherIndex</code> to <code>currentIndex</code>. It subtracts the
     * penalty due to the position of the time slot that was in currentIndex and
     * adds the penalty due to the fact that it is virtually replaced with the
     * time slot that was at otherIndex.
     *
     * @param currentIndex The targeted index position.
     * @param otherIndex The index associated to the time slot we want to move
     * in current index.
     * @param timeslots The starting set of time slots
     * @return The penalty due to the time slot change of index.
     */
    static public int getNewTimeslotPenalty(int currentIndex, int otherIndex, Timeslot[] timeslots) {
        // Compute the range of time slots that could conflict with the target time slot 
        int[] range = getRange(currentIndex, timeslots.length);
        int penalty = 0;
        // For each time slot within the computed range, calculate the penalty due
        // to moving the selected time slot in currentIndex position
        for (int i = range[0]; i <= range[1]; i++) {
            //  Not considering this two cases saves us from doing useless computations.  
            if (i != currentIndex && i != otherIndex) {
                int distance = currentIndex - i;
                for (Exam e : timeslots[currentIndex].getExams()) {
                    penalty -= timeslots[i].conflictWeight(e.getConflictingExams()) * COST[Math.abs(distance)];
                }
                for (Exam e : timeslots[otherIndex].getExams()) {
                    penalty += timeslots[i].conflictWeight(e.getConflictingExams()) * COST[Math.abs(distance)];
                }
            }
        }
        return penalty;
    }

    /**
     * Returns the cost of inverting the order of timeslots between
     * <code>startPoint</code> and <code>endPoint</code> in schedule
     * <code>s</code>.
     *
     * @param startPoint
     * @param endPoint
     * @param s
     * @return
     */
    static public int getInvertionTimeslotsPenalty(int startPoint, int endPoint, Schedule s) {
        return getInvertionTimeslotsPenalty(startPoint, endPoint, s.getTimeslots());
    }

    /**
     * Returns the cost of inverting the order of timeslots between
     * <code>startPoint</code> and <code>endPoint</code> in the array of
     * timeslots <code>timeslots</code>.
     *
     * @param startPoint
     * @param endPoint
     * @param timeslots
     * @return
     */
    static public int getInvertionTimeslotsPenalty(int startPoint, int endPoint, Timeslot[] timeslots) {
        int penalty = 0;
        for (int i = startPoint; i < endPoint; i++) {
            int index = timeslots[i].getTimeslotID();
            int inversionIndex = endPoint - (index - startPoint) - 1;
            if (index != inversionIndex) {
                penalty -= getTimeslotCost(index, timeslots);

                penalty += calcRepositioningPenalty(startPoint, endPoint, inversionIndex, index, timeslots);

            }
        }
        return penalty;
    }

    /**
     * Calculates the total penalty generated in case there is an inversion in
     * <code>timeslots</code> between the points <code>startPoint</code> and
     * <code>endPoint</code>. It consider the penalty of the timeslot and it
     * virtually changed the order to simulate the inverted array of timeslots.
     *
     * @param startPoint
     * @param endPoint
     * @param inversionIndex
     * @param index
     * @param timeslots
     * @return
     */
    static int calcRepositioningPenalty(int startPoint, int endPoint, int inversionIndex, int index, Timeslot[] timeslots) {
        int penalty = 0;
        int[] range = getRange(inversionIndex, timeslots.length);
        for (int j = range[0]; j <= range[1]; j++) {
            if (inversionIndex != j) {

                int distance = inversionIndex - j;
                Timeslot t = timeslots[j];
                if (j >= startPoint && j < endPoint) {
                    t = timeslots[endPoint - (j - startPoint) - 1];
                }
                for (Exam e : timeslots[index].getExams()) {
                    penalty += t.conflictWeight(e.getConflictingExams()) * COST[Math.abs(distance)];
                }
            }
        }
        return penalty;
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
        int[] range = {Math.max(index - 5, 0), Math.min(index + 5, tmax - 1)};
        return range;
    }

    /**
     * Returns the penalty generated by timeslots <code>t1</code> and
     * <code>t2</code> assuming their distance =1.
     *
     * @param t1
     * @param t2
     * @return
     */
    public static int getMutualTimeslotsCost(Timeslot t1, Timeslot t2) {
        Timeslot[] timeslots = {t1, t2};
        return getCost(timeslots);
    }

    /**
     * Calculates the cost of a given set of timeslot, provided a map that maps
     * a pair of timeslot with their cost calculated when their distance =1
     * (e.g. exploiting the function "getMutualTimeslotCost").
     *
     * @param timeslots
     * @param costMap
     * @return
     */
    public static int getCost(Timeslot[] timeslots, Map<String, Integer> costMap) {
        int[] divisor = {0,1, 2, 4, 8, 16};
        int penalty=0;
        String coupleHash;
        for (int i = 1; i<=5; i++) {
            for(int j = 0; j<timeslots.length-i; j++){
                coupleHash = Schedule.getTimeslotPairHash(timeslots[j], timeslots[j+i]);
                penalty+=costMap.get(coupleHash)/divisor[i];
            }
        }
        return penalty;
    }
}
