/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import fileutils.SolutionWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import optimization.domain.Schedule;

/**
 * This class checks if the solution provided is the best EVER found. (A new
 * folder ("best") inside the files folder is created, where the current best
 * solutions are saved. NB these solution are not committed, so each of us will
 * have a .sol in this folder only if <b>he</b> found that solution. DO NOT
 * THROW THEM AWAY!). !!!!!!!!!!!! NB!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! (This
 * functionality can be used only on the public instances, to use it uncomment
 * the benchmarks in the main)
 *
 * @author Carolina Bianchi
 */
public class AbsoluteBestChecker {

    private static final String BEST_PATH = "files/best/currentBest.txt";
    private static Map<String, Integer> currentBest;

    /**
     * Checks if the solution found is the best ever. If it is the case, writes
     * it into the folder files/best and updates the file
     * files/best/currentBest.txt.
     *
     * @param mySolution
     */
    public static void checkIfBestEver(Schedule mySolution) {
        /*loadCurrentBest();
        int myCost = mySolution.getCost();
        if (currentBest.get(Optimization.instance) == null || myCost < currentBest.get(Optimization.instance)) {
            dreamBig(mySolution);
            System.out.println("You found the best solution ever found for this instance! \n (Old best:" + currentBest.get(Optimization.instance) + ")");
            currentBest.put(Optimization.instance, myCost);
            Optimization.instance = "best/" + Optimization.instance; // Dirty fix but anyways
            (new SolutionWriter(mySolution)).writeSolution();
            updateCurrentBestFile();

        }*/
    }

    /**
     * Reads the file at <code>BEST_PATH</code>: each line of the file
     * represents the best solution found for a given instance. (Each line
     * should be in the format instanceName \t currentBestObjFun).
     */
    private static void loadCurrentBest() {
        /*try (BufferedReader in = new BufferedReader(new FileReader(BEST_PATH))) {
            String line;
            String[] tokens;
            currentBest = new HashMap<>();
            while ((line = in.readLine()) != null) {
                tokens = line.split("\t");
                currentBest.put(tokens[0], Integer.parseInt(tokens[1]));
            }
        } catch (IOException ex) {
            Logger.getLogger(AbsoluteBestChecker.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    /**
     * Writes the updated file with the real current best results on each
     * instance.
     */
    private static void updateCurrentBestFile() {
        /*String line;
        int benchmark;
        double gap;
        try (PrintWriter writer = new PrintWriter(new FileWriter(BEST_PATH))) {
            for (Map.Entry<String, Integer> entry : currentBest.entrySet()) {
                benchmark = Optimization.BENCHMARKS.get(entry.getKey());
                line = entry.getKey() + "\t" + entry.getValue();
                gap = 100 * ((entry.getValue() * 1.0 - benchmark * 1.0) / benchmark * 1.0);
                line += "\t" + gap + "%";
                writer.println(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(AbsoluteBestChecker.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    /**
     * You never know.... checks if the solution is better than the benchmarks
     * (lol)
     *
     * @param mySolution
     */
    private static void dreamBig(Schedule mySolution) {
        /*if (Optimization.BENCHMARKS.get(Optimization.instance) == null) {
            System.out.println("Sorry we don't have a benchmark for this instance");
        } else if (mySolution.getCost() < Optimization.BENCHMARKS.get(Optimization.instance)) {
            System.out.println("BIG MONEY!!!! WE HAVE A CAREER IN SCHEDULING WOW BETTER THAN THE BENCHMARKS!");
        }*/
    }

}
