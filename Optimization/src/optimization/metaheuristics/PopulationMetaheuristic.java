/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.metaheuristics;

import java.util.Collection;
import java.util.List;
import optimization.Optimizer;
import optimization.domain.Schedule;

/**
 * Reperesents a population-based metaheuristic, i.e. a metaheuristic which
 * improves multiple candidate initial solutions.
 * *********IMPORTANT!!!!!!*******: When implementing a new
 * PopulationMetaheuristic besides overriding the improveInitialSolution()
 * method, the new class must be added to Optimizer.pMetaheuristics in the
 * method called initMetaheuristics() around line 74 of Optimizer.
 *
 * @author Carolina Bianchi
 */
public abstract class PopulationMetaheuristic extends Metaheuristic {

    protected Collection<Schedule> initialPopulation;
    public static int INITIAL_POP_SIZE = 4; //?? dont know

    public PopulationMetaheuristic(Optimizer optimizer, List<Schedule> initialPopulation) {
        super(optimizer);
        this.initialPopulation = initialPopulation;
    }

}
