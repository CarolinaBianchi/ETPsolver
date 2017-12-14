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
     * Returns the cost associated to a specific exam
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
            if(i!=src) {   
                int distance = src - i;
                penalty += timeslots[i].conflictWeight(e.getConflictingExams2()) * COST[Math.abs(distance)];
            }
        }
        return penalty;
    }

    /**
     * Returns the cost associated to a specific time slot
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
                cost += timeslots[i].conflictWeight(e.getConflictingExams2()) * COST[Math.abs(distance)];
            }
        }
        return cost;
    }
    
    
    /**
     * Returns the cost of swapping an exam from time slot src to time slot dest; 
     *
     * @param e The exam we want to move.
     * @param src The index of the current time slot of the given exam.
     * @param dest The index of the time slot where we want to move the selected exam.
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
     * Returns the cost of swapping an exam from time slot src to time slot dest; 
     *
     * @param e The exam we want to move.
     * @param src The index of the current time slot of the given exam.
     * @param dest The index of the time slot where we want to move the selected exam.
     * @param schedule The starting schedule.
     * @return The penalty assigned to a specific move. 
     */
    static public int getExamMovePenalty(Exam e, int src, int dest, Schedule schedule) {
        return getExamMovePenalty(e, src, dest, schedule.getTimeslots());
    }

    /**
     * Returns the cost of swapping time slot at index i with the one at index j; 
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
     * Returns the cost of swapping time slot at index i with the one at index j; 
     *
     * @param i The index of the first time slot to swap.
     * @param j The index of the second time slot to swap.
     * @param schedule The starting schedule 
     * @return The penalty assigned to a specific swap. 
     */
    static public int getTimeslotSwapPenalty(int i, int j, Schedule schedule){
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
     * @param otherIndex The index associated to the time slot we want to move in current index.
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
            if(i!=currentIndex && i!=otherIndex) {
                int distance = currentIndex - i;
                for (Exam e : timeslots[currentIndex].getExams()) {
                    penalty -= timeslots[i].conflictWeight(e.getConflictingExams2()) * COST[Math.abs(distance)];
                }
                for (Exam e : timeslots[otherIndex].getExams()) {
                    penalty += timeslots[i].conflictWeight(e.getConflictingExams2()) * COST[Math.abs(distance)];
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
        int[] range = {Math.max(index - 5, 0), Math.min(index + 5, tmax-1)};
        return range;
    }
    
    
    
    
    
    
    
    /* ******* OLD METHOD'S GRAVEYARD ********* */
    
    /**
     * Calculates the mutual penalty between 2 timeslots.
     *
     * @param indexes
     * @param timeslots
     * @return
    
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
    } */
    
    /**
     * Orders the indexes so that the first one is less or equal than the second
     * one.
     *
     * @param indexes
     
    static void reorder(int[] indexes) {
        int tmp;
        if (indexes[0] > indexes[1]) {
            tmp = indexes[0];
            indexes[0] = indexes[1];
            indexes[1] = tmp;
        }

    }*/
}
