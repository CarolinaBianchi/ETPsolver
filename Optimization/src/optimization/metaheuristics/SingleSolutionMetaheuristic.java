/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.metaheuristics;

import optimization.Optimizer;
import optimization.Schedule;

/**
 * Represents a single solution metaheuristic, i.e. a metaheuristic which
 * improves a single initial solution.
 *
 * @author Carolina Bianchi
 */
public abstract class SingleSolutionMetaheuristic extends Metaheuristic {

    private Schedule initSolution;

    public SingleSolutionMetaheuristic(Optimizer optimizer, Schedule initSolution) {
        super(optimizer);
        this.initSolution = initSolution;
    }

}
