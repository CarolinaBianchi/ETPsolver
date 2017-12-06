/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.metaheuristics;

import java.util.Collection;
import java.util.List;
import optimization.Cloner;
import optimization.Optimizer;
import optimization.Schedule;

/**
 *
 * @author Elisa
 */
public class GeneticAlgorithm extends PopulationMetaheuristic {
    
    private List<Schedule> population;

    public GeneticAlgorithm(Optimizer optimizer, Collection<Schedule> initialPopulation) {
        super(optimizer, initialPopulation);
    }

    @Override
    void improveInitialSol() {
        System.out.println("Genetic algorithm");
        population=(List<Schedule>) Cloner.clone(initialPopulation);
        
    }
    
    
}
