/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that manages the program logic, exploiting other classes functions.
 *
 * @author Carolina Bianchi
 */
public class Optimizer {

    private List<Exam> exams;
    private List<Student> students;
    private Map<Integer, Exam> eIdExam;
    private Schedule schedule;
    private Initializer initializer;
    private int tmax;
    private final int S; // number of students
    private final int E; // number of exams
    private boolean[][] a; // a matrix: a[i][j] is true if student i is enrolled in exam j.

    public Optimizer(String instanceName) throws IOException {
        init(instanceName);
        S = students.size();
        E = exams.size();
        a = new boolean[S][E];
        initializer = new Initializer();
        schedule = new Schedule(tmax);
        buildStudentListInEx();
        //checkExamEnrollments(); //DEBUG
        buildConflictingExamsLists();
    }

    /**
     * Builds the attributes exams, students and tmax exploiting FileManager's
     * methods. HINT: We could run these 3 methods on 3 different threads.
     *
     * @param instanceName the name of the instance.
     */
    private void init(String instanceName) throws IOException {
        this.exams = FileManager.readExams(instanceName);
        buildEIdExam();
        this.students = FileManager.readStudents(instanceName);
        this.tmax = FileManager.readTimeslots(instanceName);
    }

    /**
     * Adds the students to the exams in which they're enrolled and ; builds the
     * matrix <code>a</code> accordingly.
     */
    private void buildStudentListInEx() {
        Student student;
        Exam exam;
        List<Integer> examsIds;
        for (int i = 0; i < students.size(); i++) {
            student = students.get(i);
            examsIds = student.getExamsIds();
            for (int eId : examsIds) {
                exam = eIdExam.get(eId);
                exam.addStutent(student);    //In the future one of this two lines may be useless
                student.addExam(exam);
                a[i][exams.indexOf(exam)] = true;
            }
        }
        //printA();
    }

    private void buildConflictingExamsLists() {
        List<Exam> sExams;
        Exam e1, e2;
        for (Student s : students) {
            sExams = s.getExams();
            //System.out.println(s.getExams().size());
            for (int i = 0; i < sExams.size(); i++) {
                e1 = sExams.get(i);
                for (int j = i + 1; j < sExams.size(); j++) {
                    e2 = sExams.get(j);
                    e2.addConflictingExam(e2);
                    e2.addConflictingExam(e1);
                }
                //System.out.println(e1.getConflictingExams().size());
            }
        }
    }

    /**
     * Builds eIdExam, which maps the examId with the exam itself.
     */
    private void buildEIdExam() {
        eIdExam = new HashMap<>();
        for (Exam e : exams) {
            eIdExam.put(e.getId(), e);
        }
    }

    /**
     * For debuggin purposes, can be later deleted: I check if the number of
     * "trues" in matrix a, in column corrisponding to the exam j, are equal to
     * the number of enrolled students declared in the exam file.
     */
    private void checkExamEnrollments() {
        int counter;
        boolean equal = true;
        for (int j = 0; j < a[0].length; j++) { // I cycle externally on exams
            counter = 0;
            for (int i = 0; i < a.length; i++) { // I sum the number of enrollments in that exam
                counter = (a[i][j]) ? counter + 1 : counter;
            }
            equal &= (counter == exams.get(j).getNumStudents());
        }
        System.out.println(equal);
    }
    
    public void run(){
        try {
            initializer.run(exams, schedule);
        } catch (Exception ex) {
            // hehe
        }
    }
    
    private void printA(){
        for(int i = 0; i< a.length; i++){
            System.out.println("");
            for(int j=0; j<a[0].length; j++){
                System.out.print(a[i][j]?1:0+" ");
            }
        }
    }

}
