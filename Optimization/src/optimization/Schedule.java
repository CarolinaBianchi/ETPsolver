/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import java.util.Iterator;
import java.util.List;
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
    private double cost; // to be defined

    public Schedule(int tmax) {
        timeslots = new Timeslot[tmax];
        initTimeslots();
    }

    public void setTimeslots(Timeslot[] ts) {
        this.timeslots = ts;
    }

    private void initTimeslots() {
        for (int i = 0; i < timeslots.length; i++) {
            timeslots[i] = new Timeslot();
        }
    }

    public Timeslot[] getTimeslots() {
        return this.timeslots;
    }

    public int getTmax() {
        return this.timeslots.length;
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
     * Moves an exam from a source timeslot to a destination.
     *
     * @param ex
     * @param source
     * @param dest
     */
    private void move(Exam ex, Timeslot source, Timeslot dest) {
        source.removeExam(ex);
        dest.addExam(ex);
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
        if (tk.isCompatible(ex)) {
            //System.out.println("Successful move");
            move(ex, tj, tk);
            return true;
        }

        return false;
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
        if (tk.isCompatible(ex)) {
            //System.out.println("Successful move");
            move(ex, tj, tk);
            return true;
        }

        return false;
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
        if (tk.isCompatible(ex)) {
            //System.out.println("Successful move");
            move(ex, tj, tk);
            return true;
        }

        return false;
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
     * Returns a clone of this schedule.
     *
     * @return
     */
    @Override
    public Schedule clone() {
        int tmax = this.getTmax();
        Schedule s = new Schedule(tmax);
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
   
    @Override
    public int compareTo(Schedule o) {
        return (o.getObjFunction() - this.getObjFunction() * 100);
    }

    
    /**
     * Calculates the value of the objective function for the schedule.
     * For each distance j it checks in all the timeslots if in the timeslot that
     * is at a distance j there is a conflicting exam. If so it calculate the penalty.
     * @return 
     */
    private int getObjFunction() {
        int penalty=0;        
        for(int j=1; j<=5; j++){
            for (int i = 0; i < timeslots.length-j; i++){
                for (Exam e : timeslots[i].getExams()) {
                    penalty+=timeslots[i + j].calcPenalty(j, e.getConflictingExams());
                }
            }
        }        
        return penalty;
    }
    
    

}
