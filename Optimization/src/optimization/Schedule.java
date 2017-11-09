/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

/**
 *
 * @author Carolina Bianchi
 */
public class Schedule {

    private Timeslot[] timeslots;

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

    public Timeslot getTimeslot(int i) {
        return timeslots[i];
    }

    @Override
    public String toString() {
        String s = "";
        for (int i =0; i<timeslots.length; i++) {
            s += "Timeslot "+i+timeslots[i].toString()+"\n";
        }
        return s;
    }

}
