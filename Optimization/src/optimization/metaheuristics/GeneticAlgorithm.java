/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.metaheuristics;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import optimization.Cloner;
import optimization.Optimizer;
import optimization.domain.Exam;
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
    private static final int N_ITERATIONS = 100;
    private static final int MINUTES = 3;

    public GeneticAlgorithm(Optimizer optimizer, List<Schedule> initialPopulation) {
        super(optimizer, initialPopulation);
        population = initialPopulation;
    }

    @Override
    void improveInitialSol() {
        System.out.println("Genetic Algorithm");
     
        int counter = 0;
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;

        calcObjFunctions();

        while (elapsedTime < MINUTES * 60 * 1000) {
            while (counter++ % 100 != 0) {
                switch (new Random().nextInt(3)) {
                    case 0:
                        mutateExams();
                        break;
                    case 1:
                        mutateTimeslots();
                        break;
                    case 2:
                        invertTimeslots();
                        break;
                }
            }
            //System.out.println("OBJfunction value: " + findBestSchedule().getCost());
            elapsedTime = System.currentTimeMillis() - startTime;
        }

        mySolution = findBestSchedule();
        System.out.println("OBJfunction value: " + mySolution.getCost());
    }

    /**
     * Calculates the value of the objective function for each schedule. in the
     * <code>population</code>
     */
    private void calcObjFunctions() {
        for (Schedule s : population) {
            s.updateCost();
        }
    }

    /**
     * Tries to swap exams between timeslots of a random schedule. If the
     * operation has success, it start the selection process.
     *
     * @return
     */
    private void mutateExams() {
        Schedule oldSchedule = getRandomSchedule();
        Schedule newSchedule = Cloner.clone(oldSchedule);
        if (newSchedule.mutateExams()) {
            actSelection(oldSchedule, newSchedule);
        }
    }

    /**
     * Swaps one timeslot with another in a random schedule, then it starts the
     * selection process.
     */
    private void mutateTimeslots() {
        Schedule oldSchedule = getRandomSchedule();
        Schedule newSchedule = Cloner.clone(oldSchedule);
        newSchedule.mutateTimeslots();
        actSelection(oldSchedule, newSchedule);
    }

    /**
     * Returns a random schedule from the population.
     *
     * @return
     */
    private Schedule getRandomSchedule() {
        Random rnd = new Random();
        return population.get(rnd.nextInt(population.size()));
    }

    /**
     * Inverts the order of a random interval of timeslots in a random schedule.
     *
     */
    private void invertTimeslots() {
        Schedule oldSchedule = getRandomSchedule();
        Schedule newSchedule = Cloner.clone(oldSchedule);
        newSchedule.invertTimeslots(getRandomCutPoints(newSchedule.getTmax()));
        actSelection(oldSchedule, newSchedule);
    }

    /**
     * Does the selection process. If the <code>newSchedule</code> has a lower
     * value of the objective function with respect to <code>oldSchedule</code>
     * , it substitued the old schedule with the new one in the population.
     *
     * @param oldSchedule
     * @param newSchedule
     */
    private void actSelection(Schedule oldSchedule, Schedule newSchedule) {
        newSchedule.updateCost();
        if (newSchedule.getCost() < oldSchedule.getCost()) {
            population.remove(oldSchedule);
            population.add(newSchedule);
        }
    }

    /**
     * Returns to random number that are the range of an interval in a schedule
     * with length <code>tmax</code>
     *
     * @param tmax
     * @return
     */
    private int[] getRandomCutPoints(int tmax) {
        Random rnd = new Random();
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

    private void doOrderCrossover() {
        Schedule parent1 = getRandomSchedule();
        Schedule parent2 = getRandomSchedule();
        Schedule child1 = Cloner.clone(parent1);
        Schedule p2Copy = Cloner.clone(parent2);
        //Schedule child2=Cloner.clone(parent2);
        //int[] cutPoints = getRandomPoints(child1);
//        int[] cutPoints = new int[]{2, 5};
//        child1.selectSection(cutPoints);
//
//        for (int i = cutPoints[1]; i < child1.getTmax(); i++) { // dall'endPoint del genitore1 provo a mettere i timeslots del 2
//            child1.addTimeslot(i, parent2.getTimeslot(i), cutPoints);
//        }
//
//        for (int i = 0; i < cutPoints[0]; i++) {
//            child1.addTimeslot(i, parent2.getTimeslot(i), cutPoints);
//        }
//
//        int unpositioned = 622 - child1.getNExams();
//        System.out.println("unpositioned " + unpositioned);
//        child1.doExamOrderCrossover(parent2.getSectionTimeslots(cutPoints), cutPoints);

//        int[] cutPoints = new int[]{2, 5};
//        child1.selectSection(cutPoints);
//        int counter=0;
//
//        for (int j = cutPoints[1]; j < p2Copy.getTmax(); j++) {
//            List<Exam> exams=p2Copy.getTimeslot(j).getExams();
//            for (int k=0; k< p2Copy.getTimeslot(j).getExams().size();k++) {
//                boolean positioned=false;
//                for (int i = cutPoints[1]; i < child1.getTmax(); i++) {
//                    if (child1.getTimeslot(i).isCompatible(exams.get(k))) {
//                        child1.getTimeslot(i).addExam(exams.get(k));
//                        p2Copy.getTimeslot(j).removeExam(exams.get(k));
//                        positioned=true;
//                        counter++;
//                        break;
//                    }
//                }
//
//                if(!positioned){
//                    for (int i = 0; i < cutPoints[0]; i++) {
//                    if (child1.getTimeslot(i).isCompatible(exams.get(k))) {
//                        child1.getTimeslot(i).addExam(exams.get(k));
//                        p2Copy.getTimeslot(j).removeExam(exams.get(k));
//                        counter++;
//                    }
//                }
//                }
//                
//            }
//
//        }
//        
//        for (int j = 0; j < cutPoints[0]; j++) {
//            
//            List<Exam> exams=p2Copy.getTimeslot(j).getExams();
//            for (int k=0; k< p2Copy.getTimeslot(j).getExams().size();k++) {
//                boolean positioned=false;
//                for (int i = cutPoints[1]; i < child1.getTmax(); i++) {
//                    if (child1.getTimeslot(i).isCompatible(exams.get(k))) {
//                        child1.getTimeslot(i).addExam(exams.get(k));
//                        p2Copy.getTimeslot(j).removeExam(exams.get(k));
//                        positioned=true;
//                        counter++;
//                        break;
//                    }
//                }
//
//                if(!positioned){
//                    for (int i = 0; i < cutPoints[0]; i++) {
//                    if (child1.getTimeslot(i).isCompatible(exams.get(k))) {
//                        child1.getTimeslot(i).addExam(exams.get(k));
//                        p2Copy.getTimeslot(j).removeExam(exams.get(k));
//                        counter++;
//                    }
//                }
//                }
//                
//            }
//
//        }
//
//        int unpositioned = 622 - child1.getNExams();
//        System.out.println("unpositioned " + unpositioned);
//
//        // add new schedules
    }
}
