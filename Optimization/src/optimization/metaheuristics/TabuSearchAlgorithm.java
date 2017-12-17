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
    private int numberOfIterarion = 100000;
    //private Schedule bestSchedule;
    private double initialCost;
    private int tabuListsize = 500;

    public TabuSearchAlgorithm(Optimizer optimizer, Schedule initialSchedule) {
        super(optimizer, initialSchedule);
        //this.mySchedule=mySchedule;
        this.tabuList = new LinkedList<>();
        this.optimizer = optimizer;
        this.initialSchedule = initialSchedule;
        initSolution.setCost(CostFunction.getCost(initSolution));
        this.initialCost = CostFunction.getCost(initialSchedule);
        System.out.println(initialCost);
    }

    @Override
    void improveInitialSol() {
        mySolution = algorithm1();
    }
    
    /** this method implement the tabu search algorithm 
     * firstly it generate two random timeslot from the initialSchedule, which one is the sourse the other is the destination
     * and then from the source timeslot it randomly chooses one exam which will be move into the destination timeslot if there are compatible
     * 
     * @return return the best schedule found
     */

    public Schedule algorithm1() {
        Schedule bestSchedule = this.initialSchedule;
        Schedule currentSchedule = bestSchedule;
        double currentBestCost = this.initialCost;
        double currentCost;
        System.out.println(currentBestCost+"  "+this.initialSchedule.getCost());
        for (int i = 0; i < numberOfIterarion; i++) {

            Timeslot source = currentSchedule.getRandomTimeslot();   // source Timeslot
            Timeslot dest = currentSchedule.getRandomTimeslot();   // destination Timeslot
            Exam ex = source.getRandomExam();
            if (source.getTimeslotID() != dest.getTimeslotID() && ex!=null && currentSchedule.move(ex, source, dest)) { 
                currentCost=currentSchedule.getCost();
                if(currentCost < currentBestCost){
                    currentBestCost=currentCost;
                    bestSchedule=currentSchedule;
                    if(tabuList.size() < tabuListsize){
                        if (!tabuList.contains(source.getTimeslotID() + "-" + dest.getTimeslotID() + "-" + ex)) {
                            /**
                             * I insert in the tabuList a String containing information about the 
                             * move I have to prevent
                             */
                        tabuList.addFirst(dest.getTimeslotID() + "-" + source.getTimeslotID() + "-" + ex); 
                        }
                    }else{
                        if (!tabuList.contains(source.getTimeslotID() + "-" + dest.getTimeslotID() + "-" + ex)) {
                        tabuList.removeLast();
                        tabuList.addFirst(dest.getTimeslotID() + "-" + source.getTimeslotID() + "-" + ex);
                        }
                   }
                }else{
                 
                    currentSchedule.move(ex, dest, source);
                }
                System.out.println("currentCost  " + currentCost + "  bestCost   " + currentBestCost);
            }
        }
        return bestSchedule;
    }
    
    /** this method implement the tabu search algorithm second version 
     * firstly it generate two random timeslots from the initialSchedule, which one is the sourse the other the destination
     * and then from the source timeslot it randomly chooses one exam(ex1) it does the same for the second timeslot to obtain an exam(ex2)
     * next, it swaps the 2 exams into the timeslots
     * 
     * @return return the best schedule found
     */
    
     public Schedule algorithm2() {
        Schedule bestSchedule = this.initialSchedule;
        Schedule currentSchedule = bestSchedule;
        double currentBestCost = this.initialCost;
        double currentCost;
        System.out.println(currentBestCost+"  "+this.initialSchedule.getCost());
        for (int i = 0; i < numberOfIterarion; i++) {

            Timeslot source = currentSchedule.getRandomTimeslot();   // source Timeslot
            Timeslot dest = currentSchedule.getRandomTimeslot();   // destination Timeslot
            Exam ex1 = source.getRandomExam();
            Exam ex2 = source.getRandomExam();
            
            if (source.getTimeslotID() != dest.getTimeslotID() && currentSchedule.checkFeasibleSwap(source, ex1, dest, ex2)) {  
                currentSchedule.swap(source, ex1, dest, ex2);
                currentCost=currentSchedule.getCost();
                if(currentCost < currentBestCost){
                    currentBestCost=currentCost;
                    bestSchedule=currentSchedule;
                    if(tabuList.size() < tabuListsize){
                        //  I insert in the tabuList a String containing information about the swap I have to prevent 
                       if (!tabuList.contains(source.getTimeslotID()+"-"+ex1+"-"+ dest.getTimeslotID()+"-"+ ex2)) {
                        tabuList.addFirst(dest.getTimeslotID()+"-"+ ex1 +"-"+source.getTimeslotID()+"-" + ex2);
                        }
                                                   
                    }else{
                        if (!tabuList.contains(source.getTimeslotID()+"-"+ex1+"-"+ dest.getTimeslotID()+"-"+ ex2)) {
                        tabuList.removeLast();
                        tabuList.addFirst(dest.getTimeslotID()+"-"+ ex1 +"-"+source.getTimeslotID()+"-" + ex2);
                        }
                   }
                }else{
                   currentSchedule.swap(dest, ex1, source, ex2);
                }
               
                System.out.println("currentCost  " + currentCost + "  bestCost   " + currentBestCost);
            }
        }
        return bestSchedule;
    }
     
     
     
    
  /*  private boolean checkFeasibleSwap(Timeslot t1, Exam ex1, Timeslot t2, Exam ex2) {
        return checkSwap(t1, ex1, ex2) && checkSwap(t2, ex2, ex1);
    }
  */
    /**
     * Checks the feasibility of swapping Exam ex1 (which is in timeslot t1)
     * whith Exam ex2.
     *
     * @param t1
     * @param ex1
     * @param ex2
     * @return
     */
 /*   private boolean checkSwap(Timeslot t1, Exam ex1, Exam ex2) {
        t1.removeExam(ex1);
        boolean compatible = t1.isCompatible(ex2);
        t1.addExam(ex1);
        return compatible;
    }
*/
    /**
     * Swaps 2 exams.
     *
     * @param t1 source timeslot of exam ex1
     * @param ex1
     * @param t2 source timeslot of exam ex2
     * @param ex2
     */
   /**/
     
    /**
     * Swaps 1 exams.
     *
     * @param source source timeslot of exam ex
     * @param dest  destination timeslot
     * @param ex2
     */
  /*   private void swapOneExam(Timeslot source, Timeslot dest, Exam ex,Schedule schedule) {
       schedule.move(ex, source, dest);
    }*/
     
     /** this method implement the tabu search algorithm 
     * firstly it denerate two random timeslot from the initialSchedule which one is the sourse(t1) the other is the destination(t2)
     * and then from the source timeslot I randomly choose one exam which will be swap into the destination timeslot if there are compatible
     * 
     * @return return the best schedule found
     */

  /*  public Schedule algorithm1bis() {
        Schedule bestSchedule = this.initialSchedule;
        double currentBestCost = this.initialCost;
        System.out.println(currentBestCost);
        for (int i = 0; i < numberOfIterarion; i++) {
            Schedule currentSchedule = bestSchedule;
            double currentCost;
            Timeslot t1 = currentSchedule.getRandomTimeslot();   // source Timeslot
            Timeslot t2 = currentSchedule.getRandomTimeslot();   // dest Timeslot
            Exam e = t1.getRandomExam();
            if (t1.getTimeslotID() != t2.getTimeslotID() && e!=null && currentSchedule.move(e, t1, t2)) {  
                //currentCost = CostFunction.getCost(currentSchedule);
                currentCost=currentSchedule.getCost();
               
                if (tabuList.size() < tabuListsize) {
                    if (!tabuList.contains(t1.getTimeslotID() + "-" + t2.getTimeslotID() + "-" + e)) {
                        tabuList.addFirst(t2.getTimeslotID() + "-" + t1.getTimeslotID() + "-" + e);
                        if (currentCost < currentBestCost) {
                            currentBestCost = currentCost;
                        }
                    } else {
                        if (currentCost < currentBestCost) {
                            currentBestCost = currentCost;
                        } else {
                            t1.addExam(e);
                            t2.removeExam(e);
                        }

                    }

                } else {
                    if (!tabuList.contains(t1.getTimeslotID() + "-" + t2.getTimeslotID() + "-" + e)) {
                        tabuList.removeLast();
                        tabuList.addFirst(t2.getTimeslotID() + "-" + t1.getTimeslotID() + "-" + e);
                        if (currentCost <= currentBestCost) {
                            currentBestCost = currentCost;
                        }
                    } else {
                        if (currentCost < currentBestCost) {
                            currentBestCost = currentCost;
                        } else {
                            t1.addExam(e);
                            t2.removeExam(e);
                        }

                    }
                }

                System.out.println("currentCost  " + currentCost + "  bestCost   " + currentBestCost);
            }
        }
        return bestSchedule;
    }*/
     
     /*
     public Schedule algorithm2bis() {
        Schedule bestSchedule = this.initialSchedule;
        double cost = this.initialCost;
        System.out.println(cost);
        for (int i = 0; i < numberOfIterarion; i++) {
            Schedule currentSchedule = bestSchedule;
            double currentCost;
            Timeslot t1 = currentSchedule.getRandomTimeslot();   // source Timeslot
            Timeslot t2 = currentSchedule.getRandomTimeslot();   // dest Timeslot
            Exam e = t1.getRandomExam();
            Exam e2 =t2.getRandomExam();
            
            if (t1.getTimeslotID() != t2.getTimeslotID() && e!=null && this.checkFeasibleSwap(t1, e, t2, e2) ) {    
                this.swap(t1, e, t2, e2,currentSchedule);
                currentCost=currentSchedule.getCost();

                if (tabuList.size() < tabuListsize) {
                  //  String str=t1.getTimeslotID()+"-"+ e.getId() + t2.getTimeslotID() + e2.getId();
                    if (!tabuList.contains(t1.getTimeslotID()+"-"+e+"-"+ t2.getTimeslotID()+"-"+ e2)) {
                        tabuList.addFirst(t2.getTimeslotID()+"-"+ e +"-"+t1.getTimeslotID()+"-" + e2);
                        if (currentCost < cost) {
                            cost = currentCost;
                        }
                    } else {
                        if (currentCost < cost) {
                            cost = currentCost;
                        } else {
                          //  t1.addExam(e);
                          //  t2.removeExam(e);
                            this.swap(t2, e2, t1, e, currentSchedule);
                        }

                    }

                } else {
                    if (!tabuList.contains(t1.getTimeslotID()+"-"+e+"-"+ t2.getTimeslotID()+"-"+ e2)) {
                        tabuList.removeLast();
                        tabuList.addFirst(t2.getTimeslotID()+"-"+ e +"-"+t1.getTimeslotID()+"-" + e2);
                        if (currentCost <= cost) {
                            cost = currentCost;
                        }
                    } else {
                        if (currentCost < cost) {
                            cost = currentCost;
                        } else {
                          //  t1.addExam(e);
                          //  t2.removeExam(e);
                            this.swap(t2, e2, t1, e, currentSchedule);
                        }

                    }
                }

                System.out.println("currentCost  " + currentCost + "  bestCost   " + cost);
            }
        }
        return bestSchedule;
    }
     */
     
     
    
}
