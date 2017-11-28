/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Elisa
 */
public class Optimization {

    /* This assigmnent has to be deleted in the future when we will launch the 
    program only from command line (java -jar "Optimization.jar" instancename).
    When calling the program from command line, you have to move the "files" 
    folder in the same folder of the jar.
     */
    public static String instance = "instance06";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //checkArgs(args);  this has to be uncommented when running the program from command line.
        try {
            // TODO code application logic here
            Optimizer optimizer = new Optimizer(instance);
            optimizer.run();
        } catch (IOException ex) {
            Logger.getLogger(Optimization.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Checks the format of the argument passed when running the program from
     * command line.
     *
     * @param args
     */
    private static void checkArgs(String[] args) {
        if (args.length != 1) {
            System.out.println("Input argument error. You can launch the program by writing java -jar \"Optimization.jar\" instanceName");
            System.exit(0);
        }
        instance = args[0];
    }
}
