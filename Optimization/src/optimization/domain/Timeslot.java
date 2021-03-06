/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Class that represents a Timeslot.
 *
 * @author Carolina Bianchi
 */
public class Timeslot implements Cloneable {

    private List<Exam> exams;
    private int numCollisions;
    private int timeslotID;

    public Timeslot(int timeslotID) {
        this.exams = new ArrayList<>();
        this.timeslotID = timeslotID;
    }

    public List<Exam> getExams() {
        return this.exams;
    }

    public int getTimeslotID() {
        return this.timeslotID;
    }

    public int getCollisions() {
        return this.numCollisions;
    }

    public void setCollisions(int n) {
        this.numCollisions = n;
    }

    public void setTimeslotID(int tID) {
        this.timeslotID = tID;
    }

    public Exam getExam(int i) {
        return this.exams.get(i);
    }

    public Exam getRandomExam() {
        if (exams.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return this.exams.get(random.nextInt(exams.size()));
    }

    public void addExam(Exam e) {
        if (e != null) {
            this.exams.add(e);
        }
    }

    public void removeExam(Exam e) {
        this.exams.remove(e);
    }

    /**
     * Force an exam to be scheduled in this time slot;
     *
     * @param toBePlaced The exam we want to force in this time slot.
     */
    public void forceExam(Exam toBePlaced) {
        int collisionsGenerated = 0;

        for (Exam e : this.exams) {
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
        if (isFree()) {
            return true;
        }
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

    /**
     * Counts the weight of the conflict between an exam with a certain set of
     * conflicting exams (represented by the conflictWeights) and this timeslot.
     * [TO DO!!!!]
     *
     * @param conflictWeights
     * @return
     */
    public int conflictWeight(Map<Integer, Integer> conflictWeights) {
        int count = 0;
        Integer commonStudents;
        for (Exam examHere : this.exams) {
            if ((commonStudents = conflictWeights.get(examHere.getId())) != null) {
                count += commonStudents;
            }
        }
        return count;
    }

    /**
     * Returns the number of exams in the timeslot.
     *
     * @return
     */
    public int getNExams() {
        return this.exams.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Timeslot other = (Timeslot) obj;
        if (!Objects.equals(this.exams, other.exams)) {
            return false;
        }
        return true;
    }

    /**
     * Because an exam is in one and only one timeslot, the timeslot can be
     * identified by the id of its first exam univocally.
     *
     * @return
     */
    @Override
    public int hashCode() {
        if (this.exams.isEmpty()) {
            return 0;
        }
        return this.exams.get(0).getId() + 1;
    }

    @Override
    public Timeslot clone() {
        Timeslot t = new Timeslot(timeslotID);
        for (Exam e : this.exams) {
            t.addExam(e.clone());
        }
        t.setCollisions(this.numCollisions);
        return t;
    }

    /**
     * Returns true if the exam <code>e</code> is assigned to the timeslot
     *
     * @param e
     * @return
     */
    public boolean contains(Exam e) {
        return this.exams.contains(e);
    }
    
    /**
     * Returns true if the exam  ID <code>eID</code> is assigned to the timeslot
     * @param eID
     * @return 
     */
    public boolean contains(int eID){
       for(Exam e:exams){
           if(e.getId()==eID){
               return true;
           }
       }
       return false;
    }

    /**
     * Removes all the exams from the timeslot
     */
    public void clean() {
        this.exams = new ArrayList<>();
    }

    /**
     * Adds a list of exams to the timeslot
     *
     * @param newExams
     */
    public void addExams(List<Exam> newExams) {
        this.exams.addAll(newExams);
    }

    /**
     * Removes all exams and adds the list of <code>newExams</code> to the
     * timeslot
     *
     * @param newExams
     */
    public void cleanAndAddExams(List<Exam> newExams) {
        clean();
        addExams(newExams);
    }
    
    /**
     * For each exam in <code>toInsert</code>, if the timeslot doesn't contain it and 
     * it is compatible with it, it assign the exam to the timeslot and it also add it
     * to the list of positioned exams.
     * @param toInsert
     * @return 
     */
    public List<Exam> tryInsertExams(List<Exam> toInsert){
        List<Exam> positioned=new ArrayList<>();
        for(Exam e:toInsert){
            if(!contains(e) && isCompatible(e)){
                addExam(e);
                positioned.add(e);
            }
        }
        return positioned;
    }
    
    /**
     * Returns the Exam with ID <code>eId</code> 
     * @param eId
     * @return 
     */
    public Exam getExamByID(int eId){
        for(Exam e:exams){
            if(e.getId()==eId){
                return e;
            }
        }
        return null;
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
