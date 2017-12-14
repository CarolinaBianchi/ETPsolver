/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import optimization.domain.Exam;
import optimization.domain.Schedule;
import optimization.domain.Student;
import fileutils.FileManager;
import fileutils.SolutionWriter;
import optimization.initialization.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import optimization.metaheuristics.*;

/**
 * Class that manages the program logic, exploiting other classes functions.
 *
 * @author Carolina Bianchi
 */
public class Optimizer {

    private List<Exam> exams;
    private List<Student> students;
    private Map<Integer, Exam> eIdExam;
    private Schedule bestSchedule; //..
    private List<Schedule> initialSchedules = new ArrayList<>();
    private Set<AbstractInitializer> initializers = new HashSet<>();
    private Set<Class<? extends SingleSolutionMetaheuristic>> ssMetaheuristics = new HashSet<>();
    private Set<Class<? extends PopulationMetaheuristic>> pMetaheuristics = new HashSet<>();
    private int tmax;

    public Optimizer(String instanceName) throws IOException {
        init(instanceName);
        buildStudentListInEx();
        buildConflictingExamsLists();
        initInitializers();
        initMetaheuristics();
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
        bestSchedule = new Schedule(tmax);
        bestSchedule.setCost(0);
    }

    /**
     * Creates the set of initializers and joins their threads to the main
     * thread.
     */
    private void initInitializers() {
        for (int i = 0; i < 10; i++) {
            initializers.add(new BucketInitializer(Cloner.clone(exams), tmax, this));
        }
        /*initializers.add(new BucketInitializer(Cloner.clone(exams), tmax, this));*/
        joinThreads(initializers);

    }

    /**
     * Creates the set of metaheuristics.
     */
    private void initMetaheuristics() {
        ssMetaheuristics.add(SimulatedAnnealing.class);
        // we add every class that extends SingleSolutionMetaheuristic
        pMetaheuristics.add(GeneticAlgorithm.class);
        // we add every class that extends SingleSolutionMetaheuristic
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
     * As a new initial solution is generated, every SingleSolutionMetaheuristic
     * starts working on it.
     *
     * @param newInitialSol
     */
    public void updateOnNewInitialSolution(Schedule newInitialSol) {
        newInitialSol.updateCost();
        synchronized (initialSchedules) {
            this.initialSchedules.add(newInitialSol);
        }
        //runAllSSMetaheuristics(newInitialSol);
        checkAllPMetaheuristics();
    }

    /**
     * Runs every implementation of a SingleSolutoinMetaheuristics with the new
     * initial solution.
     *
     * @param newInitialSol
     */
    private void runAllSSMetaheuristics(Schedule newInitialSol) {

        Set<Thread> threadPool = new HashSet<>(3);
        // For each new initial solution 3 (random number) single solution metaheuristics 
        //of each type start working on it.
        //for (int i = 0; i < 3; i++) {
        for (Class<? extends SingleSolutionMetaheuristic> clazz : this.ssMetaheuristics) {
            try {
                Metaheuristic m = (clazz.getConstructor(Optimizer.class, Schedule.class))
                        .newInstance(this, Cloner.clone(newInitialSol));
                threadPool.add(m);
                m.start();
            } catch (Exception ex) {
                Logger.getLogger(Optimizer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //}
        joinThreads(threadPool);
    }

    /**
     * If the number of current initial solutions is enough for the Population
     * initializers, they're are created and started. The initial schedules are
     * reset and the initializers are run again.
     */
    private void checkAllPMetaheuristics() {
        if (this.initialSchedules.size() < PopulationMetaheuristic.INITIAL_POP_SIZE) {
            return;
        }
        Set<Thread> threadPool = new HashSet<>(3);
        /* Each time the minumum number of initial solutions is reached, 3 
        (random number) instances for each population metaheuristic that we have
        start working on them
         */
        //for (int i = 0; i < 3; i++) {
            for (Class<? extends PopulationMetaheuristic> clazz : this.pMetaheuristics) {
                try {
                    PopulationMetaheuristic m = (clazz.getConstructor(Optimizer.class, List.class)).newInstance(this, Cloner.clone(initialSchedules));
                    threadPool.add(m);
                    m.start();
                } catch (Exception ex) {
                    Logger.getLogger(Optimizer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        //}
        joinThreads(threadPool);
        synchronized (initialSchedules) {
            this.initialSchedules = new ArrayList<>();
        }
        //run(); to run again the initializers
    }

    /**
     * As a new solution is generated by some metaheuristic, compares the cost
     * of the scheduled obtained in that metaheuristics and if it is lower than
     * the one of the previous best schedule, it saves the new solution as the
     * best one and writes it. <code>bestSchedule</code> is initialized with
     * cost equal to 0, so the new solution is accepted also if the that initial
     * value is found.
     *
     * @param mySolution
     */
    public synchronized void updateOnNewSolution(Schedule mySolution) {
        if (bestSchedule.getCost() == 0 || mySolution.getCost() < bestSchedule.getCost()) {
            bestSchedule = mySolution;
            writeSolution();
        }
    }

    /**
     * Joins the set of threads to the main thread, so that the main thread
     * waits for their execution before dying.
     *
     * @param <T>
     * @param coll
     */
    private <T extends Thread> void joinThreads(Set<T> coll) {

        try {
            for (Thread t : coll) {
                t.join();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Optimizer.class.getName()).log(Level.SEVERE, null, ex);
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

    /**
     * When a new best solution is found it writes it.
     */
    private void writeSolution() {

        SolutionWriter sw = new SolutionWriter(bestSchedule);

        try {
            sw.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(Optimizer.class.getName()).log(Level.SEVERE, null, ex);
        }

        sw.start();

    }
}
