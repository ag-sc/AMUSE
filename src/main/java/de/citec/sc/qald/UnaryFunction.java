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
public class UnaryFunction implements Term {

    private String functionName;
    private String argumentName;

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getArgumentName() {
        return argumentName;
    }

    public void setArgumentName(String argumentName) {
        this.argumentName = argumentName;
    }

    public UnaryFunction() {
    }

    public UnaryFunction(String functionName, String argumentName) {
        this.functionName = functionName;
        this.argumentName = argumentName;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.functionName);
        hash = 97 * hash + Objects.hashCode(this.argumentName);
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
        final UnaryFunction other = (UnaryFunction) obj;
        if (!Objects.equals(this.functionName, other.functionName)) {
            return false;
        }
        if (!Objects.equals(this.argumentName, other.argumentName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return functionName + "(" + argumentName + ")";
    }

    @Override
    public Term clone() {
        return new UnaryFunction(functionName, argumentName);
    }

    @Override
    public boolean isVariable() {
        return false;
    }

}
