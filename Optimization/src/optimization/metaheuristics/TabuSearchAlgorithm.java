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
import optimization.domain.Schedule;
import optimization.domain.Timeslot;

/**
 *
 * @author lucie
 */
public class TabuSearchAlgorithm extends SingleSolutionMetaheuristic {

    private Optimizer optimizer;
    //private Schedule mySchedule;
    private LinkedList<String> tabuList;
    private int numberOfIterarion = 10;
    private Schedule bestSchedule;
    private double bestCost;
    private int tabuListsize = 10;

    public TabuSearchAlgorithm(Optimizer optimizer, Schedule initialSchedule) {
        super(optimizer, initialSchedule);
        //this.mySchedule=mySchedule;
        this.tabuList = new LinkedList<>();
        this.optimizer = optimizer;
        this.bestSchedule = initialSchedule;
        this.bestCost = this.optimizer.objectifFunction(bestSchedule);

        /* for(Timeslot time:this.mySchedule.getTimeslots()){
                time.getExams().stream().forEach(ex->System.out.println(ex.getId()+" "));
             /*   for(Exam ex:time.getExams()){
                    System.out.println(ex.getId()+" ");
                }
            }*/
    }

    @Override
    void improveInitialSol() {
        algorithm();
    }

    public void algorithm() {

        for (int i = 0; i < numberOfIterarion; i++) {
            Schedule currentSchedule = this.bestSchedule;
            double currentCost;
            Timeslot t1 = currentSchedule.getRandomTimeslot();
              System.out.println("element of timeslot t1  "+t1.getTimesoltID());
            //  t1.getExams().stream().forEach(ex->System.out.println(ex.getId()+" "));
            Timeslot t2 = currentSchedule.getRandomTimeslot();
               System.out.println("element of timeslot t2  "+t2.getTimesoltID());
            //   t2.getExams().stream().forEach(ex->System.out.println(ex.getId()+" "));
            Exam e = t1.getRandomExam();
                System.out.println("exam ID   "+e.getId());
            if (t1.getTimesoltID() != t2.getTimesoltID()) {
                t1.removeExam(e);
                t2.addExam(e);

                currentCost = this.optimizer.objectifFunction(currentSchedule);
                System.out.println("currentCost  " + currentCost + "  bestCost   " + this.bestCost());

                if (tabuList.size() < tabuListsize) {
                    if (!tabuList.contains(t2.getTimesoltID() + "-" + t1.getTimesoltID() + "-" + e)) {
                        tabuList.addFirst(t2.getTimesoltID() + "-" + t1.getTimesoltID() + "-" + e);
                        if (currentCost >= bestCost) {
                            this.bestCost = currentCost;
                        }
                    } else {
                        if (currentCost > bestCost) {
                            this.bestCost = currentCost;
                        } else {
                            t1.addExam(e);
                            t2.removeExam(e);
                        }

                    }

                } else {
                    if (!tabuList.contains(t2.getTimesoltID() + "-" + t1.getTimesoltID() + "-" + e)) {
                        tabuList.removeLast();
                        tabuList.addFirst(t2.getTimesoltID() + "-" + t1.getTimesoltID() + "-" + e);
                        if (currentCost >= bestCost) {
                            this.bestCost = currentCost;
                        }
                    } else {
                        if (currentCost > bestCost) {
                            this.bestCost = currentCost;
                        } else {
                            t1.addExam(e);
                            t2.removeExam(e);
                        }

                    }
                }
            }
        }

    }

    double bestCost() {
        return this.bestCost;
    }

}
