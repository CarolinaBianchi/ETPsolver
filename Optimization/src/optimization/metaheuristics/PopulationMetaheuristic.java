/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.metaheuristics;

import java.util.Collection;
import optimization.Optimizer;
import optimization.Schedule;

/**
 * Reperesents a population-based metaheuristic, i.e. a metaheuristic which
 * improves multiple candidate initial solutions.
 *
 * @author Carolina Bianchi
 */
public abstract class PopulationMetaheuristic extends Metaheuristic {

    private Collection<Schedule> initialPopulation;

    public PopulationMetaheuristic(Optimizer optimizer, Collection<Schedule> initialPopulation) {
        super(optimizer);
        this.initialPopulation = initialPopulation;
    }

    /**
     * Returns the minumim amount of initial solution needed to run the concrete
     * metaheuristic.
     *
     * @return
     */
    public abstract int getThreshold();
}
