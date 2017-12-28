/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import optimization.metaheuristics.preprocessing.CheapestInsertion;
import optimization.domain.Exam;
import optimization.domain.Schedule;
import optimization.domain.Student;
import fileutils.FileManager;
import fileutils.SolutionWriter;
import optimization.initialization.*;
import java.io.IOException;
import java.text.DecimalFormat;
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
    }

    /**
     * Creates the set of initializers and joins their threads to the main
     * thread.
     */
    private void initInitializers() {
        for (int i = 0; i < PopulationMetaheuristic.INITIAL_POP_SIZE; i++) {
            initializers.add(new BucketInitializer(Cloner.clone(exams), tmax, this, this.students.size()));
        }
        joinThreads(initializers);

    }

    /**
     * Creates the set of metaheuristics.
     */
    private void initMetaheuristics() {
        //ssMetaheuristics.add(TabuSearchAlgorithm.class);
        // we add every class that extends SingleSolutionMetaheuristic
        pMetaheuristics.add(GeneticAlgorithm.class);
        // we add every class that extends SingleSolutionMetaheuristic
        ssMetaheuristics.add(DeepDiveAnnealingV2.class);
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
//        synchronized (initialSchedules) {
//            this.initialSchedules = new ArrayList<>();
//        }
        //run(); to run again the initializers
    }

    /**
     * As a new initial solution is generated, it is preprocessed by means of
     * the CheapestInsertion metaheuristic.
     *
     * @param newInitialSol
     */
    public void updateOnNewInitialSolution(Schedule newInitialSol) {
        System.out.println("Initial");
        printResult(newInitialSol);
        (new CheapestInsertion(this, newInitialSol)).run();

    }

    /**
     * As the solution is processed by the CheapestInsertion metaheuristic, the
     * SingleSolutionMetaheursitic are run.
     *
     * @param preprocessed
     */
    public void updateOnNewPreprocessedSolution(Schedule preprocessed) {
        System.out.println("After preprocessing");
        printResult(preprocessed);
        runAllSSMetaheuristics(preprocessed);
        if (bestSchedule == null || preprocessed.getCost() < bestSchedule.getCost()) {
            bestSchedule = preprocessed;
        }
    }

    /**
     * As a new solution is generated by some metaheuristic, it is saved in a
     * list of initial solution. The list is then checked to see if we have
     * enough initial solution to start the PopulationMetaheuristics.
     *
     * @param mySolution
     */
    public void updateOnNewSolution(Schedule mySolution) {
        System.out.println("After single solution");
        printResult(mySolution);

        synchronized (initialSchedules) {
            this.initialSchedules.add(mySolution);
        }
        if (mySolution.getCost() < bestSchedule.getCost()) {
            bestSchedule = mySolution;
        }
        checkAllPMetaheuristics();

    }

    /**
     * As a new final solution is generated, the best solution is picked and
     * printed.
     *
     * @param mySolution
     */
    public void updateOnFinalSolution(Schedule mySolution) {
        if (mySolution.getCost() < bestSchedule.getCost()) {
            bestSchedule = mySolution;
        }
        writeSolution();
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
                synchronized (this) {
                    this.wait(10);
                    in.start();
                }
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
        printResult();
        AbsoluteBestChecker.checkIfBestEver(bestSchedule);
    }

    private void printResult() {
        printResult(bestSchedule);
    }

    private void printResult(Schedule mySchedule) {
        int benchmark = Optimization.getBenchmark();
        DecimalFormat df = new DecimalFormat("##.00");
        double gap = 100 * ((mySchedule.getCost() * 1.0 - benchmark * 1.0) / benchmark * 1.0);
        System.out.println("OurSolution:" + mySchedule.getCost() + "\tBenchmark:" + benchmark + "\t gap:" + df.format(gap) + "%");
    }

}
