/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author lucie
 */
public class Student {
    private String sId;
    private List<Exam> exams=new LinkedList<>();
    
    public Student(String sId){
        this.sId=sId;
    }
   
    public void  addExam(Exam sID){
        this.exams.add(sID);
    }
    
    public boolean checkExam(Exam sId){
        return this.exams.contains(sId);
        
    }
    
    @Override
     public String toString(){
        return this.sId;
    }
    
}
