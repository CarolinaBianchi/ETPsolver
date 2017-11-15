/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fileutils;

import java.io.FileWriter;
import java.io.PrintWriter;
import optimization.Exam;
import optimization.Optimization;
import optimization.Schedule;
import optimization.Timeslot;

/**
 * Writes the solution found on a new thread.
 *
 * @author Carolina Bianchi
 */
public class SolutionWriter extends Thread {

    private final Schedule schedule;

    public SolutionWriter(Schedule schedule) {
        this.schedule = schedule;
    }

    @Override
    public void run() {
        writeSolution();
    }

    /**
     * Writes the schedule in a file which path is files/instancename/.sol.
     *
     */
    private void writeSolution() {

        try (PrintWriter writer = new PrintWriter(new FileWriter("files/" + Optimization.instance + ".sol"))) {
            Timeslot[] timeslots = schedule.getTimeslots();
            for (int i = 1; i < timeslots.length + 1; i++) {
                Timeslot t = timeslots[i - 1];
                for (Exam e : t.getExams()) {
                    writer.println(e.getId() + " " + i);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
