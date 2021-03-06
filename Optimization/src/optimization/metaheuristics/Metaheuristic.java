/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.metaheuristics;

import optimization.Optimizer;
import optimization.domain.Schedule;

/**
 * Represents a general metaheuristic.
 *
 * @author Carolina Bianchi
 */
public abstract class Metaheuristic extends Thread {

    protected Optimizer optimizer;
    protected Schedule mySolution;
    protected long MAX_MILLIS;

    public Metaheuristic(Optimizer optimizer, long MAX_MILLIS) {
        this.optimizer = optimizer;
        this.MAX_MILLIS = MAX_MILLIS;
    }

    @Override
    public void run() {
        improveInitialSol();
        notifyNewSolution();
    }

    public abstract void improveInitialSol();

    abstract void notifyNewSolution();
    /*private void notifyNewSolution() {
        optimizer.updateOnNewSolution(mySolution);
    }*/

}
