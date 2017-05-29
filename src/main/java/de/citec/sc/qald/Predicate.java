/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.qald;

import java.util.Objects;

/**
 *
 * @author sherzod
 */
public class Predicate implements Term {

    private String predicateName;
    boolean isVariable;

    public boolean IsVariable() {
        return isVariable;
    }

    public void setIsVariable(boolean isVariable) {
        this.isVariable = isVariable;
    }

    public String getPredicateName() {
        return predicateName;
    }

    public void setPredicateName(String predicateName) {
        this.predicateName = predicateName;
    }

    public Predicate(String predicateName, boolean isVariable) {
        this.predicateName = predicateName;
        this.isVariable = isVariable;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + Objects.hashCode(this.predicateName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Predicate other = (Predicate) obj;
        if (!Objects.equals(this.predicateName, other.predicateName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (isVariable) {
            return "?" + predicateName;
        }
        return predicateName;
    }

    @Override
    public Predicate clone() {
        return new Predicate(predicateName, isVariable);
    }

    @Override
    public boolean isVariable() {
        return IsVariable();
    }

}
