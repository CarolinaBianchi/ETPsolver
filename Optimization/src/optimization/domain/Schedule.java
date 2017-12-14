/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.domain;

import java.util.List;
import java.util.Random;

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
     * Computes the cost of this schedule.
     */
    public void computeCost() {
        this.cost = CostFunction.getCost(timeslots);
    }

    /**
     * Updates the cost of this schedule.
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
     * @param source
     * @param dest
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
    private boolean checkFeasibleSwap(Timeslot t1, Exam ex1, Timeslot t2, Exam ex2) {
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
    private void swap(Timeslot t1, Exam ex1, Timeslot t2, Exam ex2) {
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
            //System.out.println("Successful swap");
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
     * Returns a clone of this schedule.
     *
     * @return
     */
    @Override
    public Schedule clone() {
        int tmax = this.getTmax();
        Schedule s = new Schedule(tmax, numStudents);
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

    //------------------------------------ Genetic algorithm-------------------------------------
    /**
     * Schedules with lower value of the objective function are put firsts
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Schedule o) {
        return (int) ((this.getCost() - o.getCost()) * 100);
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
            swapped = randomSwap();
            iterNum++;

        } while (!swapped && iterNum != MAX_SWAP_TRIES); // 10?
        computeCost();
        return swapped;
    }

    /**
     * Takes two random timeslots and swaps the exams of one to the other
     */
    public void mutateTimeslots() {

        Timeslot tj, tk;
        Timeslot tmp = new Timeslot(0);

        tj = getRandomTimeslot();
        tk = getRandomTimeslot();

        tmp.addExams(tj.getExams());
        tj.cleanAndAddExams(tk.getExams());
        tk.cleanAndAddExams(tmp.getExams());
        computeCost();
    }

    /**
     * Given two cut-points, it remove all the exams in the timeslots in that
     * interval. Then it assigns those exams to the timeslots in the reverse
     * order.
     *
     * @param cutPoints
     */
    public void invertTimeslots(int[] cutPoints) {
        int startPoint = cutPoints[0];
        int endPoint = cutPoints[1];
        int length = endPoint - startPoint;
        Timeslot[] tmpSlots = createTmpSlots(length);

        for (int i = 0; i < length; i++) {
            tmpSlots[length - 1 - i].addExams(timeslots[startPoint + i].getExams());
            timeslots[startPoint + i].clean();
        }

        for (int i = 0; i < length; i++) {
            timeslots[startPoint + i].addExams(tmpSlots[i].getExams());
        }
        computeCost();
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
     * Eliminates all the exams that are not in the timeslots in the intevarl
     * [<code>cutPoints[0]</code>, <code>cutPoints[1]</code>)
     *
     * @param cutPoints
     */
//    public void selectSection(int[] cutPoints) {
//        for (int i = 0; i < this.getTmax(); i++) {
//            if (i < cutPoints[0] || i >= cutPoints[1]) {
//                timeslots[i].clean();
//            }
//        }
//    }
    /**
     * Returns if the exam <code>e</code> is in the interval of timeslots
     * [<code>cutPoints[0]</code>, <code>cutPoints[1]</code>)
     *
     * @param e
     * @param points
     * @return
     */
//    public boolean isExamInSection(Exam e, int[] points) {
//        for (int i = points[0]; i < points[1]; i++) {
//            if (timeslots[i].contains(e)) {
//                return true;
//            }
//        }
//        return false;
//    }
    /**
     * If an exam of timeslot <code>t</code> is not in the interval of timeslots
     * [<code>cutPoints[0]</code>, <code>cutPoints[1]</code>), it adds it in the
     * timeslot <code>timeslots[position]</code>
     *
     * @param position
     * @param t
     * @param points
     */
//    public void addTimeslotExams(int position, Timeslot t, int[] points) {
//        for (Exam e : t.getExams()) {
//            if (!isExamInSection(e, points)) {
//                timeslots[position].addExam(e);
//            }
//        }
//    }
    /**
     * Retrieves the timeslot in the interval [<code>cutPoints[0]</code>,
     * <code>cutPoints[1]</code>)
     *
     * @param cutPoints
     * @return
     */
//    public Timeslot[] getSectionTimeslots(int[] cutPoints) {
//        Timeslot[] tmpSlots = createTmpSlots(cutPoints[1] - cutPoints[0]);
//        for (int i = cutPoints[0]; i < cutPoints[1]; i++) {
//            tmpSlots[i - cutPoints[0]] = timeslots[i];
//        }
//        return tmpSlots;
//    }
//    public void doExamOrderCrossover(Timeslot[] p2section, int[] points) {
//        List<Exam> unpositioned = findUnpositionedExams(p2section);
//        int counter = 0;
//        boolean positioned = false;
//        for (Exam e : unpositioned) {
//            //System.out.println(e);
//            for (int i = points[1]; i < getTmax(); i++) {
//                if (positioned = timeslots[i].isCompatible(e)) {
//                    timeslots[i].addExam(e);
//                    counter++;
//                    break;
//                }
//            }
//            if (!positioned) {
//                for (int i = 0; i < points[1]; i++) {
//                    if (timeslots[i].isCompatible(e)) {
//                        timeslots[i].addExam(e);
//                        counter++;
//                        break;
//                    }
//                }
//            }
//            //System.out.println(positioned);
//        }
//        System.out.println(counter);
//    }
//
//    private List<Exam> findUnpositionedExams(Timeslot[] p2section) {
//        List<Exam> unpositioned = new ArrayList<>();
//        boolean positioned;
//        for (Timeslot tk : p2section) {
//            for (Exam e : tk.getExams()) {
//                positioned = false;
//                for (Timeslot tj : timeslots) {
//                    positioned = positioned || tj.contains(e);
//                }
//                if (!positioned) {
//                    unpositioned.add(e);
//                }
//            }
//        }
//        return unpositioned;
//    }
    private void printTimeslots() {
        for (int i = 0; i < timeslots.length; i++) {
            System.out.println(i + " - has timeslot " + timeslots[i].getTimeslotID());
        }
    }
}
