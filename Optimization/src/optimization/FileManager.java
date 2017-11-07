/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that manages file reads and writes.
 *
 * @author Carolina Bianchi
 */
public class FileManager {

    private static final String EXAM_SUFFIX = ".exm";
    private static final String STUDENT_SUFFIX = ".stu";
    private static final String TIMESLOT_SUFFIX = ".slo";
    private static final String FILES_PATH = "files/";

    /**
     * Reads the exam file and builds the list of exams.
     *
     * @param instance the name of the instance
     * @return an ArrayList of exams
     * @throws IOException
     */
    public static List<Exam> readExams(String instance) throws IOException {
        List<Exam> exams = new ArrayList<>();
        String[] tokens;
        try (BufferedReader in = new BufferedReader(new FileReader(FILES_PATH+instance+EXAM_SUFFIX))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (!line.isEmpty()) {
                    tokens = line.split(" ");
                    Exam ex = new Exam(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
                    exams.add(ex);
                    //System.out.println(ex);
                }
            }
        }
        return exams;
    }

    /**
     * Reads students file and builds the list of students.
     *
     * @param instance the name of the instance
     * @return an List<Student>
     * @throws IOException
     */
    public static List<Student> readStudents(String instance) throws IOException {
        List<Student> students = new ArrayList();
        try (BufferedReader in = new BufferedReader(new FileReader(FILES_PATH+instance+STUDENT_SUFFIX))) {
            String line;
            String[] tokens;
            Student st;
            while ((line = in.readLine()) != null) {
                if (!line.isEmpty()) {
                    tokens = line.split(" ");
                    st = new Student(tokens[0]);
                    st.addExamId(Integer.parseInt(tokens[1]));
                    students.add(st);
                    //System.out.println(st);
                }
            }
        }
        return students;
    }

    /**
     * Reads the timeslots file.
     *
     * @param instance the name of the instance.
     * @return the number of timeslots.
     * @throws IOException
     */
    public static int readTimeslots(String instance) throws IOException {
        String line;
        try (BufferedReader in = new BufferedReader(new FileReader(FILES_PATH+instance+TIMESLOT_SUFFIX));) {
            line = in.readLine();
            if (!line.trim().isEmpty()) {
                return Integer.parseInt(line.trim());
            }
        }
        throw new IOException(); // I should never get here
    }

}
