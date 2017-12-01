/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.initialization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import optimization.Exam;
import optimization.Optimizer;
import optimization.Schedule;

/**
 * This initializer works as follows. It initially considers a window of
 * timeslots of a certain width (<code>INITIAL_PERC * tmax</code>) and tries to
 * place there as many exams as possible; when a certain <code>THRESHOLD</code>
 * of tries is reached, the window is widened 1 timeslot at the time, and the
 * process is repeated.
 *
 * @author Carolina Bianchi
 */
public class WindowInitializer extends AbstractInitializer {

    private final double INITIAL_PERC = 2.0 / 3.0;  //...
    private final int STUCK = 2; //..
    private final int THRESHOLD;
    private final int MOVE_TRIES;
    private final int PLACING_TRIES;
    private int currentWidth;
    private List<Exam> alreadyPlaced;
    private List<Exam> notYetPlaced;

    
    public WindowInitializer(List<Exam> exams, int tmax, Optimizer opt) {
        super(exams, tmax, opt);
        this.currentWidth = (int) (INITIAL_PERC * tmax);
        Collections.sort(exams);
        this.notYetPlaced = new ArrayList<>(exams);
        this.alreadyPlaced = new ArrayList<>();
        this.MOVE_TRIES = 2 * tmax;
        this.PLACING_TRIES = 2 * tmax;
        this.THRESHOLD = 10000; // BOH it works well with our instances.....
    }

    /**
     * <pre>
     * This initialization works as foolows.
     * - I try to place as many exams as possible randomly considering a
     * restricted window of timeslots. (exams are considered in descending order
     * of their number of conflicts).
     * - When I get stuck with an exam (I do not manage to place it
     * <code>PLACING_TRIES</code> number of tries) I try to move the already
     * placed exams in other timeslots.
     * - As soon as the number of tries reaches <code>THRESHOLD</code>, the
     * window is widened.
     * - The cycle is repeated until I manage to place all exams.
     * If I get stuck, I restart the process.
     * </pre>
     *
     * @return
     */
    @Override
    public Schedule initialize() {

        int numtries = 0, imStuck = 0;
        while (!notYetPlaced.isEmpty()) {
            computeRandomSchedule();

            // I try to move randomly, until I manage to do it.
            while (!randomMove()) {
            }
            numtries++;
            // If I'm above the threshold
            if (numtries > THRESHOLD) {
                // I widen the window, if possible
                if (this.currentWidth < mySchedule.getTmax()) {
                    this.currentWidth++;
                } else if (imStuck++ == STUCK) {
                    /* The window is at its max size,  
                    If I'm stuck I restart*/
                    imStuck = 0;
                    restart();
                }
                numtries = 0;
                printStatus();
            }
        }
        printStatus();
        writeSolution();
        return mySchedule;
    }

    /**
     * Tries to generate a schedule by means of random computation.
     */
    protected void computeRandomSchedule() {
        Exam toBePlaced;
        // I iterate on the exams that still have to be placed
        while (!notYetPlaced.isEmpty()) {
            toBePlaced = notYetPlaced.get(0);
            // If i didn't manage to place the exam in the available number of tries, I pass to the swap/move phase
            if (!tryRandomPlacement(toBePlaced)) {
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
            if (mySchedule.randomPlacement(toBePlaced, currentWidth)) {
                notYetPlaced.remove(toBePlaced);
                alreadyPlaced.add(toBePlaced);
                return true;
            }
        }
        return false;
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
            if (mySchedule.randomMove(currentWidth)) {
                moved = true;
            }
        }
        return moved;
    }

    /**
     * Restarts the entire process : resets alla parameters to their initial
     * values.
     */
    protected void restart() {
        int tmax = mySchedule.getTmax();
        this.currentWidth = (int) (INITIAL_PERC * tmax);
        this.notYetPlaced = new ArrayList<>(exams);
        this.alreadyPlaced = new ArrayList<>();
        mySchedule = new Schedule(tmax);
    }

    /**
     * Prints some info about the current status of the solution.
     */
    protected void printStatus() {
        new Thread() {
            @Override
            public void run() {
                System.out.println("currentWidth " + currentWidth + " placed:" + getPercPlaced());
                System.out.println(mySchedule.getNExams() + "/" + exams.size());
            }
        }.start();
    }

}
