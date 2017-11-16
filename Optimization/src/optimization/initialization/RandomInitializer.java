/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.initialization;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import optimization.Exam;
import optimization.Schedule;
import fileutils.SolutionWriter;
import optimization.Timeslot;

/**
 * Random initialization
 *
 * @author Carolina Bianchi
 */
public class RandomInitializer extends AbstractInitializer {

    private static int PLACING_TRIES; // to be defined
    private static int SWAP_TRIES; //..
    private static int MOVE_TRIES; //..
    private int alreadyPlaced; // number of exams already placed in the schedule

    public RandomInitializer(List<Exam> exams, List<Schedule> schedules, int tmax) {
        super(exams, schedules, tmax);
        this.alreadyPlaced = 0;
        RandomInitializer.PLACING_TRIES = (int) 1.5 * tmax;
        RandomInitializer.SWAP_TRIES = 2 * tmax;
        RandomInitializer.MOVE_TRIES = 2 * tmax;
    }

    @Override
    public Schedule initialize() {
        Collections.sort(exams);
        computeRandomSchedule();
        while (mySchedule.getNExams() != exams.size()) {
            if (this.getPercPlaced() < 0.98) {
                computeRandomSchedule();
            } else {
                computeOrderedSchedule();
            }

            while (!randomSwap() && !randomMove()) {
                // HORRIBLE CODE
            }
        }
        writeSolution();
        return mySchedule;
    }

    protected void computeRandomSchedule() {
        Exam toBePlaced;
        // I iterate on the exams that still have to be placed
        while (alreadyPlaced != exams.size()) {
            toBePlaced = exams.get(alreadyPlaced);
            // If i didn't manage to place the exam in the available number of tries, I pass to the swap/move phase
            if (!tryRandomPlacement(toBePlaced)) {
                break;
            }

        }
    }

    private void computeOrderedSchedule() {
        Exam toBePlaced;
        // I iterate on the exams that still have to be placed
        while (alreadyPlaced != exams.size()) {
            toBePlaced = exams.get(alreadyPlaced);
            // If i didn't manage to place the exam in the available number of tries, I pass to the swap/move phase
            if (!tryOrderedPlacement(toBePlaced)) {
                break;
            }

        }
    }

    /**
     * Tries to place an exam in a random Timeslot in a given number of tries.
     *
     * @param toBePlaced
     * @return true if the exam was placed, false otherwise.
     */
    private boolean tryRandomPlacement(Exam toBePlaced) {
        Timeslot destination;
        for (int k = 0; k < PLACING_TRIES; k++) {
            destination = mySchedule.getRandomTimeslot();
            if (destination.isCompatible(toBePlaced)) {
                destination.addExam(toBePlaced);
                //toBePlaced.setPlaced(true);
                alreadyPlaced++;
                return true;
            }
        }
        return false;
    }

    private boolean tryOrderedPlacement(Exam toBePlaced) {
        for (Timeslot destination : mySchedule.getTimeslots()) {
            if (destination.isCompatible(toBePlaced)) {
                destination.addExam(toBePlaced);
                alreadyPlaced++;
                return true;
            }
        }
        return false;
    }

    /**
     * Performs a random swap between 2 exams. (It tries at maximum SWAP_TIMES
     * times to do it).
     *
     * @return true if the swapping attempt succeeded, false otherwise.
     */
    protected boolean randomSwap() {
        Exam ex1, ex2;
        Timeslot tj, tk;
        boolean swapped = false;
        for (int i = 0; i < SWAP_TRIES; i++) {
            tj = mySchedule.getRandomTimeslot();
            tk = mySchedule.getRandomTimeslot();
            ex1 = tj.getRandomExam();
            ex2 = tk.getRandomExam();
            if (checkFeasibleSwap(tj, ex1, tk, ex2)) {
                swap(tj, ex1, tk, ex2);
                swapped = true;
            }
        }
        return swapped;
    }

    /**
     * Tries to move 1 exam from a timeslot to another one.
     *
     * @return true if the exam was moved, false otherwise.
     */
    private boolean randomMove() {
        Timeslot tj, tk;
        Exam ex;
        boolean moved = false;
        for (int i = 0; i < MOVE_TRIES; i++) {
            tj = mySchedule.getRandomTimeslot();
            tk = mySchedule.getRandomTimeslot();
            ex = tj.getRandomExam();
            if (tk.isCompatible(ex)) {
                move(ex, tj, tk);
                moved = true;
            }
        }
        return moved;

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

    private void writeSolution() {

        SolutionWriter sw = new SolutionWriter(mySchedule);
        try {
            sw.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(RandomInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }
        sw.run();

    }

    @Override
    public String toString() {
        return this.getClass().getCanonicalName() + super.toString();
    }
}