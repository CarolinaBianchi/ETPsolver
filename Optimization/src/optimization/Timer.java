/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import java.util.logging.Level;
import java.util.logging.Logger;
import optimization.metaheuristics.DeepDiveAnnealingV2;

/**
 *
 * @author Carolina Bianchi
 */
public class Timer extends Thread {

    private Optimizer optimizer;
    private final long START_TIME; // Time at which the whole program started
    private final long TOT_TIME; // Total available time
    private static long TOT_SEARCH_TIME; // Time reserved to search the solution
    public final long PRINT_TIME = 1000/*ms*/; //Time reserverd for printing the solution
    public static long INIT_SOL_TIME; //Fraction of time reserved to the production of the first generation of solutions
    public static long METAHEURISTICS_TIME; // Time reserved to run the metaheuristics
    public static final int MAX_THREADS = 2;
    public static final int POP_THREADS = 1;
    private int generation = 0;

    public Timer(long totTime, Optimizer optimizer) {
        this.optimizer = optimizer;
        this.START_TIME = System.currentTimeMillis();
        this.TOT_TIME = totTime;
        this.TOT_SEARCH_TIME = TOT_TIME - PRINT_TIME;
        this.INIT_SOL_TIME = (long) ((4.0 / 10.0) * TOT_SEARCH_TIME);
        this.METAHEURISTICS_TIME = (long) (1.0 / 20.0 * (TOT_SEARCH_TIME));
    }

    @Override
    public void run() {
        generateInitSolPool();
        manageTime();
    }

    /**
     * Method that handles the management of time.
     */
    private void manageTime() {
        long elapsedTime = getElapsedTime();
        if (elapsedTime > TOT_SEARCH_TIME) {
            printBestSolution();
        }

        /*If we are in the first fraction of time, we run the methods to initialize the initial solutions. */
        if (elapsedTime < (long) (INIT_SOL_TIME) && INIT_SOL_TIME != METAHEURISTICS_TIME) {
            optimizer.runAllSSMetaheuristics();
            this.INIT_SOL_TIME = METAHEURISTICS_TIME;
            DeepDiveAnnealingV2.changeK(0.80); // After the initialization, the temperature dacay is speeded up

        } else if (elapsedTime > (long) (INIT_SOL_TIME)) {
            while (elapsedTime < TOT_SEARCH_TIME) {
                if (isTimeForANewGeneration()) {
                    this.INIT_SOL_TIME = Math.min(TOT_SEARCH_TIME - getElapsedTime(), INIT_SOL_TIME);
                    generation++;
                    startNewGeneration();
                }
                wait500ms();
                elapsedTime = getElapsedTime();
            }
            printBestSolution();
        }
        wait500ms();
    }

    /**
     * Returns the time elapsed from the beginning.
     *
     * @return
     */
    private long getElapsedTime() {
        return System.currentTimeMillis() - this.START_TIME;
    }

    /**
     * Invokes the optimizer's method to write the final solution.
     */
    private void printBestSolution() {
        optimizer.writeSolution();
    }

    /**
     * Invokes the optimizer's method to run the initializers.
     */
    private void generateInitSolPool() {
        optimizer.run();
    }

    /**
     * Tells if it is time for a new generation.
     *
     * @return
     */
    private boolean isTimeForANewGeneration() {
        return getElapsedTime() > (long) INIT_SOL_TIME + (long) (generation * (METAHEURISTICS_TIME));
    }

    /**
     * Starts a new generation.
     */
    private void startNewGeneration() {
        optimizer.naturalSelection();
        optimizer.checkAllPMetaheuristics();
    }

    /**
     * Checks what we need to do in this moment.
     */
    public void checkTime() {
        this.manageTime();
    }

    /**
     * Waits 500ms.
     */
    private void wait500ms() {
        synchronized (this) {
            try {
                this.wait(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(Timer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
