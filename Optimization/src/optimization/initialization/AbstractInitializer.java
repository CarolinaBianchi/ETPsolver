/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.initialization;

import fileutils.SolutionWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import optimization.domain.Exam;
import optimization.Optimizer;
import optimization.domain.Schedule;

/**
 * Defines the general structure of an initializer. This class extends the class
 * Thread, so that more than 1 initializer can be run at the same time.
 *
 * @author Carolina Bianchi
 */
public abstract class AbstractInitializer extends Thread {

    protected List<Exam> exams;
    protected Schedule mySchedule;
    private Optimizer optimizer;

    public AbstractInitializer(List<Exam> exams, int tmax, Optimizer optimizer) {
        this.exams = exams;
        this.optimizer=optimizer;
        this.mySchedule = new Schedule(tmax);
    }

    @Override
    public void run() {
        initialize();
        notifyNewInitialSolution();
    }
    
    private void notifyNewInitialSolution(){
        optimizer.updateOnNewInitialSolution(mySchedule);
    }

    /**
     * Computes a new schedule.
     *
     * @return
     */
    abstract public Schedule initialize();

    /**
     * Returns the percentage of exams that are currently placed in the
     * schedule.
     *
     * @return
     */
    protected double getPercPlaced() {
        return (double) mySchedule.getNExams() / (double) exams.size();
    }

    /**
     * Writes the schedule that was computated by the initializer. (WILL BE
     * DELETED).
     */
    protected void writeSolution() {

        SolutionWriter sw = new SolutionWriter(mySchedule);
        try {
            sw.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(FeasibleInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }
        sw.start();

    }

    @Override
    public String toString() {
        return mySchedule.getNExams() + " out of " + exams.size() + " placed";
    }
}
