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
public class Variable implements Term {

    private String variableName;

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public Variable(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.variableName);
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
        final Variable other = (Variable) obj;
        if (!Objects.equals(this.variableName, other.variableName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return variableName;
    }

    @Override
    public Term clone() {
        return new Variable(variableName);
    }

    @Override
    public boolean isVariable() {
        return true;
    }
}
