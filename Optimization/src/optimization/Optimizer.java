/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import fileutils.FileManager;
import optimization.initialization.*;
import java.io.IOException;
import java.util.ArrayList;
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
    // In case we want to implement a multistart algorithm.
    private List<Schedule> initialSchedules;
    private List<AbstractInitializer> initializers;
    private int tmax;
    // (I'm not sure any of these last 3 attributes is useful)
    private final int S; // number of students
    private final int E; // number of exams
    private boolean[][] a; // a matrix: a[i][j] is true if student i is enrolled in exam j. 

    public Optimizer(String instanceName) throws IOException {
        init(instanceName);
        S = students.size();
        E = exams.size();
        a = new boolean[S][E];
        initialSchedules = new ArrayList<>();
        buildStudentListInEx();
        buildConflictingExamsLists();
        initInitializers();
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

    private void initInitializers() {
        
        AbstractInitializer randomInit = new BucketInitializer(Cloner.clone(exams), initialSchedules, tmax)/*,
                randomInit2 = new BucketInitializer(Cloner.clone(exams), initialSchedules, tmax)*/;

        //AbstractInitializer someOther = new SomeOtherInitializer(Cloner.clone(exams), schedules, tmax);
        initializers = new ArrayList<>();
        initializers.add(randomInit);
        //initializers.add(randomInit2);
        //inizializers.add(someOther);
        try {
            ((Thread) randomInit).join();
            //((Thread) randomInit2).join();
            //((Thread)someOther).join();
        } catch (InterruptedException ex) {
            Logger.getLogger(Optimizer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Adds the students to the exams in which they're enrolled and builds the
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
                //exam.addStudent(student);    //In the future one of this two lines may be useless
                student.addExam(exam);
                a[i][exams.indexOf(exam)] = true;
            }
        }
    }

    /**
     * Builds the list of conflicting exams in each exam.
     */
    private void buildConflictingExamsLists() {
        List<Exam> sExams;
        Exam e1, e2;
        for (Student s : students) {
            sExams = s.getExams();
            for (int i = 0; i < sExams.size(); i++) {
                e1 = sExams.get(i);
                for (int j = i + 1; j < sExams.size(); j++) {
                    e2 = sExams.get(j);
                    e1.addConflictingExam(e2.getId());
                    e2.addConflictingExam(e1.getId());
                    
                    // Used for adding the conflicts to the exam conflict map.
                    e1.addConflictingExam2(e2.getId());
                    e2.addConflictingExam2(e1.getId());
                }
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
     * Starts the initializers' threads.
     */
    public void run() {
        try {
            for (AbstractInitializer in : this.initializers) {
                in.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
