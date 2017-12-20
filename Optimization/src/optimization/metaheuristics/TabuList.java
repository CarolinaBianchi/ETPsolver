/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.metaheuristics;

import java.util.LinkedList;
import optimization.domain.Exam;

/** This class represents a tabu list and it offers useful methods to manage
 * each instance of the class.
 *
 * @author flavio
 */
public class TabuList {
    
    private LinkedList<String> tabuList;
    private int tabuListSize;
    private final int MIN_TABU_SIZE = 1; // Minimum size of the tabu list
    
    public TabuList(int tabuListSize) {
        this.tabuListSize = tabuListSize;
        tabuList = new LinkedList<>();
    }
    
    /**
     * Check the presence of the move in the tabu list.
     * !!! ACHTUNG !!! - It does not check that the move is the best possible one. 
     * @param e The exam to move
     * @param src The source timeslot id
     * @param dest The destination timeslot id
     * @return True - If the move is allowed, False - Otherwise
     */
    public boolean moveIsTabu(Exam e, int src, int dest) {
        return tabuList.contains( this.getMoveHash(e, src, dest));
    }
    
    /**
     * Returns a string representing the move of exam ex from timeslot
     *
     * @sourceIndex to timeslot @destIndex.
     *
     * @param sourceIndex
     * @param destIndex
     * @param ex
     * @return
     */
    public String getMoveHash(Exam ex, int sourceIndex, int destIndex ) {
        return sourceIndex + "-" + destIndex + "-" + ex;
    }

    /**
     * Updates the tabu list. It inserts the current move at the top of the tabu
     * list and removes the first one if the size of the list exceeds its
     * maximum.
     * 
     * @param e
     * @param src
     * @param dest 
     */
    public void updateTabuList(Exam e, int src, int dest) {
        String moveHash = getMoveHash(e, src, dest);
        
        if (tabuList.size() < tabuListSize) {
            if (!tabuList.contains(moveHash)) {
                /**
                 * I insert in the tabuList a String containing information
                 * about the move I have to prevent
                 */
                tabuList.addFirst(moveHash);
            }
        } else if (!tabuList.contains(moveHash)) {
            tabuList.removeLast();
            tabuList.addFirst(moveHash);
        }
    }
    
    /**
     * Decrement the tabu list size by one
     */
    public void reduceTabuList() {
        if( tabuList.size() == tabuListSize) {
            tabuList.removeLast();
        }
        
        if(tabuListSize!=MIN_TABU_SIZE) {
            tabuListSize--;
        }
    }
    
}
