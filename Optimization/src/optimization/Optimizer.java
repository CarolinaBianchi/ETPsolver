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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    private Timer timer;
    public boolean endAll;

    public Optimizer(String instanceName, int tlim) throws IOException {
        init(instanceName);
        buildStudentListInEx();
        buildConflictingExamsLists();
        initInitializers();
        initMetaheuristics();
        timer = new Timer(1000 * tlim, this);
        joinThread(timer);
    }

    /**
     * Returns the list of schedules.
     *
     * @return
     */
    public List<Schedule> getInitialSchedules() {
        return this.initialSchedules;
    }

    /**
     * Adds a new schedule to the set of initial schedules.
     *
     * @param schedule
     */
    private void addSchedule(Schedule schedule) {
        synchronized (initialSchedules) {
            if (!initialSchedules.contains(schedule)) {
                initialSchedules.add(schedule);
            }
        }
    }

    /*-------------------------- INITIALIZATION -----------------------------*/
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

    /*------------------------------ initialization end-----------------------*/
    /**
     * Creates the set of initializers and joins their threads to the main
     * thread.
     */
    protected void initInitializers() {
        initInitializers(PopulationMetaheuristic.INITIAL_POP_SIZE);
    }

    /**
     * Creates the set of initializers and joins their threads to the main
     * thread.
     *
     * @param nInit number of initializers
     */
    protected void initInitializers(int nInit) {
        initializers = new HashSet<>();
        for (int i = 0; i < nInit; i++) {
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
     * Runs every implementation of a SingleSolutoinMetaheuristics having as
     * initial solution <code>schedule</code>.
     *
     * @param schedule
     */
    protected void runAllSSMetaheuristics(Schedule schedule) {

        Set<Thread> threadPool = new HashSet<>(3);
        // For each new initial solution a single solution metaheuristic starts working on it.
        for (Class<? extends SingleSolutionMetaheuristic> clazz : this.ssMetaheuristics) {
            try {
                Metaheuristic m = (clazz.getConstructor(Optimizer.class, Schedule.class, long.class))
                        .newInstance(this, Cloner.clone(schedule), Timer.INIT_SOL_TIME);
                threadPool.add(m);
                m.start();
            } catch (Exception ex) {
                Logger.getLogger(Optimizer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        joinThreads(threadPool);
    }

    /**
     * Runs every implementation of a SingleSolutoinMetaheuristics having as
     * initial solution the last solution added to the set of initial schedules.
     *
     * @param schedule
     */
    protected void runAllSSMetaheuristics() {
        runAllSSMetaheuristics(this.initialSchedules.get(initialSchedules.size() - 1));
    }

    /**
     * Runs every implementation of a SingleSolutoinMetaheuristics having as
     * initial solution the first <code> number</code> solutions added to
     * <code>initialSchedules</code>.
     *
     * @param schedule
     */
    private void runAllSSMetaheuristics(int number) {
        List<Schedule> initialSchedules;
        synchronized (this.initialSchedules) {
            initialSchedules = Cloner.clone(this.initialSchedules);
        }
        Collections.sort(initialSchedules);
        for (int i = 0; i < number; i++) {
            Schedule newInitialSol = initialSchedules.get(i);
            runAllSSMetaheuristics(newInitialSol);
        }

    }

    /**
     * If the number of current initial solutions is enough for the Population
     * initializers, they're are created and started. The initial schedules are
     * reset and the initializers are run again.
     */
    public void checkAllPMetaheuristics() {
        if (this.initialSchedules.size() < PopulationMetaheuristic.INITIAL_POP_SIZE) {
            return;
        }
        Set<Thread> threadPool = new HashSet<>(3);
        /* Each time the minumum number of initial solutions is reached, 3 
        (random number) instances for each population metaheuristic that we have
        start working on them
         */
        synchronized (initialSchedules) {
            for (Class<? extends PopulationMetaheuristic> clazz : this.pMetaheuristics) {
                try {
                    PopulationMetaheuristic m = (clazz.getConstructor(Optimizer.class, List.class, long.class))
                            .newInstance(this, Cloner.clone(initialSchedules), Timer.METAHEURISTICS_TIME);
                    threadPool.add(m);
                    m.start();
                } catch (Exception ex) {
                    Logger.getLogger(Optimizer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        this.runAllSSMetaheuristics(Timer.MAX_THREADS - Timer.POP_THREADS);
        //joinThreads(threadPool);
    }

    /**
     * As a new initial solution is generated, it is preprocessed by means of
     * the CheapestInsertion metaheuristic.
     *
     * @param newInitialSol
     */
    public void updateOnNewInitialSolution(Schedule newInitialSol) {
        //System.out.println("Initial");
        addSchedule(newInitialSol);
        //printResult(newInitialSol);
        checkBest(newInitialSol);
        printResult();
        (new CheapestInsertion(this, newInitialSol, 10000)).run();

    }

    /**
     * As the solution is processed by the CheapestInsertion metaheuristic, the
     * SingleSolutionMetaheursitic are run.
     *
     * @param preprocessed
     */
    public void updateOnNewPreprocessedSolution(Schedule preprocessed) {
        //System.out.println("After preprocessing");
        addSchedule(preprocessed);
        checkBest(preprocessed);
        printResult();
        //runAllSSMetaheuristics(preprocessed);
        timer.checkTime();
    }

    /**
     * As a new solution is generated by some metaheuristic, it is saved in a
     * list of initial solution. The list is then checked to see if we have
     * enough initial solution to start the PopulationMetaheuristics.
     *
     * @param mySolution
     */
    public void updateOnNewSolution(Schedule mySolution) {
        //System.out.println("After single solution");
        addSchedule(mySolution);
        checkBest(mySolution);
        printResult();
        timer.checkTime();
        //checkAllPMetaheuristics();

    }

    /**
     *
     * @param schedules
     */
    public void updateOnPopulationSolution(List<Schedule> schedules) {
        for (Schedule schedule : schedules) {
            addSchedule(schedule);
            checkBest(schedule);
            printResult();
            //timer.checkTime();
        }
    }

    /**
     * Selects only the first <code>INIT_POP_SIZE</code> schedules considering
     * their cost.
     */
    protected void naturalSelection() {

        synchronized (initialSchedules) {
            Collections.sort(initialSchedules);
            while (initialSchedules.size() > PopulationMetaheuristic.INITIAL_POP_SIZE) {
                if (probability80()) {
                    initialSchedules.remove(initialSchedules.size() - 1);
                }
            }
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

        //try {
        for (Thread t : coll) {
            joinThread(t);
        }
        //t.join();
        //}
        //} catch (InterruptedException ex) {
        //    Logger.getLogger(Optimizer.class.getName()).log(Level.SEVERE, null, ex);
        //}

    }

    private <T extends Thread> void joinThread(T thread) {
        try {
            thread.join();
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
    protected synchronized void writeSolution() {
        if (this.endAll) {
            return;
        }
        this.endAll = true;
        SolutionWriter sw = new SolutionWriter(bestSchedule);
        //System.out.println("Final solution");
        printResult();
        sw.writeSolution();
        //AbsoluteBestChecker.checkIfBestEver(bestSchedule);
        System.exit(0);
    }

    /**
     * Prints the result related to the current best schedule.
     */
    private void printResult() {
        printResult(bestSchedule);
    }

    /**
     * Prints the result related to a schedule.
     *
     * @param mySchedule
     */
    private void printResult(Schedule mySchedule) {
        //int benchmark = Optimization.getBenchmark();
        DecimalFormat df = new DecimalFormat("##.00");
        //double gap = 100 * ((mySchedule.getCost() * 1.0 - benchmark * 1.0) / benchmark * 1.0);
        //System.out.println("OurSolution:" + mySchedule.getCost() + "\tBenchmark:" + benchmark + "\t gap:" + df.format(gap) + "%");
        System.out.println("Best: "+mySchedule.getCost());
    }

    /**
     * Returns true with a probability of 90%.
     *
     * @return
     */
    private boolean probability80() {
        return (new Random()).nextDouble() < 0.8;
    }

    /**
     * Checks if the solution is the best ever found.
     *
     * @param mySolution
     */
    private void checkBest(Schedule mySolution) {
        if (bestSchedule == null || mySolution.getCost() < bestSchedule.getCost()) {
            bestSchedule = mySolution;
        }
    }

}
