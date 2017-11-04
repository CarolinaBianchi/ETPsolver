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
public class Exam {
    
    private Integer numStudent;
    private Integer examId;
    private List<Student> students=new LinkedList<Student>(); 
    
    public Exam(Integer examId,Integer numStudent){
        this.examId=examId;
        this.numStudent=numStudent;
    }
    
    public void  addStutent(Student sID){
        this.students.add(sID);
    }
    
    public boolean checkStudent(Student sId){
        return this.students.contains(sId);
        
    }
    
    @Override
    public String toString(){
        return this.examId+" "+this.numStudent;
    }
    
}
