/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class in charge to clone object and collections of objects.
 *
 * @author Carolina Bianchi
 */
public class Cloner {

  
    /**
     * Method in charge of cloning an object.
     * @param <T> the class of the object
     * @param obj the object that has to be cloned
     * @return the clone
     */
    static public <T extends Cloneable> T clone(T obj) {

        try {
            return (T) obj.getClass().getMethod("clone").invoke(obj);
        } catch (Exception ex) {
            Logger.getLogger(Cloner.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        }
        return null;
    }
    
    /**
     * Clones a collection of objects that implement Cloneable.
     * @param <T> the class of the object
     * @param objs the collection that has to be cloned
     * @return a clone of that collection
     */
    static public <T extends Cloneable> List<T> clone(Collection<T> objs) {
        List<T> newList = new ArrayList<>(objs.size());
        for(Cloneable obj : objs){
            newList.add((T) clone(obj));
        }
        return newList;
    }
   

}
