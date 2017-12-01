/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.initialization;

import java.util.Collections;
import java.util.List;
import optimization.Exam;
import optimization.Optimizer;
import optimization.Schedule;

/**
 *
 * @author Carolina Bianchi
 */
public class InfeasibleInitializer extends FeasibleInitializer {

    public InfeasibleInitializer(List<Exam> exams, int tmax, Optimizer opt) {
        super(exams, tmax, opt);
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
                forceRandomSchedule();
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
     * Force the generation of a random schedule, even if it creates some
     * collisions.
     */
    protected void forceRandomSchedule() {
        Exam toBePlaced;
        // I iterate on the exams that still have to be placed
        while (alreadyPlaced != exams.size()) {
            toBePlaced = exams.get(alreadyPlaced);
            // If i didn't manage to place the exam in the available number of tries, I pass to the swap/move phase
            if (!tryRandomPlacement(toBePlaced)) {
                mySchedule.forceRandomPlacement(toBePlaced);
                alreadyPlaced++;

                System.out.println("Placed: " + this.getPercPlaced());
            }

        }
    }

}
