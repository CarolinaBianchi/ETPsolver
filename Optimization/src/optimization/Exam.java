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
public class Exam extends ProxyExam implements Comparable<Exam>, Cloneable {

    private List<Student> students = new ArrayList<>();
    // 2 exams are "conflicting" if at least one student is enrolled in both of 
    // them.(-> can't be placed in the same timeslot)
    private final Set<ProxyExam> conflictingExams = new HashSet<>();

    public Exam(int id, int numStudents) {
        super(id, numStudents);
    }

    public void addStutent(Student sID) {
        this.students.add(sID);
    }

    public boolean checkStudent(Student sId) {
        return this.students.contains(sId);
    }

    public void addConflictingExam(ProxyExam e) {
        this.conflictingExams.add(e);
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public Set<ProxyExam> getConflictingExams() {
        return this.conflictingExams;
    }

    /**
     * Tells if this exam is compatible (i.e. not conflicting) with Exam
     * <code> ex2</code>.
     *
     * @param e2
     * @return true if the two exams are not conflicting, false otherwise.
     */
    public boolean isCompatible(ProxyExam e2) {
        return !this.conflictingExams.contains(e2);
    }

    @Override
    public Exam clone() {
        Exam clone = new Exam(id, numStudents);
        for (ProxyExam e : this.conflictingExams) {
            clone.addConflictingExam(new ProxyExam(e));
        }
        // Since it's useless for now... if we had to read it a method to clone students should be provided.
        clone.setStudents(this.students);
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

}
