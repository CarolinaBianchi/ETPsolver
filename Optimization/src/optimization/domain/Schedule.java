/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Class that represents a Schedule.
 *
 * @author Carolina Bianchi
 */
public class Schedule implements Cloneable, Comparable<Schedule> {

    private static int MAX_SWAP_TRIES = 10;
    private Timeslot[] timeslots;
    private int cost;
    private int numStudents;
    private boolean cheapestInsPreproc = false; // Says if this schedule has undergone the cheapest insertion preprocessing.

    public Schedule(int tmax, int numStudents) {
        timeslots = new Timeslot[tmax];
        this.numStudents = numStudents;
        initTimeslots();
    }

    public void setTimeslots(Timeslot[] ts) {
        this.timeslots = ts;
    }

    private void initTimeslots() {
        for (int i = 0; i < timeslots.length; i++) {
            timeslots[i] = new Timeslot(i);
        }
    }

    public Timeslot[] getTimeslots() {
        return this.timeslots;
    }

    public int getTmax() {
        return this.timeslots.length;
    }

    public int getNumberStudents() {
        return this.numStudents;
    }

    /**
     * Sets the value of the objective function of this schedule.
     *
     * @param cost
     */
    public void setCost(int cost) {
        this.cost = cost;
    }

    /**
     * Updates the cost of this schedule.
     *
     * @param penalty
     */
    public void updateCost(int penalty) {
        this.cost += penalty;
    }

    /**
     * Gets the value of the objective function of this schedule.
     *
     * @return
     */
    public int getCost() {
        return this.cost;
    }

    public void setPreprocessed(boolean preprocessed) {
        this.cheapestInsPreproc = preprocessed;
    }

    public boolean isPreprocessed() {
        return this.cheapestInsPreproc;
    }

    public int getTotalCollisions() {
        int tot = 0;
        for (Timeslot t : timeslots) {
            tot += t.getCollisions();
        }

        return tot;
    }

    public Timeslot getTimeslot(int i) {
        return timeslots[i];
    }

    public Timeslot getRandomTimeslot() {
        Random rnd = new Random();
        return timeslots[rnd.nextInt(timeslots.length)];
    }

    /**
     * Returns a random Timeslot up to a certain bound.
     *
     * @param bound
     * @return
     */
    public Timeslot getRandomTimeslot(int bound) {
        Random rnd = new Random();
        return timeslots[rnd.nextInt(bound)];
    }

    /**
     * Returns a random Timeslot between a specific given bucket.
     *
     * @param bucket The set of time slots to consider
     * @return A randomly chosen time slot between the available ones.
     */
    public Timeslot getRandomTimeslot(List<Integer> bucket) {
        Random rnd = new Random();
        return timeslots[bucket.get(rnd.nextInt(bucket.size()))];
    }

    public Timeslot getTimeslotWithExams() {
        Random rnd = new Random();
        Timeslot t;
        do {
            t = timeslots[rnd.nextInt(timeslots.length)];
        } while (t.isFree());
        return t;
    }

    /**
     * Returns the number of timeslots currently in this schedule.
     *
     * @return
     */
    public int getNExams() {
        int counter = 0;
        for (Timeslot t : timeslots) {
            counter += t.getExams().size();
        }
        return counter;
    }

    void clear() {
        initTimeslots();
    }

    /**
     * Moves an exam from a source time slot to a destination.
     *
     * @param ex
     * @param source
     * @param dest
     * @return
     */
    public boolean move(Exam ex, Timeslot source, Timeslot dest) {
        if (dest.isCompatible(ex)) {
            int penalty = CostFunction.getExamMovePenalty(ex, source.getTimeslotID(), dest.getTimeslotID(), timeslots);
            updateCost(penalty);
            source.removeExam(ex);
            dest.addExam(ex);
            return true;
        }
        return false;
    }

    /**
     * Moves an exam from a source time slot to a destination.
     *
     * @param ex
     * @param sourceI
     * @param destI
     * @return
     */
    public boolean move(Exam ex, int sourceI, int destI) {
        return move(ex, timeslots[sourceI], timeslots[destI]);
    }

    /**
     * Checks the feasibility of swapping Exam ex1 (which is in timeslot t1)
     * with exam ex2 which is in timeslot t2.
     *
     * @param t1
     * @param ex1
     * @param t2
     * @param ex2
     * @return
     */
    public boolean checkFeasibleSwap(Timeslot t1, Exam ex1, Timeslot t2, Exam ex2) {
        return checkSwap(t1, ex1, ex2) && checkSwap(t2, ex2, ex1);
    }

    /**
     * Checks the feasibility of swapping Exam ex1 (which is in timeslot t1)
     * whith Exam ex2.
     *
     * @param t1
     * @param ex1
     * @param ex2
     * @return
     */
    private boolean checkSwap(Timeslot t1, Exam ex1, Exam ex2) {
        t1.removeExam(ex1);
        boolean compatible = t1.isCompatible(ex2);
        t1.addExam(ex1);
        return compatible;
    }

    /**
     * Swaps 2 exams.
     *
     * @param t1 source timeslot of exam ex1
     * @param ex1
     * @param t2 source timeslot of exam ex2
     * @param ex2
     */
    public void swap(Timeslot t1, Exam ex1, Timeslot t2, Exam ex2) {
        move(ex1, t1, t2);
        move(ex2, t2, t1);
    }

    /**
     * Tries to place an exam in a random Timeslot.
     *
     * @param toBePlaced The exam we want to schedule.
     * @return true if the exam was placed, false otherwise.
     */
    public boolean randomPlacement(Exam toBePlaced) {
        Timeslot destination;

        destination = getRandomTimeslot();
        if (destination.isCompatible(toBePlaced)) {
            destination.addExam(toBePlaced);
            return true;
        }
        return false;
    }

    /**
     * Tries to place an exam in a random Timeslot considering a window of
     * timeslots of width <code>width</code>.
     *
     * @param toBePlaced the exam that has to be placed
     * @param width the width of the window
     * @return true if the exam was successfully placed, false otherwise.
     */
    public boolean randomPlacement(Exam toBePlaced, int width) {
        Timeslot destination = getRandomTimeslot(width);
        if (destination.isCompatible(toBePlaced)) {
            destination.addExam(toBePlaced);
            return true;
        }
        return false;
    }

    /**
     * Tries to place an exam in a random Timeslot considering a specific
     * "bucket" (that is, a given list of time slots).
     *
     * @param toBePlaced the exam that has to be placed
     * @param bucket the list of time slots to consider.
     * @return true if the exam was successfully placed, false otherwise.
     */
    public boolean randomPlacement(Exam toBePlaced, List<Integer> bucket) {
        Timeslot destination = getRandomTimeslot(bucket);
        if (destination.isCompatible(toBePlaced)) {
            destination.addExam(toBePlaced);
            return true;
        }
        return false;
    }

    /**
     * Force an exam to be placed in a random schedule, even if it generates a
     * collision.
     *
     * @param toBePlaced The exam we want to schedule.
     */
    public void forceRandomPlacement(Exam toBePlaced) {
        getRandomTimeslot().forceExam(toBePlaced);
    }

    /**
     * Tries to move 1 exam from a Timeslot to another one.
     *
     * @return true if the exam was successfully moved, false otherwise.
     *
     */
    public boolean randomMove() {
        Timeslot tj = getTimeslotWithExams();
        Timeslot tk = getRandomTimeslot();
        Exam ex = tj.getRandomExam();

        return move(ex, tj, tk);
    }

    /**
     * Moves 2 exams inside a window of timeslots with a certain
     * <code>width</code>. (Used in WindowInitialized)
     *
     * @param width the width of the window
     * @return true if the exam was successfully moved, false otherwise.
     */
    public boolean randomMove(int width) {
        Timeslot tj = getTimeslotWithExams();
        Timeslot tk = getRandomTimeslot(width);
        Exam ex = tj.getRandomExam();
        return move(ex, tj, tk);
    }

    /**
     * Moves 2 exams inside a list of buckets. (Used in BucketInitializer)
     *
     * @param bucket The list of time slots considered
     * @return true if the exam was successfully moved, false otherwise.
     */
    public boolean randomMove(List<Integer> bucket) {
        Timeslot tj = getTimeslotWithExams();
        Timeslot tk = getRandomTimeslot(bucket);

        Exam ex = tj.getRandomExam();
        return move(ex, tj, tk);
    }

    /**
     * Performs a random swap between 2 exams.
     *
     * @return true if the swapping attempt succeeded, false otherwise.
     *
     *
     */
    public boolean randomSwap() {
        Timeslot tj, tk;
        Exam ex1, ex2;

        tj = getTimeslotWithExams();
        tk = getTimeslotWithExams();
        ex1 = tj.getRandomExam();
        ex2 = tk.getRandomExam();
        if (checkFeasibleSwap(tj, ex1, tk, ex2)) {
            swap(tj, ex1, tk, ex2);
            return true;
        }

        return false;
    }

    /**
     * Tries to place an exam in a free Time slot.
     *
     * @param toBePlaced The exam we want to schedule.
     * @return true if the exam was placed, false otherwise.
     */
    public boolean freePlacement(Exam toBePlaced) {

        // For each available time slot, check if it has no exams scheduled in 
        // it yet. If so, schedule the given exam in the first free slot found.
        for (Timeslot destination : timeslots) {
            if (destination.isFree()) {
                destination.addExam(toBePlaced);
                return true;
            }
        }

        return false;
    }

    /**
     * Count the number of free time slots available;
     *
     * @return The number of free time slots available.
     */
    public int freeTimeslotAvailability() {
        int count = 0;

        // For each available time slot, check if it has no exams scheduled in 
        // it yet. If so, increment the counter.
        for (Timeslot destination : timeslots) {
            if (destination.isFree()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Returns true if the Exam <code>exam</code> can be moved to the Timeslot
     *
     * @<code>destIndex</code>.
     *
     * @param exam
     * @param destIndex
     * @return
     */
    public boolean isFeasibleMove(Exam exam, int destIndex) {
        return this.timeslots[destIndex].isCompatible(exam);
    }

    /**
     * Swaps timeslot i with timeslot j.
     *
     * @param i
     * @param j
     */
    public void swapTimeslots(int i, int j) {
        int penalty = CostFunction.getTimeslotSwapPenalty(i, j, timeslots);

        Timeslot ti = getTimeslot(i);
        Timeslot tj = getTimeslot(j);

        timeslots[i] = tj;
        timeslots[j] = ti;

        // Since we swapped two timeslots, we need to update the position written
        // in their instaces.
        timeslots[i].setTimeslotID(i);
        timeslots[j].setTimeslotID(j);
        updateCost(penalty);
    }

    /**
     * Returns a String used to identify a couple of timeslots.
     *
     * @param t1
     * @param t2
     * @param reverse if they need to be considered in the opposite order
     * @return
     */
    public static String getTimeslotPairHash(Timeslot t1, Timeslot t2, boolean reverse) {
        if (reverse) {
            return t2.hashCode() + "-" + t1.hashCode();
        }
        return t1.hashCode() + "-" + t2.hashCode();
    }

    /**
     * Returns a String used to identify a couple of timeslot in the order
     * t1->t2.
     *
     * @param t1
     * @param t2
     * @return
     */
    static String getTimeslotPairHash(Timeslot t1, Timeslot t2) {
        return getTimeslotPairHash(t1, t2, false);
    }

    /**
     * Returns a clone of this schedule.
     *
     * @return
     */
    @Override
    public Schedule clone() {
        int tmax = this.getTmax();
        Schedule s = new Schedule(tmax, numStudents);
        s.cheapestInsPreproc=this.cheapestInsPreproc;
        s.setCost(this.cost);
        Timeslot[] tclone = new Timeslot[tmax];
        for (int i = 0; i < tmax; i++) {
            tclone[i] = this.timeslots[i].clone();
        }
        s.setTimeslots(tclone);
        return s;
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < timeslots.length; i++) {
            s += "Timeslot " + i + timeslots[i].toString() + "\n";
        }
        return s;
    }

    /**
     * Schedules with lower value of the objective function are put firsts
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Schedule o) {
        return this.getCost() - o.getCost();
    }

    /**
     * Tries to swap two exams of two different timeslots
     *
     * @return if the swap has happend
     */
    public boolean mutateExams() {
        boolean swapped;
        int iterNum = 0;
        do {
            swapped = randomSwapWithPenalty();
            iterNum++;

        } while (!swapped && iterNum != MAX_SWAP_TRIES);

        return swapped;

    }

    private boolean randomSwapWithPenalty() {
        Timeslot tj, tk;
        Exam ex1, ex2;

        tj = getTimeslotWithExams();
        tk = getTimeslotWithExams();
        ex1 = tj.getRandomExam();
        ex2 = tk.getRandomExam();

        if (checkFeasibleSwap(tj, ex1, tk, ex2)) {
            int penalty = getSwapCost(tj.getTimeslotID(), ex1, tk.getTimeslotID(), ex2);
            if (penalty < 0) {
                swap(tj, ex1, tk, ex2);
                return true;
            }

        }

        return false;
    }

    /**
     * Calculates the cost of swapping <code>ex1</code> with <code>ex2</code>
     *
     * @param tjId
     * @param ex1
     * @param tkId
     * @param ex2
     * @return
     */
    private int getSwapCost(int tjId, Exam ex1, int tkId, Exam ex2) {
        return CostFunction.getExamMovePenalty(ex1, tjId, tkId, timeslots) + CostFunction.getExamMovePenalty(ex2, tkId, tjId, timeslots);
    }

    /**
     * Takes two random timeslots and swaps their position if the generated
     * total penalty decreases.
     *
     */
    public void tryMutateTimeslots() {

        Timeslot tk = getRandomTimeslot();
        Timeslot tj = getRandomTimeslot();
        int penalty = CostFunction.getTimeslotSwapPenalty(tk.getTimeslotID(), tj.getTimeslotID(), timeslots);
        if (penalty < 0) {
            mutateTimeslots(tj, tk);
            updateCost(penalty);
        }
    }

    /**
     * Swaps the postion of two timeslots.
     *
     * @param tj
     * @param tk
     */
    private void mutateTimeslots(Timeslot tj, Timeslot tk) {
        int tjId = tj.getTimeslotID();
        int tkId = tk.getTimeslotID();
        timeslots[tkId] = tj;
        timeslots[tjId] = tk;
        timeslots[tkId].setTimeslotID(tkId);
        timeslots[tjId].setTimeslotID(tjId);
    }

    /**
     * Given two cut-points, if the cost function decreases, it invert the order
     * of the timeslots in the interval
     * [<code>cutPoints[0]</code>;<code>cutPoints[1]</code>)
     *
     * @param cutPoints
     */
    public void tryInvertTimeslots(int[] cutPoints) {
        int penalty = CostFunction.getInvertionTimeslotsPenalty(cutPoints[0], cutPoints[1], timeslots);
        if (penalty < 0) {
            invertTimeslots(cutPoints[0], cutPoints[1]);
            updateCost(penalty);
        }

    }

    /**
     * Remove all the exams in the timeslots in the interval
     * [<code>startPoint</code>;<code>endPoint</code>). Then it assigns those
     * exams to the timeslots in the reverse order.
     *
     * @param startPoint
     * @param endPoint
     * @param penalty
     */
    private void invertTimeslots(int startPoint, int endPoint) {
        int length = endPoint - startPoint; //3
        Timeslot[] tmpSlots = this.createTmpSlots(length);

        for (int i = 0; i < length; i++) {
            tmpSlots[length - 1 - i].addExams(timeslots[startPoint + i].getExams());
            timeslots[startPoint + i].clean();
        }

        for (int i = 0; i < length; i++) {
            timeslots[startPoint + i].addExams(tmpSlots[i].getExams());
        }

    }

    /**
     * Creates a temporary array of timeslots
     *
     * @param length
     * @return
     */
    private Timeslot[] createTmpSlots(int length) {
        Timeslot[] tmpSlots = new Timeslot[length];

        for (int i = 0; i < tmpSlots.length; i++) {
            tmpSlots[i] = new Timeslot(i);
        }

        return tmpSlots;
    }

    /**
     * Takes a random exam among <code>parent2Exams</code> and, if the cost
     * function decreases, it does the crossover.
     *
     * @param parent2Exams
     */
    public void tryCrossover(List<Exam> parent2Exams) {
        // If there are no exams
        if (parent2Exams.isEmpty()) {
            return;
        }

        Exam candidate = parent2Exams.get(new Random().nextInt(parent2Exams.size()));
        for (Timeslot t : timeslots) {
            if (t.contains(candidate)) {
                int penalty = calcCrossoverPenalty(parent2Exams, t);
                if (penalty < 0) {
                    doCrossover(parent2Exams, t);
                    updateCost(penalty);
                }
                return;
            }
        }

    }

    /**
     * Tries to insert the exams of <code>parent2Exams</code> into timeslot
     * <code>t</code>. If at least one exams has been moved, it fix the
     * schedule, removing the exam from its previous position.
     *
     * @param parent2Exams
     * @param t
     */
    private void doCrossover(List<Exam> parent2Exams, Timeslot t) {
        List<Exam> positionedExams = t.tryInsertExams(parent2Exams);
        if (!positionedExams.isEmpty()) {
            fixSchedule(t, positionedExams);
        }
    }

    /**
     * For eaxh exam in <code>parent2Exams</code>, if they are not contained in
     * <code>dest</code> it checks if it is compatible to move there the exam;
     * if so it xcalculates the penalty related to the movement.
     *
     * @param parent2Exams
     * @param dest
     * @return
     */
    private int calcCrossoverPenalty(List<Exam> parent2Exams, Timeslot dest) {
        int penalty = 0;
        int destTId = dest.getTimeslotID();
        for (Exam e : parent2Exams) {
            Timeslot src = getTimeslotByExam(e);
            if (src != dest && dest.isCompatible(e)) {
                penalty += CostFunction.getExamMovePenalty(e, src.getTimeslotID(), destTId, this);
            }
        }
        return penalty;
    }

    /**
     * Retrieves the timeslot which contains exam <code>e</code>
     *
     * @param e
     * @return
     */
    private Timeslot getTimeslotByExam(Exam e) {
        for (Timeslot t : timeslots) {
            if (t.contains(e)) {
                return t;
            }
        }
        return null; // never
    }

    /**
     * Removes all the exams in <code>positionedExams</code> from their previous
     * position
     *
     * @param destTimeslot
     * @param positionedExams
     */
    private void fixSchedule(Timeslot destTimeslot, List<Exam> positionedExams) {
        for (Exam e : positionedExams) {
            for (Timeslot t : timeslots) {
                if (t.contains(e) && t != destTimeslot) {
                    t.removeExam(e);
                }

            }
        }
    }

    /**
     * Computes the cost of this schedule.
     */
    public void computeCost() {
        this.cost = CostFunction.getCost(timeslots);
    }
    
    /**
     * Computes the cost of this schedule.
     */
    public void computeCost(Map<String, Integer> costMap) {
        this.cost = CostFunction.getCost(timeslots, costMap);
    }

    /**
     * Takes a random exam in a random timeslots and look for its conflicting
     * exams. If there are conflicting exams, it selects a number of exams equal
     * to the minimum between <code>nSwaps</code> and the number of conflicting
     * exams. Builds swappingExams, an HashMap in which for each exam correspond
     * its timeslot, and it finds out if it's possible to do the swap.
     *
     * @param nSwaps
     */
    public void multipleSwaps(int nSwaps) {
        Timeslot candidateTimeslot = getRandomTimeslot();
        Exam candidate = candidateTimeslot.getRandomExam();
        Set<Integer> conflictingExams = candidate.getConflictingExams();
        if (conflictingExams.isEmpty()) {
            return;
        }
        nSwaps = Math.min(nSwaps, conflictingExams.size());
        int[] swappingExamsIDs = getSwappingExamsIDs(conflictingExams, nSwaps);
        Map<Exam, Timeslot> swappingExams = createSwappingExams(swappingExamsIDs, candidate, candidateTimeslot);
        if (checkFeasibleSwaps(swappingExams)) {
            doMultipleSwaps(swappingExams);
        }

    }

    /**
     * Retrieves <cide>nSwaps</code> IDs of exams among the ones in conflictinf
     * exams.
     *
     * @param conflictingExams
     * @param nSwaps
     * @return
     */
    private int[] getSwappingExamsIDs(Set<Integer> conflictingExams, int nSwaps) {
        Integer[] examPositions = getRandomPositions(nSwaps, conflictingExams.size());
        int[] examsId = new int[nSwaps];
        int currentPosition = 0;
        int counter = 0;
        for (Integer eId : conflictingExams) {
            for (Integer ePosition : examPositions) {
                if (ePosition == currentPosition) {
                    examsId[counter] = eId;
                    counter++;
                    break;
                }
            }
            currentPosition++;
        }
        return examsId;
    }

    /**
     * Retrives an array of <code>nSwaps</code> differenr random positions
     * between 0 and <code>length</code>
     *
     * @param nSwaps
     * @param length
     * @return
     */
    private Integer[] getRandomPositions(int nSwaps, int length) {
        Random rnd = new Random();
        Integer[] randomPositions = new Integer[nSwaps];
        for (int i = 0; i < nSwaps; i++) {
            randomPositions[i] = rnd.nextInt(length);
            if (i != 0) {
                checkPosition(randomPositions, i, length);
            }

        }
        return randomPositions;
    }

    /**
     * If the found position is already saved in <code>randomPosition</code> it
     * changes it with a new different one.
     *
     * @param randomPositions
     * @param index
     * @param length
     */
    private void checkPosition(Integer[] randomPositions, int index, int length) {
        Random rnd = new Random();
        for (int j = 0; j < index; j++) {
            if (index != j && Objects.equals(randomPositions[index], randomPositions[j])) {
                randomPositions[index] = rnd.nextInt(length);
                checkPosition(randomPositions, index, length);
            }
        }
    }

    /**
     * Returns a Map in which for each exam there is the timeslot in which it is
     * scheduled.
     *
     * @param eID
     * @param candidate
     * @param candidateTimeslot
     * @return
     */
    private Map<Exam, Timeslot> createSwappingExams(int[] eID, Exam candidate, Timeslot candidateTimeslot) {
        Map<Exam, Timeslot> swappingExams = new HashMap<>();
        swappingExams.put(candidate, candidateTimeslot);
        for (int i = 0; i < eID.length; i++) {
            Timeslot t = getTimeslotByEID(eID[i]);
            Exam e = t.getExamByID(eID[i]);
            swappingExams.put(e, t);
        }
        return swappingExams;
    }

    /**
     * Retrieves the timeslot in which there is the exam with ID
     * <code>eId</code>
     *
     * @param eId
     * @return
     */
    private Timeslot getTimeslotByEID(int eId) {
        for (Timeslot t : timeslots) {
            if (t.contains(eId)) {
                return t;
            }
        }
        return null;

    }

    /**
     * Returns if multiple swaps are feasible and the improve the cost function.
     *
     * @param swappingExams
     * @return
     */
    private boolean checkFeasibleSwaps(Map<Exam, Timeslot> swappingExams) {
        List<Exam> exams = createExamList(swappingExams);
        int counter = 0;
        for (int i = 0; i < exams.size(); i++) {
            Exam e1 = exams.get(i);
            Exam e2 = (i == exams.size() - 1) ? exams.get(0) : exams.get(i + 1);
            if (checkSwap(swappingExams.get(e2), e2, e1)) {
                counter++;
            }
        }
        return counter == exams.size();
    }

    /**
     * Retrives a list of exams from a map in which for eaxh exam there is the
     * correspondant timeslot.
     *
     * @param swappingExams
     * @return
     */
    private List<Exam> createExamList(Map<Exam, Timeslot> swappingExams) {
        List<Exam> exams = new ArrayList<>();
        for (Exam e : swappingExams.keySet()) {
            exams.add(e);
        }
        return exams;
    }

    /**
     * Does multiple swaps among exams contained in <code>swappingExams</code>
     * and it updates the value of the cost function.
     *
     * @param swappingExams
     */
    private void doMultipleSwaps(Map<Exam, Timeslot> swappingExams) {
        List<Exam> exams = createExamList(swappingExams);
        int penalty = 0;
        for (int i = 0; i < exams.size(); i++) {
            Exam e1 = exams.get(i);
            Exam e2 = (i == exams.size() - 1) ? exams.get(0) : exams.get(i + 1);
            Timeslot src = swappingExams.get(e1);
            Timeslot dest = swappingExams.get(e2);
            penalty += CostFunction.getExamMovePenalty(e1, src.getTimeslotID(), dest.getTimeslotID(), timeslots);
            src.removeExam(e1);
            dest.addExam(e1);
        }
        if (penalty < 0) {
            updateCost(penalty);
        }else{
            undoSwaps(swappingExams);
        }
        
    }
    
    /**
     * It undoes the operation of previous multiple swaps done using the map swappingExams
     * @param swappingExams 
     */
    private void undoSwaps(Map<Exam, Timeslot> swappingExams) {
        List<Exam> exams = createExamList(swappingExams);        
        for(int i=0; i<exams.size();i++){
            Exam e1 = exams.get(i);
            Exam e2 = (i == exams.size() - 1) ? exams.get(0) : exams.get(i + 1);
            Timeslot dest = swappingExams.get(e1);
            Timeslot src = swappingExams.get(e2);
            src.removeExam(e1);
            dest.addExam(e1);
        }
    }

}
