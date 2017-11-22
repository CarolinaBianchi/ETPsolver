/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an exam.
 *
 * @author lucie
 */
public class Exam implements Comparable<Exam>, Cloneable {

    //private List<Student> students = new ArrayList<>();
    // 2 exams are "conflicting" if at least one student is enrolled in both of 
    // them.(-> can't be placed in the same timeslot)
    private final Set<Integer> conflictingExams = new HashSet<>();
    private final Integer numStudents;
    private final Integer id;

    public Exam(int id, int numStudents) {
        this.id = id;
        this.numStudents = numStudents;
    }
    
    public int getNumStudents() {
        return this.numStudents;
    }
 
    public int getId() {
        return this.id;
    }

    /*public void addStudent(Student sID) {
        this.students.add(sID);
    }

    public boolean checkStudent(Student sId) {
        return this.students.contains(sId);
    }*/

    public void addConflictingExam(int eId) {
        this.conflictingExams.add(eId);
    }

    /*public void setStudents(List<Student> students) {
        this.students = students;
    }*/

    public Set<Integer> getConflictingExams() {
        return this.conflictingExams;
    }

    /**
     * Tells if this exam is compatible (i.e. not conflicting) with Exam
     * <code> eId</code>.
     *
     * @param eId
     * @return true if the two exams are not conflicting, false otherwise.
     */
    public boolean isCompatible(int eId) {
        return !this.conflictingExams.contains(eId);
    }

    @Override
    public Exam clone() {
        Exam clone = new Exam(id, numStudents);
        for (int eId : this.conflictingExams) {
            clone.addConflictingExam(eId);
        }
        // Since it's useless for now... if we had to read it a method to clone students should be provided.
        //clone.setStudents(this.students);
        return clone;
    }

    @Override
    public int compareTo(Exam o) {
        return this.conflictingExams.size() - o.getConflictingExams().size();
    }

    @Override
    public String toString() {
        return "Ex" + this.id + " ";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Exam) {
            return ((Exam) obj).getId() == this.getId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + Objects.hashCode(this.id);
        return hash;
    }
}
