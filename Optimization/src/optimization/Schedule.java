/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import java.util.Random;

/**
 * Class that represents a Schedule.
 * @author Carolina Bianchi
 */
public class Schedule {

    private Timeslot[] timeslots;
    private double cost; // to be defined

    public Schedule(int tmax) {
        timeslots = new Timeslot[tmax];
        initTimeslots();
    }

    private void initTimeslots() {
        for (int i = 0; i < timeslots.length; i++) {
            timeslots[i] = new Timeslot();
        }
    }

    public Timeslot[] getTimeslots() {
        return this.timeslots;
    }

    public int getTmax() {
        return this.timeslots.length;
    }

    public Timeslot getTimeslot(int i) {
        return timeslots[i];
    }

    public Timeslot getRandomTimeslot() {
        Random rnd = new Random();
        return timeslots[rnd.nextInt(timeslots.length)];
    }
    
    /**
     * Returns the number of timeslots currently in this schedule.
     * @return 
     */
    public int getNExams() {
        int counter = 0;
        for (Timeslot t : timeslots) {
            counter += t.getExams().size();
        }
        return counter;
    }

    void clear() {
        initTimeslots();
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < timeslots.length; i++) {
            s += "Timeslot " + i + timeslots[i].toString() + "\n";
        }
        return s;
    }

}
