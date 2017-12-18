/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.metaheuristics;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import optimization.Optimizer;
import optimization.domain.CostFunction;
import optimization.domain.Schedule;

/**
 * Class that implements the Genetic Algorithm. Each schedule is a chromosome
 * and the metaheuristics mutates, inverts and combine(in theory) chromosomes to
 * find the best possible one
 *
 * @author Elisa
 */
public class GeneticAlgorithm extends PopulationMetaheuristic {

    private List<Schedule> population;
    private static final int MINUTES = 3;
    private static Random rnd;

    public GeneticAlgorithm(Optimizer optimizer, List<Schedule> initialPopulation) {
        super(optimizer, initialPopulation);
        population = initialPopulation;
        rnd = new Random();
    }

    @Override
    void improveInitialSol() {
        System.out.println("Genetic Algorithm");

        calcObjFunctions();
        //invertTimeslots();
        startAlgorithm();
        mySolution = findBestSchedule();
        System.out.println("OBJfunction value: " + mySolution.getCost());
    }

    /**
     * Starts the timer and chose randomly how to change the population.
     */
    private void startAlgorithm() {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        long totalTime = MINUTES * 60 * 1000;
        while (elapsedTime < totalTime) {
            switch (rnd.nextInt(4)) {
                case 0:
                    mutateExams();
                    break;
                case 1:
                    mutateTimeslots();
                    break;
                case 2:
                    invertTimeslots();
                    break;
                case 3:
                    doCrossover();
                    break;
            }

            elapsedTime = System.currentTimeMillis() - startTime;

        }
    }

    /**
     * Calculates the value of the objective function for each schedule in the
     * <code>population</code>
     */
    private void calcObjFunctions() {
        for (Schedule s : population) {
            s.setCost(CostFunction.getCost(s));
        }
    }

    /**
     * Tries to swap exams between timeslots of a random schedule. If the
     * operation has success and it decreases the cost, the mutated scheduled is
     * kept as part of the population.
     *
     * @return
     */
    private void mutateExams() {
        Schedule s = getRandomSchedule();
        int oldCost=s.getCost();
        s.mutateExams();
        //System.out.println("MutateExams "+ (oldCost!=s.getCost())+ "| "+oldCost+" - "+ s.getCost());
    }

    /**
     * In a random schedule, it swaps one timeslot with another if the swap
     * reduces the penalty.
     */
    private void mutateTimeslots() {
        Schedule s = getRandomSchedule();
        int oldCost=s.getCost();
        s.tryMutateTimeslots();
        //System.out.println("MutateTimeslots "+ (oldCost!=s.getCost())+ "| "+oldCost+" - "+ s.getCost());
    }

    /**
     * Returns a random schedule from the population.
     *
     * @return
     */
    private Schedule getRandomSchedule() {
        return population.get(rnd.nextInt(population.size()));
    }

    /**
     * Inverts the order of a random interval of timeslots in a random schedule.
     *
     */
    private void invertTimeslots() {
        Schedule s = getRandomSchedule();
        int oldCost=s.getCost();
        int[] cutPoints = getRandomCutPoints(s.getTmax());
        if ((cutPoints[1] - cutPoints[0]) > 1) {
            s.tryInvertTimeslots(cutPoints);
        }
        //System.out.println("InvertTimeslots "+ (oldCost!=s.getCost())+ "| "+oldCost+" - "+ s.getCost());
    }

    /**
     * Returns to random number that are the range of an interval in a schedule
     * with length <code>tmax</code>
     *
     * @param tmax
     * @return
     */
    private int[] getRandomCutPoints(int tmax) {
        int[] points = new int[2];
        points[0] = rnd.nextInt(tmax - 1); //startPoint
        points[1] = points[0] + rnd.nextInt(tmax - points[0] - 1); //endPoint
        return points;
    }

    /**
     * Sorts the schedule on the value of the objective functiona and returns
     * the schedule with the best, so lower, value.
     *
     * @return
     */
    private Schedule findBestSchedule() {
        Collections.sort(population);
        return population.get(0);
    }

    /**
     * Takes two different schedules from the population and tries to do a
     * crossover between the two. If the change improves the value of the cost
     * function, it takes the exams contained into a timeslot of
     * <code>parent2</code> and it select randomly one of them. It finds the
     * position of this exam in <code>parent1</code> and tries to add there the
     * other exams previously found.
     */
    private void doCrossover() {
        Schedule parent1 = getRandomSchedule();
        int oldCost=parent1.getCost();
        Schedule parent2 = getRandomSchedule();
        if (parent1 != parent2) {
            parent1.tryCrossover(parent2.getRandomTimeslot().getExams());
        }
        //System.out.println("DoCrossover "+ (oldCost!=parent1.getCost())+ "| "+oldCost+" - "+ parent1.getCost());
    }
}
