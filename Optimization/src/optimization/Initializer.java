/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import java.util.Collections;
import java.util.List;

/**
 * Class that manages the operation to find a suitable initial solution for the
 * algorithm.
 *
 * @author Carolina Bianchi
 */
public class Initializer {

    public void run(List<Exam> exams, Schedule schedule) throws Exception {
        Collections.sort(exams);
        Timeslot[] timeslots = schedule.getTimeslots();
        for (Exam e : exams) {
            int i = 0;
            while (!e.isPlaced()) {
                if (checkCompatibility(e, timeslots[i])) {
                    timeslots[i].addExam(e);
                    e.setPlaced(true);
                }
                i++;
                if(i== schedule.getTimeslots().length){
                    throw new Exception(); // need to shuffle the timetable
                }
            }
        }
        System.out.println(schedule);
    }

    private boolean checkCompatibility(Exam exam, Timeslot t) {
        boolean compatible = true;
        List<Exam> alreadyIn = t.getExams();
        for (Exam conflictingE : exam.getConflictingExams()) {
            compatible &= !alreadyIn.contains(conflictingE);
            if(!compatible){
                return compatible;
            }
        }
        return true;
    }
    
}
