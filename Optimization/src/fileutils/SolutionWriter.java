/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fileutils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import optimization.domain.Exam;
import optimization.Optimization;
import optimization.domain.Schedule;
import optimization.domain.Timeslot;

/**
 * Writes the solution found on a new thread.
 *
 * @author Carolina Bianchi
 */
public class SolutionWriter {

    private final Schedule schedule;

    public SolutionWriter(Schedule schedule) {
        this.schedule = schedule;
    }

    /**
     * Writes the schedule in a file which path is files/instancename/.sol.
     *
     */
    public void writeSolution() {
        //saveStat();   //Uncomment if you want to save the statistics
        try (PrintWriter writer = new PrintWriter(new FileWriter("" + Optimization.instance + ".sol"))) {
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

    /**
     * Logs the gap of the current solution in a file.
     */
    private void saveStat() {
        //Uncomment if you want to save the statistics. you also have to uncomment the benchmarks in the main
        /*try {
            int benchmark = Optimization.BENCHMARKS.get(Optimization.instance);
            double gap = 100 * ((schedule.getCost() * 1.0 - benchmark * 1.0) / benchmark * 1.0);
            String line = "\n" + gap;
            Files.write(Paths.get("files/" + Optimization.instance + "Stats.txt"), line.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }
}
