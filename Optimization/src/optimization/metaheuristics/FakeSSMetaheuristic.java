/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.metaheuristics;

import optimization.Optimizer;
import optimization.domain.Schedule;

/**
 *
 * @author Carolina Bianchi
 */
public class FakeSSMetaheuristic extends SingleSolutionMetaheuristic {

    public FakeSSMetaheuristic(Optimizer optimizer, Schedule initSolution) {
        super(optimizer, initSolution);
    }

    @Override
    void improveInitialSol() {
        System.out.println("\t I'm a single solution metaheuristic");
    }

}
