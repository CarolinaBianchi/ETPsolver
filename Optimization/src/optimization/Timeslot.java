/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class that represents a Timeslot.
 *
 * @author Carolina Bianchi
 */
public class Timeslot {

    List<Exam> exams;
    private int numCollisions; 

    public Timeslot() {
        this.exams = new ArrayList<>();
    }

    public List<Exam> getExams() {
        return this.exams;
    }
    
    public int getCollisions() {
        return this.numCollisions;
    }

    public Exam getExam(int i) {
        return this.exams.get(i);
    }

    public Exam getRandomExam() {
        Random random = new Random();
        return this.exams.get(random.nextInt(exams.size()));
    }

    public void addExam(Exam e) {
        this.exams.add(e);
    }

    public void removeExam(Exam e) {
        this.exams.remove(e);
    }

    /**
     * Force an exam to be scheduled in this time slot;
     * @param toBePlaced The exam we want to force in this time slot.
     */
    public void forceExam(Exam toBePlaced) {
        int collisionsGenerated = 0;
        
        for( Exam e : this.exams ) {
            collisionsGenerated += toBePlaced.studentsInvolvedInCollision(e.getId());
        }
        
        this.exams.add(toBePlaced);
        this.numCollisions += collisionsGenerated;
    }

    /**
     * Tells if the timeslot is compatible with Exam <code>e</code>. i.e. if it
     * doesn't contain any of e's conflicting exams.
     *
     * @param e
     * @return
     */
    public boolean isCompatible(Exam e) {
        boolean compatible = true;
        for (Exam alreadyIn : exams) {
            compatible &= e.isCompatible(alreadyIn.getId());
            if (!compatible) {
                return compatible;
            }
        }
        return true;
    }
    
    /**
     * Tell if there are exams currently scheduled in this time slot
     *
     * @return TRUE - If the time slot is still free / FALSE - Otherwise.
     */
    public boolean isFree() {
        return exams.isEmpty();
    }

    @Override
    public String toString() {
        String s = "";
        for (Exam e : this.exams) {
            s += e.toString() + "\t";
        }
        return s;
    }

}
