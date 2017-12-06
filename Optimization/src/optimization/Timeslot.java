/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Class that represents a Timeslot.
 *
 * @author Carolina Bianchi
 */
public class Timeslot implements Cloneable{

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
    
    public void setCollisions(int n){
        this.numCollisions = n;
    }

    public Exam getExam(int i) {
        return this.exams.get(i);
    }

    public Exam getRandomExam() {
        if(exams.isEmpty()){
            return null;
        }
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
    public Timeslot clone(){
        Timeslot t = new Timeslot();
        for(Exam e : this.exams){
            t.addExam(e.clone());
        }
        t.setCollisions(this.numCollisions);
        return t;
    }
    
    //--------------------- Genetic algorithm----------------------------------
    
    public boolean contains(int eId){
        return this.exams.get(eId)!=null;
    }
    
    public int calcPenalty(int distance, Set<Integer> conflictingExams){
        int penalty=0;
        for(Exam e:exams){
            for (Iterator<Integer> it = conflictingExams.iterator(); it.hasNext();) {
                    if(it.next()==e.getId()){
                        penalty+=2^(5-distance)*e.getNumStudents();
                    }
                }
        }
        return penalty;
    }
    
    public void clean() {
        this.exams = new ArrayList<>();
    }
    
    public void addExams(List<Exam> newExams){
        this.exams.addAll(newExams);
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
