/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents an exam.
 *
 * @author lucie
 */
public class Exam implements Comparable<Exam> {

    private final Integer numStudents;
    private final Integer id;
    private final List<Student> students = new ArrayList<>();
    // 2 exams are "conflicting" if at least one student is enrolled in both of 
    // them.(-> can't be placed in the same timeslot)
    private final Set<Exam> conflictingExams = new HashSet<>();

    public Exam(Integer examId, Integer numStudents) {
        this.id = examId;
        this.numStudents = numStudents;
    }

    public int getId() {
        return this.id;
    }

    public int getNumStudents() {
        return this.numStudents;
    }

    public void addStutent(Student sID) {
        this.students.add(sID);
    }

    public boolean checkStudent(Student sId) {
        return this.students.contains(sId);
    }

    public void addConflictingExam(Exam e) {
        this.conflictingExams.add(e);
    }

    public Set<Exam> getConflictingExams() {
        return this.conflictingExams;
    }

    /**
     * Tells if this exam is compatible (i.e. not conflicting) with Exam
     * <code> ex2</code>.
     *
     * @param e2
     * @return true if the two exams are not conflicting, false otherwise.
     */
    public boolean isCompatible(Exam e2) {
        return !this.conflictingExams.contains(e2);
    }

    @Override
    public int compareTo(Exam o) {
        return this.conflictingExams.size() - o.getConflictingExams().size();
    }

    @Override
    public String toString() {
        return "Ex" + this.id + " ";
    }

}
