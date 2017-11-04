/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Elisa
 */
public class Optimization {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
         Map<Integer,Exam> exams=new HashMap<>();
         Map<String,Student> students=new TreeMap<>();
         int timeSlot;
          
       // System.out.println("insert the number of the instance");
       
        
      /*
   reading of exam file
   */ 
          
   try (                
    BufferedReader in = new BufferedReader(new FileReader("instance01.exm"));)
{
          String line;
          while ((line = in.readLine()) != null) {  
               String[] e=line.split(" ");
               int examId=Integer.parseInt(e[0]);       
               int nStudent=Integer.parseInt(e[1]);
               Exam ex=new Exam(examId,nStudent);
               exams.put(examId, ex);
               System.out.println(ex);
    
          }
} catch(IOException e) {System.out.println("error in reading exm file"+e.getMessage());}
   
 /*
   reading of student file
   */ 
   try (BufferedReader in = new BufferedReader(new FileReader("instance01.stu"));)
{
      String line;
      while ((line = in.readLine()) != null) {   
   
           String[] e=line.split(" ");
           String sId=e[0];       
           Student st=new Student(sId);
           st.addExam(exams.get(Integer.parseInt(e[1])));
           exams.get(Integer.parseInt(e[1])).addStutent(st);
           students.put(sId, st);
      
           System.out.println(st);
      }
} catch(IOException e) {System.out.println("error in reading stu file"+e.getMessage());}
  
   
   /*
   reading of timeSlot file
   */ 
   
    try (BufferedReader in = new BufferedReader(new FileReader("instance01.slo"));)
{
      String line;
      line = in.readLine(); 
      if(    !line.trim().isEmpty()) {
          int t=Integer.parseInt(line.trim());
          timeSlot=t;System.out.println("number of timeSlot:"+timeSlot);
             } else {
                   System.out.println("errore during timeslot reading");
             }
      
          
      
} catch(IOException e) {System.out.println("error in reading stu file"+e.getMessage());}
    
    }
    
}
