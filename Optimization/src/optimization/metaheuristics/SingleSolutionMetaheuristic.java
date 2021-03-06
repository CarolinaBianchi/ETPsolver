/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.metaheuristics;

import optimization.Cloner;
import optimization.Optimizer;
import optimization.domain.Schedule;

/**
 * Represents a single solution metaheuristic, i.e. a metaheuristic which
 * improves a single initial solution. *********IMPORTANT!!!!!!*******: When
 * implementing a new SingleSolutionMetaheuristics besides overriding the
 * improveInitialSolution() method, the new class must be added to
 * Optimizer.ssMetaheuristics in the method called initMetaheuristics() around
 * line 74 of Optimizer.
 *
 * @author Carolina Bianchi
 */
public abstract class SingleSolutionMetaheuristic extends Metaheuristic {

    protected Schedule initSolution;

    public SingleSolutionMetaheuristic(Optimizer optimizer, Schedule initSolution, long MAX_MILLIS) {
        super(optimizer, MAX_MILLIS);
        this.mySolution = initSolution;
        this.initSolution = Cloner.clone(initSolution);
    }
    
    @Override
    public void notifyNewSolution(){
        optimizer.updateOnNewSolution(mySolution);
    }

}
