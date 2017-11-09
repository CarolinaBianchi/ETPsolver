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
 * @author Carolina Bianchi
 */
public class Timeslot {

    List<Exam> exams;

    public Timeslot(){
        this.exams = new ArrayList<>();
    }
    public List<Exam> getExams() {
        return this.exams;
    }

    public void addExam(Exam e) {
        this.exams.add(e);
    }

    public void removeExam(Exam e) {
        this.exams.remove(e);
    }
    
    public String toString(){
        String s="";
        
        for(Exam e: this.exams){
            s+=e.toString()+"\t";
        }
        
        return s;
    }

}
