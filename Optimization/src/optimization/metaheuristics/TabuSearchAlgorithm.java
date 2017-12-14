/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.metaheuristics;

import java.util.LinkedList;
import java.util.List;
import optimization.domain.Exam;
import optimization.Optimizer;
import optimization.domain.CostFunction;
import optimization.domain.Schedule;
import optimization.domain.Timeslot;

/**
 *
 * @author lucie
 */
public class TabuSearchAlgorithm extends SingleSolutionMetaheuristic {

    private Optimizer optimizer;
    private Schedule initialSchedule;
    private LinkedList<String> tabuList;
    private int numberOfIterarion = 1000;
    //private Schedule bestSchedule;
    private double initialCost;
    private int tabuListsize = 100;

    public TabuSearchAlgorithm(Optimizer optimizer, Schedule initialSchedule) {
        super(optimizer, initialSchedule);
        //this.mySchedule=mySchedule;
        this.tabuList = new LinkedList<>();
        this.optimizer = optimizer;
        this.initialSchedule = initialSchedule;
        this.initialCost = CostFunction.getCost(initialSchedule);
        System.out.println(initialCost);

        /* for(Timeslot time:this.mySchedule.getTimeslots()){
                time.getExams().stream().forEach(ex->System.out.println(ex.getId()+" "));
             /*   for(Exam ex:time.getExams()){
                    System.out.println(ex.getId()+" ");
                }
            }*/
    }

    @Override
    void improveInitialSol() {
        mySolution = algorithm();
    }

    public Schedule algorithm() {
        Schedule bestSchedule = this.initialSchedule;
        double cost = this.initialCost;
        System.out.println(cost);
        for (int i = 0; i < numberOfIterarion; i++) {
            Schedule currentSchedule = bestSchedule;
            double currentCost;
            Timeslot t1 = currentSchedule.getRandomTimeslot();
            //  System.out.println("element of timeslot t1  "+t1.getTimesoltID());
            //  t1.getExams().stream().forEach(ex->System.out.println(ex.getId()+" "));
            Timeslot t2 = currentSchedule.getRandomTimeslot();
            //   System.out.println("element of timeslot t2  "+t2.getTimesoltID());
            //   t2.getExams().stream().forEach(ex->System.out.println(ex.getId()+" "));
            Exam e = t1.getRandomExam();
            //    System.out.println("exam ID   "+e.getId());
            if (t1.getTimeslotID() != t2.getTimeslotID()) {
                t1.removeExam(e);
                t2.addExam(e);

                currentCost = CostFunction.getCost(currentSchedule);
                // System.out.println("currentCost  " + currentCost + "  bestCost   " + cost);

                if (tabuList.size() < tabuListsize) {
                    // System.out.println("tlsize "+tabuList.size()+" size "+tabuListsize);
                    if (!tabuList.contains(t2.getTimeslotID() + "-" + t1.getTimeslotID() + "-" + e)) {
                        tabuList.addFirst(t2.getTimeslotID() + "-" + t1.getTimeslotID() + "-" + e);
                        if (currentCost < cost) {
                            //   System.out.println("currentCost  " + currentCost + "  bestCost   " + cost);
                            cost = currentCost;
                            //   System.out.println("currentCost  " + currentCost + "  bestCost   " + cost);
                        }
                    } else {
                        if (currentCost < cost) {
                            cost = currentCost;
                        } else {
                            t1.addExam(e);
                            t2.removeExam(e);
                        }

                    }

                } else {
                    if (!tabuList.contains(t2.getTimeslotID() + "-" + t1.getTimeslotID() + "-" + e)) {
                        tabuList.removeLast();
                        tabuList.addFirst(t2.getTimeslotID() + "-" + t1.getTimeslotID() + "-" + e);
                        if (currentCost <= cost) {
                            cost = currentCost;
                        }
                    } else {
                        if (currentCost < cost) {
                            cost = currentCost;
                        } else {
                            t1.addExam(e);
                            t2.removeExam(e);
                        }

                    }
                }

                System.out.println("currentCost  " + currentCost + "  bestCost   " + cost);
            }
        }
        return bestSchedule;
    }

    /*double bestCost() {
        return this.bestCost;
    }*/
}
