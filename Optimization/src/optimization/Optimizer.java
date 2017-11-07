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

/**
 * Class that manages the program logic, exploiting other classes functions.
 *
 * @author Carolina Bianchi
 */
public class Optimizer {

    private List<Exam> exams;
    private List<Student> students;
    private Map<Integer, Exam> eIdExam;
    private int tmax;
    private final int S; // number of students
    private final int E; // number of exams
    private boolean[][] a; // a matrix: a[i][j] is true if student i is enrolled in exam j.

    public Optimizer(String instanceName) throws IOException {
        init(instanceName);
        S = students.size();
        E = exams.size();
        a = new boolean[S][E];
        buildStudentListInEx();
        //checkExamEnrollments(); //DEBUG
    }

    /**
     * Builds the attributes exams, students and tmax exploiting
     * FileManager's methods. HINT: We could run these 3 methods on 3 different
     * threads.
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

}
