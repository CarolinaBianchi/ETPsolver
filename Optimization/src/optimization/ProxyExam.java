/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import java.util.Objects;

/**
 *
 * @author Carolina Bianchi
 */
public class ProxyExam {

    protected final Integer numStudents;
    protected final Integer id;

    public ProxyExam(Integer id, Integer numStudents) {
        this.id = id;
        this.numStudents = numStudents;
    }

    public ProxyExam(ProxyExam e) {
        this.id = e.getId();
        this.numStudents = e.getNumStudents();
    }

    public int getNumStudents() {
        return this.numStudents;
    }

    public int getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProxyExam) {
            return ((ProxyExam) obj).getId() == this.getId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + Objects.hashCode(this.id);
        return hash;
    }

}
