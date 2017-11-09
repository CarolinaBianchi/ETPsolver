/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author lucie
 */
public class Exam implements Comparable<Exam>{
    
    private Integer numStudents;
    private Integer id;
    private List<Student> students=new LinkedList<>(); 
    private Set<Exam> conflictingExams = new HashSet<>();
    boolean placed;
    
    
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
    
    public void addConflictingExam(Exam e){
        this.conflictingExams.add(e);
    }
    
    public Set<Exam> getConflictingExams(){
        return this.conflictingExams;
    }
    
    public void setPlaced(boolean placed){
        this.placed = placed;
    }
    
    public boolean isPlaced(){
        return this.placed;
    }
    
    @Override
    public String toString(){
        return "Ex"+this.id+" ";
    }

    @Override
    public int compareTo(Exam o) {
        return this.conflictingExams.size()-o.getConflictingExams().size();
    }
    
}
