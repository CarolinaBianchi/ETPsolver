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

    public Metaheuristic(Optimizer optimizer) {
        this.optimizer = optimizer;
    }

    @Override
    public void run() {
        improveInitialSol();
        notifyNewSolution();
    }

    abstract void improveInitialSol();

    private void notifyNewSolution() {
        optimizer.updateOnNewSolution(mySolution);
    }

}
