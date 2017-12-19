/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.metaheuristics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import optimization.Cloner;
import optimization.Optimizer;
import optimization.domain.CostFunction;
import optimization.domain.Exam;
import optimization.domain.Schedule;
import optimization.domain.Timeslot;

/**
 *
 * @author roby
 */
public class IteratedLocalSearch extends SingleSolutionMetaheuristic {
    
    private Optimizer   optimizer;
    private Schedule    initSolution;
    private Schedule    currentSolution;
    private Schedule    currentBestSolution;
    private double      initCost;
    private double      currentBestCost;
    private int         numIteration = 1000;
    private long        elapsedTime;
    private final long  MINUTES = 2;
    private final long  MAX_MILLIS = MINUTES * 60 * 1000;
    private Random      random = new Random();
    private int         tmax;
    
    public IteratedLocalSearch(Optimizer optimizer, Schedule initSolution) {
        super(optimizer, initSolution);
        this.optimizer = optimizer;
        this.initSolution = Cloner.clone(initSolution);
        this.currentSolution = initSolution;
        this.currentBestSolution = currentSolution;
        this.initCost =  CostFunction.getCost(initSolution);
        this.currentBestCost = this.initCost;
        this.tmax = initSolution.getTmax();
    }
    
    @Override
    void improveInitialSol() {
        
        long startTime = System.currentTimeMillis();
        
        localSearch();
        while (elapsedTime < MAX_MILLIS) {
            disturbance();
            elapsedTime = System.currentTimeMillis() - startTime;
        }
        
        mySolution = currentBestSolution;
        System.out.println("IteratedLocalSearch:" + (int)currentBestSolution.getCost() + "\t");
    }
    
    public void localSearch() {
        
        int sourceTimeslot, numExams, examMovePenalty;
        boolean first, last, bestFound;
        Exam exam;
        
        first = last = bestFound = false;
        
        for (int i=0; (i<tmax) && (!bestFound); i++) {
            
            sourceTimeslot = random.nextInt(tmax); //***
            if (!currentSolution.getTimeslot(sourceTimeslot).isFree()) {
                if (sourceTimeslot == 0)         
                    first = true; 
                else if (sourceTimeslot == tmax-1)  
                    last = true; 
                
                numExams = currentSolution.getTimeslot(sourceTimeslot).getNExams();
                
                for (int j=0; (j<numExams) && (!bestFound); j++) {
                    
                    exam = currentSolution.getTimeslot(sourceTimeslot).getRandomExam(); //***
                    
                    if (!last) {
                        examMovePenalty = CostFunction.getExamMovePenalty(exam, sourceTimeslot, sourceTimeslot+1, currentSolution);
                        if (examMovePenalty < 0) {
                            currentSolution.move(exam, sourceTimeslot, sourceTimeslot+1);
                            if (currentSolution.getCost() < currentBestSolution.getCost()) {
                                currentBestSolution = Cloner.clone(currentSolution);
                                bestFound = true;
                                System.out.println("New currentSolution cost:" + (int)currentBestSolution.getCost() + "\t");
                                break;
                            }
                        }
                    }
                    if (!first) {
                        examMovePenalty = CostFunction.getExamMovePenalty(exam, sourceTimeslot, sourceTimeslot-1, currentSolution);
                        if (examMovePenalty < 0) {
                            currentSolution.move(exam, sourceTimeslot, sourceTimeslot-1);
                            if (currentSolution.getCost() < currentBestSolution.getCost()) {
                                currentBestSolution = Cloner.clone(currentSolution);
                                bestFound = true; 
                                System.out.println("New currentSolution cost:" + (int)currentBestSolution.getCost() + "\t");
                                break;
                            }
                        }
                    }
                } 
            }
        }
    }
    
    public void disturbance() {
        for (int i=0; i<numIteration; i++) {
            currentSolution.randomMove();
            currentSolution.randomSwap();
        }
        localSearch();
    }
    
}
