/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import java.util.Random;

/**
 * Class that represents a Schedule.
 *
 * @author Carolina Bianchi
 */
public class Schedule {

    private Timeslot[] timeslots;
    private double cost; // to be defined

    public Schedule(int tmax) {
        timeslots = new Timeslot[tmax];
        initTimeslots();
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

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < timeslots.length; i++) {
            s += "Timeslot " + i + timeslots[i].toString() + "\n";
        }
        return s;
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
            //toBePlaced.setPlaced(true);
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
        Timeslot tj, tk;
        Exam ex;

        tj = getTimeslotWithExams();
        tk = getRandomTimeslot();
        ex = tj.getRandomExam();
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

}
