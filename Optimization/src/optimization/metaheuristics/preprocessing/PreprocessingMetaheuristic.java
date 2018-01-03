/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.metaheuristics.preprocessing;

import optimization.Optimizer;
import optimization.domain.Schedule;
import optimization.metaheuristics.SingleSolutionMetaheuristic;

/**
 *
 * @author Carolina Bianchi
 */
public abstract class PreprocessingMetaheuristic extends SingleSolutionMetaheuristic {

    public PreprocessingMetaheuristic(Optimizer optimizer, Schedule initSolution, int MAX_MILLIS) {
        super(optimizer, initSolution, MAX_MILLIS);
    }

    @Override
    public void improveInitialSol() {
        preprocess();

    }

    abstract void preprocess();
    // do something 

    /**
     *
     */
    @Override
    public void notifyNewSolution() {
        optimizer.updateOnNewPreprocessedSolution(mySolution);
    }
}
