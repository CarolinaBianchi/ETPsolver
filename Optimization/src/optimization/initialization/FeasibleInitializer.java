/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.initialization;

import java.util.Collections;
import java.util.List;
import optimization.domain.Exam;
import optimization.Optimizer;
import optimization.domain.Schedule;
import optimization.domain.Timeslot;

/**
 * Random initialization
 *
 * @author Carolina Bianchi
 */
public class FeasibleInitializer extends AbstractInitializer {

    protected static int PLACING_TRIES; // to be defined
    protected static int SWAP_TRIES; //..
    protected static int MOVE_TRIES; //..
    protected int alreadyPlaced; // number of exams already placed in the schedule

    public FeasibleInitializer(List<Exam> exams, int tmax, Optimizer opt) {
        super(exams, tmax, opt);
        this.alreadyPlaced = 0;
        FeasibleInitializer.PLACING_TRIES = (int) 1.5 * tmax;
        FeasibleInitializer.SWAP_TRIES = 2 * tmax;
        FeasibleInitializer.MOVE_TRIES = 2 * tmax;
    }

    @Override
    public Schedule initialize() {
        Collections.sort(exams);

        computeRandomSchedule();
        int numtries = 1;

        System.out.println("First trial: " + this.getPercPlaced());

        while (mySchedule.getNExams() != exams.size()) {
            if (this.getPercPlaced() < 0.98) {
                computeRandomSchedule();
            } else {
                //forceRandomSchedule();
                computeOrderedSchedule();
            }

            while (!randomMove()) {
                // HORRIBLE CODE
            }

            //System.out.println("New trial: " + this.getPercPlaced());
            //System.out.println("Available time slots: " + mySchedule.freeTimeslotAvailability());
            numtries++;
        }
        //writeSolution();
        System.out.println("Number of tries: " + numtries);
        System.out.println("Number of collisions: " + mySchedule.getTotalCollisions());

        return mySchedule;
    }

    /**
     * Try to generate a schedule by means of random computation.
     */
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

    /**
     * Try to generate a schedule by means of ordered computation.
     */
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
    protected boolean tryRandomPlacement(Exam toBePlaced) {
        for (int k = 0; k < PLACING_TRIES; k++) {

            if (mySchedule.randomPlacement(toBePlaced)) {
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

        boolean swapped = false;
        for (int i = 0; i < SWAP_TRIES; i++) {
            if (mySchedule.randomSwap()) {
                swapped = true;
            }
        }
        return swapped;
    }

    /**
     * Tries to move 1 exam from a timeslot to another one.
     *
     * @return true if the exam was moved, false otherwise.
     *
     */
    protected boolean randomMove() {
        boolean moved = false;
        for (int i = 0; i < MOVE_TRIES; i++) {
            if (mySchedule.randomMove()) {
                moved = true;
            }
        }
        return moved;
    }

    @Override
    public String toString() {
        return this.getClass().getCanonicalName() + super.toString();
    }
}
