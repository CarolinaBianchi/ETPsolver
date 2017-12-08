/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.metaheuristics;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import optimization.Cloner;
import optimization.Optimizer;
import optimization.Schedule;

/**
 * Class that implements the Genetic Algorithm. Each schedule is a chromosome and 
 * the metaheuristics mutates, inverts and cobine chromosomes to find the best possible one
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
        population = (List<Schedule>) Cloner.clone(initialPopulation);
        //mutateExams(); //OK
        //mutateTimeslots(); //OK
        //invertTimeslots(); // OK
    }

    /**
     * Given a random schedule, it swaps an exam in a timeslot with another exam 
     * of another timeslot
     * @return 
     */
    private boolean mutateExams() {
        return getRandomSchedule().mutateExams();
    }

    /**
     * Given a random schedule, it swaps one timeslot with another
     */
    private void mutateTimeslots() {
        getRandomSchedule().mutateTimeslots();
    }

    /**
     * Returns a random schedule from the population
     * @return 
     */
    private Schedule getRandomSchedule() {
        Random rnd = new Random();
        return population.get(rnd.nextInt(population.size()));
    }

    /**
     * Given two random point in a schedule (startPoint and endPoint) it 
     * inverts the order. Given  i=endPoint-startPoint-1, in the end the timeslot 
     * in position startPoint+i contains the exams that were of timeslot in 
     * position endPoint-i-1 and vice versa.
     * 
     */
    private void invertTimeslots() {
        Random rnd = new Random();
        Schedule s = getRandomSchedule();
        int startPoint = rnd.nextInt(s.getTmax() - 1);
        int endPoint = startPoint + rnd.nextInt(s.getTmax() - startPoint - 1);
        getRandomSchedule().invertTimeslots(startPoint, endPoint);
    }

}
