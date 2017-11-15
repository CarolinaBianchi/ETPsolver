/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import java.util.List;

/**
 * Defines the general structure of an initializer. This class extends the class
 * Thread, so that more than 1 initializer can be run at the same time.
 *
 * @author Carolina Bianchi
 */
public abstract class AbstractInitializer extends Thread {

    protected List<Exam> exams;
    protected List<Schedule> schedules;
    protected Schedule mySchedule;

    public AbstractInitializer(List<Exam> exams, List<Schedule> schedules, int tmax) {
        this.exams = exams;
        this.schedules = schedules;
        this.mySchedule = new Schedule(tmax);

    }

    @Override
    public void run() {
        initialize();
        schedules.add(mySchedule);
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

    @Override
    public String toString() {
        return mySchedule.getNExams() + " out of " + exams.size() + " placed";
    }
}
