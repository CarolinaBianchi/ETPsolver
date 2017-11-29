/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.initialization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.*;
import optimization.Exam;
import optimization.Schedule;
import optimization.Timeslot;

/**
 *
 * @author flavio
 */
public class BucketInitializer extends AbstractInitializer {
    private final double INITIAL_PERC = 2.0 / 3.0;  //...
    private final int STUCK = 2; //..
    private int THRESHOLD;
    private final int MOVE_TRIES;
    private final int PLACING_TRIES;
    private int currentWidth;
    private List<Exam> alreadyPlaced;
    private List<Exam> notYetPlaced;
    private List<Integer>[] buckets;
    int tmax;

    
    public BucketInitializer(List<Exam> exams, List<Schedule> schedules, int tmax) {
        super(exams, schedules, tmax);
        this.tmax = tmax;
        Collections.sort(exams);
        this.notYetPlaced = new ArrayList<>(exams);
        this.alreadyPlaced = new ArrayList<>();
        this.MOVE_TRIES = 2 * tmax;
        this.PLACING_TRIES = 2 * tmax;
        this.THRESHOLD = 1; // BOH it works well with our instances.....
        
        buckets = new List[6];
        initializeBuckets();
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

        int numtries = 0, imStuck = 0, bucketI = 0;
        List<Integer> currentBucket = buckets[bucketI++];
        
        while (!notYetPlaced.isEmpty()) {
            computeRandomSchedule(currentBucket);

            // I try to move randomly, until I manage to do it.
            while (!randomMove(currentBucket)) {
            }
            numtries++;
            // If I'm above the threshold
            if (numtries > THRESHOLD) {
                // I try to expand my current bucket with a new one.
                if (bucketI < 6) {
                    currentBucket.addAll(buckets[bucketI]);
                    this.currentWidth = currentBucket.size();
                    bucketI++;
                } else if (imStuck++ == STUCK) {
                    /* The window is at its max size,  
                    If I'm stuck I restart*/
                    imStuck = 0;
                    bucketI = 0;
                    restart();
                    currentBucket = buckets[bucketI++];
                }
                numtries = 0;
                printStatus();
                //printBucket(currentBucket);
            }
        }
        printStatus();
        writeSolution();
        return mySchedule;
    }

    /**
     * Tries to generate a schedule by means of random computation.
     */
    protected void computeRandomSchedule(List<Integer> bucket) {
        Exam toBePlaced;
        int i=0;
        // I iterate on the exams that still have to be placed
        while (!notYetPlaced.isEmpty() && i<notYetPlaced.size()) {
            toBePlaced = notYetPlaced.get(i);
            // If i didn't manage to place the exam in the available number of tries, I pass to the swap/move phase
            if (!tryRandomPlacement(toBePlaced, bucket)) {
                i++;
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
    protected boolean tryRandomPlacement(Exam toBePlaced, List<Integer> bucket) {
        for (int k = 0; k < PLACING_TRIES; k++) {
            if (mySchedule.randomPlacement(toBePlaced, bucket)) {
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
    protected boolean randomMove(List<Integer> bucket) {
        boolean moved = false;
        for (int i = 0; i < MOVE_TRIES; i++) {
            if (mySchedule.randomMove(bucket)) {
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
        this.currentWidth = 0;
        this.notYetPlaced = new ArrayList<>(exams);
        this.alreadyPlaced = new ArrayList<>();
        mySchedule = new Schedule(tmax);
        buckets = new List[6];
        initializeBuckets();
        this.THRESHOLD++;
    }

    /**
     * Prints some info about the current status of the solution.
     */
    protected void printStatus() {
        new Thread() {
            @Override
            public void run() {
                System.out.println("Buckets " + currentWidth + " placed:" + getPercPlaced());
                System.out.println(mySchedule.getNExams() + "/" + exams.size());
            }
        }.start();
    }

    /**
     * This method initializes the buckets that will hold the time slots
     */
    private void initializeBuckets() {
        int current; // Keeps track of the timeslot we're currently dealing with.
        List<Integer> currentBucket; // The bucket we're currently building.
        
        for(int i=0; i<6; i++) {
            currentBucket = new ArrayList<>();
            
            // Set the order of the buckets
            switch( i ) {
                case 1:
                    current = 3;
                    break;
                case 2:
                    current = 1;
                    break;
                case 3:
                    current = 4;
                    break;
                case 4:
                    current = 2;
                    break;
                case 5:
                    current = 5;
                    break;
                default:
                    current = 0;
                    break;
            }
            
            while(current<this.tmax) {
                currentBucket.add(current);
                current += 6;
            }
            
            buckets[i] = currentBucket;
            
        }
    }
    
    private void printBucket(List<Integer> bucket) {
        Iterator iter = bucket.listIterator();
        
        System.out.println("Time slots currently in the bucket");
        System.out.print(iter.next());
        while( iter.hasNext()) {
            System.out.print("-" + iter.next());
        }
        System.out.println("");
    }
}
