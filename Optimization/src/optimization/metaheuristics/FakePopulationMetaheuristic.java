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
 *
 * @author Carolina Bianchi
 */
public class FakePopulationMetaheuristic extends PopulationMetaheuristic{

    public FakePopulationMetaheuristic(Optimizer optimizer, List<Schedule> initialPopulation) {
        super(optimizer, initialPopulation);
    }



    @Override
    void improveInitialSol() {
        System.out.println("I'm a population metaheuristic");
    }
    
}
