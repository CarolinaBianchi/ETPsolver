/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lucie
 */
public class Student {

    private final String sId;
    private final List<Exam> exams = new ArrayList<>();
    private final List<Integer> examsIds = new ArrayList<>();

    public Student(String sId) {
        this.sId = sId;
    }
    
    public String getId(){
        return this.sId;
    }

    public void addExam(Exam sID) {
        this.exams.add(sID);
    }

    public void addExamId(int id) {
        this.examsIds.add(id);
    }

    public List<Integer> getExamsIds() {
        return this.examsIds;
    }

    public boolean checkExam(Exam sId) {
        return this.exams.contains(sId);
    }

    public boolean checkExamId(Integer id) {
        return this.examsIds.contains(id);
    }
    
    public List<Exam> getExams(){
        return this.exams;
    }

    @Override
    public String toString() {
        return this.sId;
    }

}
