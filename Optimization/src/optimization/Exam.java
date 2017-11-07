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
    
    private Integer numStudents;
    private Integer id;
    private List<Student> students=new LinkedList<Student>(); 
    
    public Exam(Integer examId,Integer numStudents){
        this.id=examId;
        this.numStudents=numStudents;
    }
    
    public int getId(){
        return this.id;
    }
    
    public int getNumStudents(){
        return this.numStudents;
    }
    
    public void  addStutent(Student sID){
        this.students.add(sID);
    }
    
    public boolean checkStudent(Student sId){
        return this.students.contains(sId);
        
    }
    
    @Override
    public String toString(){
        return this.id+" "+this.numStudents;
    }
    
}
